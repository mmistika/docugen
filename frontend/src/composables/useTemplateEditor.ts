import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useEditor } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import { Node, mergeAttributes } from '@tiptap/core';
import type { Editor } from '@tiptap/core';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';
import type { Field, FieldType } from '@/types/field';
import { createField } from '@/types/field'
import type { TemplateVersionStatus } from '@/types/template';


export const InlineFieldNode = Node.create({
  name: 'inlineField',
  group: 'inline',
  inline: true,
  selectable: true,
  atom: true,

  addAttributes() {
    return {
      id:       { default: null },
      name:     { default: 'new_field' },
      type:     { default: 'text' },
      required: { default: false },
    };
  },

  parseHTML() {
    return [{ tag: 'span[data-type="inline-field"]' }];
  },

  renderHTML({ HTMLAttributes }) {
    return [
      'span',
      mergeAttributes(HTMLAttributes, {
        'data-type': 'inline-field',
        class:
          'inline-flex items-center px-1.5 py-0.5 rounded text-xs font-mono font-medium ' +
          'bg-blue-100 text-blue-800 border border-blue-200 cursor-pointer mx-1 select-all ' +
          'transition-colors hover:bg-blue-200',
      }),
      `{${HTMLAttributes.name}}`,
    ];
  },
});

export function useTemplateEditor() {
  const route    = useRoute();
  const router   = useRouter();
  const orgStore = useOrgStore();

  const isNew = computed(
    () => !route.params.id || route.params.id === 'new'
  );
  const currentTemplateId = ref<number | null>(
    isNew.value ? null : Number(route.params.id)
  );

  const templateName          = ref(isNew.value ? 'New Template' : '');
  const selectedFieldId       = ref<string | null>(null);
  const fieldsExpanded        = ref(true);
  const inlineFieldsExpanded  = ref(true);
  const isLoading             = ref(true);
  const hasChanges            = ref(false);
  const status                = ref<TemplateVersionStatus>('DRAFT');

  const globalFields = ref<Field[]>([]);
  const inlineFields = ref<Field[]>([]);

  const editor = useEditor({
    content: '',
    extensions: [StarterKit, InlineFieldNode],
    editorProps: {
      attributes: { class: 'prose max-w-none focus:outline-none min-h-[1056px]' },
    },
    onUpdate: ({ editor }) => {
      hasChanges.value = true;
      syncInlineFields(editor);
    },
    onTransaction: ({ editor }) => {
      syncInlineFields(editor);
    },
    onSelectionUpdate: ({ editor }) => {
      if (editor.isActive('inlineField')) {
        selectedFieldId.value = editor.getAttributes('inlineField').id;
      }
    },
  });

  const syncInlineFields = (ed: Editor) => {
    const editorNodes: any[] = [];
    ed.state.doc.descendants((node: any) => {
      if (node.type.name === 'inlineField') {
        editorNodes.push(node.attrs);
      }
    });

    const updatedInlineFields: Field[] = editorNodes.map(nodeAttrs => {
      const existingConfig = inlineFields.value.find(f => f.id === nodeAttrs.id);
      if (existingConfig) {
        return { ...existingConfig, ...nodeAttrs } as Field;
      }

      const newConfig = createField(nodeAttrs.type || 'text', 'inline');
      return { ...newConfig, ...nodeAttrs } as Field;
    });

    if (JSON.stringify(updatedInlineFields) !== JSON.stringify(inlineFields.value)) {
      inlineFields.value = updatedInlineFields;
      hasChanges.value   = true;
    }
  };

  const init = async () => {
    if (!orgStore.currentOrgId) await orgStore.fetch();

    if (!isNew.value && currentTemplateId.value && orgStore.currentOrgId) {
      try {
        const data = await api.organisations.templates.getForEdit(
          orgStore.currentOrgId,
          currentTemplateId.value
        );
        templateName.value = data.name;
        status.value       = data.status;

        if (data.manifest) {
          const parsed       = JSON.parse(data.manifest);
          globalFields.value = parsed.fields        ?? [];
          inlineFields.value = parsed.inline_fields ?? [];
        }

        editor.value?.commands.setContent(data.content);
        setTimeout(() => (hasChanges.value = false), 100);
      } catch {
      }
    } else if (isNew.value && editor.value) {
      editor.value.commands.setContent(
        `<p>New Template.</p>`
      );
      setTimeout(() => (hasChanges.value = false), 100);
    }

    isLoading.value = false;
  };

  const destroyEditor = () => editor.value?.destroy();

  watch([templateName, globalFields], () => (hasChanges.value = true), { deep: true });

  const manifestJson = computed(() =>
    JSON.stringify(
      {
        manifest_version: 1,
        template:         { name: templateName.value },
        fields:           globalFields.value,
        inline_fields:    inlineFields.value,
      },
      null,
      2
    )
  );

  const handleSave = async () => {
    if (!orgStore.currentOrgId || !editor.value) return;
    const payload = {
      name:     templateName.value,
      manifest: manifestJson.value,
      content:  editor.value.getHTML(),
    };
    try {
      if (isNew.value) {
        const newId = await api.organisations.templates.create(
          orgStore.currentOrgId,
          payload
        );
        hasChanges.value = false;
        await router.replace(`/templates/${newId}`);
      } else if (currentTemplateId.value) {
        await api.organisations.templates.update(
          orgStore.currentOrgId,
          currentTemplateId.value,
          payload
        );
        hasChanges.value = false;
        status.value     = 'DRAFT';
      }
    } catch {
    }
  };

  const handlePublish = async () => {
    if (!orgStore.currentOrgId || !currentTemplateId.value) return;
    try {
      if (hasChanges.value) await handleSave();
      await api.organisations.templates.publish(
        orgStore.currentOrgId,
        currentTemplateId.value
      );
      status.value     = 'ACTIVE';
      hasChanges.value = false;
    } catch {
    }
  };

  const addGlobalField = (type: FieldType) => {
    const field = createField(type, 'global');
    globalFields.value.push(field);
    selectedFieldId.value = field.id;
  };

  const insertInlineField = (type: FieldType) => {
    if (!editor.value) return;
    const field = createField(type, 'inline');

    inlineFields.value.push(field);

    editor.value
      .chain()
      .focus()
      .insertContent({ type: 'inlineField', attrs: field })
      .insertContent(' ')
      .run();
    selectedFieldId.value = field.id;
  };

  const activeField = computed(
    () =>
      globalFields.value.find((f) => f.id === selectedFieldId.value) ||
      inlineFields.value.find((f) => f.id === selectedFieldId.value) ||
      null
  );

  const isInlineFieldActive = computed(() =>
    inlineFields.value.some((f) => f.id === selectedFieldId.value)
  );

  const updateActiveField = (key: string, value: unknown) => {
    if (!selectedFieldId.value) return;

    const globalMatch = globalFields.value.find((f) => f.id === selectedFieldId.value);
    if (globalMatch) {
      (globalMatch as Record<string, unknown>)[key] = value;
      return;
    }

    const inlineMatch = inlineFields.value.find((f) => f.id === selectedFieldId.value);
    if (inlineMatch) {
      (inlineMatch as Record<string, unknown>)[key] = value;
      if (['name', 'type', 'required'].includes(key) && editor.value) {
        const { state, view } = editor.value;
        let tr = state.tr;
        state.doc.descendants((node: any, pos: number) => {
          if (node.type.name === 'inlineField' && node.attrs.id === selectedFieldId.value) {
            tr = tr.setNodeMarkup(pos, null, { ...node.attrs, [key]: value });
          }
        });
        view.dispatch(tr);
      }

      hasChanges.value = true;
    }
  };

  return {
    isNew, isLoading,
    templateName, selectedFieldId,
    fieldsExpanded, inlineFieldsExpanded,
    hasChanges, status,
    globalFields, inlineFields,
    editor, manifestJson,
    activeField, isInlineFieldActive,
    init, destroyEditor,
    handleSave, handlePublish,
    addGlobalField, insertInlineField, updateActiveField,
  };
}