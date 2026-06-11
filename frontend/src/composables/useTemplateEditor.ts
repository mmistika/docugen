/*
 * Docugen — Document Generation & Management Platform
 * Copyright (C) 2026 Artem Bilous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useEditor } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import type { Editor } from '@tiptap/core';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';
import type { Field, FieldType } from '@/types/field';
import { createField, FIELD_TYPE_MAP } from '@/types/field';
import type { TemplateVersionStatus } from '@/types/template';
import { InlineFieldNode } from '@/editor/extensions/InlineFieldNode';
import TextAlign from '@tiptap/extension-text-align';
import { FontSize, TextStyle } from '@tiptap/extension-text-style';
import { Color } from '@tiptap/extension-color';
import { Highlight } from '@tiptap/extension-highlight';
import { FontFamily } from '@tiptap/extension-font-family';

const areFieldsEqual = (a: Field[], b: Field[]): boolean => {
    if (a.length !== b.length) return false;
    for (let i = 0; i < a.length; i++) {
        const fA = a[i]!;
        const fB = b[i]!;

        const keysA = Object.keys(fA) as (keyof Field)[];
        const keysB = Object.keys(fB) as (keyof Field)[];
        if (keysA.length !== keysB.length) return false;

        for (const key of keysA) {
            if (fA[key] !== fB[key]) return false;
        }
    }
    return true;
};

export function useTemplateEditor() {
    const route = useRoute();
    const router = useRouter();
    const orgStore = useOrgStore();

    const isNew = computed(() => !route.params.id || route.params.id === 'new');
    const currentTemplateId = ref<number | null>(
        isNew.value ? null : Number(route.params.id)
    );

    const templateName = ref(isNew.value ? 'New Template' : '');
    const selectedFieldId = ref<string | null>(null);
    const isLoading = ref(true);
    const hasChanges = ref(false);
    const status = ref<TemplateVersionStatus>('DRAFT');

    const globalFields = ref<Field[]>([]);
    const inlineFields = ref<Field[]>([]);

    const editor = useEditor({
        content: '',
        extensions: [
            StarterKit,
            InlineFieldNode,
            TextAlign.configure({
                types: ['heading', 'paragraph'],
                alignments: ['left', 'center', 'right', 'justify']
            }),
            TextStyle,
            Color,
            Highlight.configure({
                multicolor: true
            }),
            FontSize,
            FontFamily
        ],
        editorProps: {
            attributes: {
                class: 'focus:outline-none'
            }
        },
        onUpdate: ({ editor }) => {
            hasChanges.value = true;
            syncInlineFields(editor);
        },
        onSelectionUpdate: ({ editor }) => {
            if (editor.isActive('inlineField')) {
                selectedFieldId.value = editor.getAttributes('inlineField').id;
            }
        }
    });

    const syncInlineFields = (ed: Editor) => {
        const editorNodes: any[] = [];
        ed.state.doc.descendants((node: any) => {
            if (node.type.name === 'inlineField') {
                editorNodes.push(node.attrs);
            }
        });

        const updatedInlineFields: Field[] = editorNodes.map((nodeAttrs) => {
            const existingConfig = inlineFields.value.find(
                (f) => f.id === nodeAttrs.id
            );
            if (existingConfig && existingConfig.type === nodeAttrs.type) {
                return { ...existingConfig, ...nodeAttrs } as Field;
            }

            const newConfig = createField(nodeAttrs.type || 'text', 'inline');
            return { ...newConfig, ...nodeAttrs } as Field;
        });

        if (!areFieldsEqual(updatedInlineFields, inlineFields.value)) {
            inlineFields.value = updatedInlineFields;
            hasChanges.value = true;
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
                status.value = data.status;

                if (data.manifest) {
                    const parsed = JSON.parse(data.manifest);
                    globalFields.value = parsed.fields ?? [];
                    inlineFields.value = parsed.inline_fields ?? [];
                }

                editor.value?.commands.setContent(data.content);
                setTimeout(() => (hasChanges.value = false), 100);
            } catch (error) {
                console.error(
                    'Failed to load template details for editor:',
                    error
                );
            }
        } else if (isNew.value && editor.value) {
            editor.value.commands.setContent(`<p>New Template.</p>`);
            setTimeout(() => (hasChanges.value = false), 100);
        }

        isLoading.value = false;
    };

    const destroyEditor = () => editor.value?.destroy();

    watch([templateName, globalFields], () => (hasChanges.value = true), {
        deep: true
    });

    const manifestJson = computed(() =>
        JSON.stringify(
            {
                manifest_version: 1,
                template: { name: templateName.value },
                fields: globalFields.value,
                inline_fields: inlineFields.value
            },
            null,
            2
        )
    );

    const handleSave = async () => {
        if (!orgStore.currentOrgId || !editor.value) return;
        const payload = {
            name: templateName.value,
            manifest: manifestJson.value,
            content: editor.value.getHTML()
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
                status.value = 'DRAFT';
            }
        } catch (error) {
            console.error('Failed to save template draft:', error);
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
            status.value = 'ACTIVE';
            hasChanges.value = false;
        } catch (error) {
            console.error('Failed to publish template:', error);
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

    const activeField = computed(() => {
        const id = selectedFieldId.value;
        if (!id) return null;
        return (
            globalFields.value.find((f) => f.id === id) ||
            inlineFields.value.find((f) => f.id === id) ||
            null
        );
    });

    const isInlineFieldActive = computed(() =>
        inlineFields.value.some((f) => f.id === selectedFieldId.value)
    );

    const updateActiveField = (key: string, value: unknown) => {
        if (!selectedFieldId.value) return;

        const globalMatch = globalFields.value.find(
            (f) => f.id === selectedFieldId.value
        );
        if (globalMatch) {
            if (key === 'type') {
                const newType = value as FieldType;
                const def = FIELD_TYPE_MAP[newType];
                const updatedField = {
                    id: globalMatch.id,
                    name: globalMatch.name,
                    type: newType,
                    required: globalMatch.required,
                    ...def.defaults
                } as Field;
                const idx = globalFields.value.findIndex(
                    (f) => f.id === selectedFieldId.value
                );
                if (idx !== -1) {
                    globalFields.value[idx] = updatedField;
                }
            } else {
                (globalMatch as Record<string, unknown>)[key] = value;
            }
            return;
        }

        const inlineMatch = inlineFields.value.find(
            (f) => f.id === selectedFieldId.value
        );
        if (inlineMatch) {
            if (key === 'type') {
                const newType = value as FieldType;
                const def = FIELD_TYPE_MAP[newType];
                const updatedField = {
                    id: inlineMatch.id,
                    name: inlineMatch.name,
                    type: newType,
                    required: inlineMatch.required,
                    ...def.defaults
                } as Field;
                const idx = inlineFields.value.findIndex(
                    (f) => f.id === selectedFieldId.value
                );
                if (idx !== -1) {
                    inlineFields.value[idx] = updatedField;
                }
            } else {
                (inlineMatch as Record<string, unknown>)[key] = value;
            }

            if (['name', 'type', 'required'].includes(key) && editor.value) {
                const { state, view } = editor.value;
                let tr = state.tr;
                state.doc.descendants((node: any, pos: number) => {
                    if (
                        node.type.name === 'inlineField' &&
                        node.attrs.id === selectedFieldId.value
                    ) {
                        tr = tr.setNodeMarkup(pos, null, {
                            ...node.attrs,
                            [key]: value
                        });
                    }
                });
                view.dispatch(tr);
            }

            hasChanges.value = true;
        }
    };

    return {
        isNew,
        isLoading,
        templateName,
        selectedFieldId,
        hasChanges,
        status,
        globalFields,
        inlineFields,
        editor,
        manifestJson,
        activeField,
        isInlineFieldActive,
        init,
        destroyEditor,
        handleSave,
        handlePublish,
        addGlobalField,
        insertInlineField,
        updateActiveField
    };
}
