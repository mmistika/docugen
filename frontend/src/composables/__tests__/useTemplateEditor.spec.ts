import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ref } from 'vue';
import { createPinia, setActivePinia } from 'pinia';
import { useTemplateEditor } from '../useTemplateEditor';
import { useOrgStore } from '@/stores/org';
import { api } from '@/api/client';

const routeParams = ref<Record<string, string>>({ id: 'new' });
const routerReplaceMock = vi.fn();

// Mock vue-router
vi.mock('vue-router', () => ({
    useRoute: () => ({
        params: routeParams.value
    }),
    useRouter: () => ({
        replace: routerReplaceMock
    })
}));

// Define in global to bypass Vitest hoisting
(globalThis as any).mockEditorCommands = {
    setContent: vi.fn(),
    focus: vi.fn().mockReturnThis(),
    insertContent: vi.fn().mockReturnThis(),
    run: vi.fn()
};
(globalThis as any).mockTr = {
    setNodeMarkup: vi.fn().mockReturnThis()
};
(globalThis as any).mockEditor = {
    commands: (globalThis as any).mockEditorCommands,
    destroy: vi.fn(),
    getHTML: vi.fn(() => '<p>Mock Content</p>'),
    state: {
        doc: {
            descendants: vi.fn()
        },
        tr: (globalThis as any).mockTr
    },
    view: {
        dispatch: vi.fn()
    },
    chain: vi.fn(() => ({
        focus: vi.fn(() => ({
            insertContent: vi.fn().mockReturnThis(),
            run: vi.fn()
        }))
    }))
};

vi.mock('@tiptap/vue-3', () => ({
    EditorContent: {
        name: 'EditorContent',
        template: '<div><slot /></div>'
    },
    useEditor: () => ({
        __v_isRef: true,
        get value() {
            return (globalThis as any).mockEditor;
        },
        set value(v) {
            (globalThis as any).mockEditor = v;
        }
    })
}));

const mockEditor = (globalThis as any).mockEditor;
const mockEditorCommands = (globalThis as any).mockEditorCommands;
const mockTr = (globalThis as any).mockTr;

// Mock the API client
vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            templates: {
                getForEdit: vi.fn(),
                create: vi.fn(),
                update: vi.fn(),
                publish: vi.fn()
            }
        }
    }
}));

describe('useTemplateEditor Composable', () => {
    let orgStore: any;

    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);

        orgStore = useOrgStore();
        orgStore.currentOrgId = 1;
        orgStore.fetch = vi.fn().mockResolvedValue(undefined);

        routeParams.value = { id: 'new' };
        routerReplaceMock.mockClear();
        vi.clearAllMocks();
    });

    it('should initialize with "new" state', () => {
        routeParams.value = { id: 'new' };
        const editorComposable = useTemplateEditor();

        expect(editorComposable.isNew.value).toBe(true);
        expect(editorComposable.templateName.value).toBe('New Template');
        expect(editorComposable.globalFields.value).toEqual([]);
        expect(editorComposable.inlineFields.value).toEqual([]);
    });

    it('should initialize with an existing template ID', () => {
        routeParams.value = { id: '42' };
        const editorComposable = useTemplateEditor();

        expect(editorComposable.isNew.value).toBe(false);
        expect(editorComposable.templateName.value).toBe('');
    });

    it('should load template details on init for existing templates', async () => {
        routeParams.value = { id: '42' };
        const mockTemplate = {
            id: 42,
            name: 'Employment Contract',
            status: 'DRAFT',
            content: '<h1>Contract</h1>',
            manifest: JSON.stringify({
                fields: [
                    {
                        id: 'g_1',
                        name: 'salary',
                        type: 'number',
                        required: true
                    }
                ],
                inline_fields: [
                    {
                        id: 'i_1',
                        name: 'employee_name',
                        type: 'text',
                        required: true
                    }
                ]
            })
        };
        vi.mocked(api.organisations.templates.getForEdit).mockResolvedValue(
            mockTemplate as any
        );

        const editorComposable = useTemplateEditor();
        await editorComposable.init();

        expect(orgStore.fetch).not.toHaveBeenCalled();
        expect(api.organisations.templates.getForEdit).toHaveBeenCalledWith(
            1,
            42
        );
        expect(editorComposable.templateName.value).toBe('Employment Contract');
        expect(editorComposable.status.value).toBe('DRAFT');
        expect(editorComposable.globalFields.value).toHaveLength(1);
        expect(editorComposable.globalFields.value[0]?.name).toBe('salary');
        expect(editorComposable.inlineFields.value).toHaveLength(1);
        expect(editorComposable.inlineFields.value[0]?.name).toBe(
            'employee_name'
        );
        expect(mockEditorCommands.setContent).toHaveBeenCalledWith(
            '<h1>Contract</h1>'
        );
    });

    it('should fetch orgStore if currentOrgId is missing on init', async () => {
        orgStore.currentOrgId = null;
        routeParams.value = { id: 'new' };

        const editorComposable = useTemplateEditor();
        await editorComposable.init();

        expect(orgStore.fetch).toHaveBeenCalled();
    });

    it('should generate template manifest JSON', async () => {
        routeParams.value = { id: 'new' };
        const editorComposable = useTemplateEditor();
        editorComposable.templateName.value = 'Test Manifest';
        editorComposable.globalFields.value = [
            { id: 'g_1', name: 'field_1', type: 'text', required: true }
        ];

        const manifest = JSON.parse(editorComposable.manifestJson.value);
        expect(manifest.template.name).toBe('Test Manifest');
        expect(manifest.fields).toHaveLength(1);
        expect(manifest.fields[0].name).toBe('field_1');
    });

    it('should save template draft and navigate if it is new', async () => {
        routeParams.value = { id: 'new' };
        const editorComposable = useTemplateEditor();
        editorComposable.templateName.value = 'Saved Template';

        vi.mocked(api.organisations.templates.create).mockResolvedValue(101);

        await editorComposable.handleSave();

        expect(api.organisations.templates.create).toHaveBeenCalledWith(1, {
            name: 'Saved Template',
            manifest: expect.any(String),
            content: '<p>Mock Content</p>'
        });
        expect(routerReplaceMock).toHaveBeenCalledWith('/templates/101');
    });

    it('should update template draft if it is existing', async () => {
        routeParams.value = { id: '42' };
        const editorComposable = useTemplateEditor();
        editorComposable.templateName.value = 'Updated Template';

        vi.mocked(api.organisations.templates.update).mockResolvedValue(42);

        await editorComposable.handleSave();

        expect(api.organisations.templates.update).toHaveBeenCalledWith(1, 42, {
            name: 'Updated Template',
            manifest: expect.any(String),
            content: '<p>Mock Content</p>'
        });
        expect(editorComposable.status.value).toBe('DRAFT');
    });

    it('should save draft and publish the template on publish', async () => {
        routeParams.value = { id: '42' };
        const editorComposable = useTemplateEditor();
        editorComposable.hasChanges.value = true;

        vi.mocked(api.organisations.templates.update).mockResolvedValue(42);
        vi.mocked(api.organisations.templates.publish).mockResolvedValue();

        await editorComposable.handlePublish();

        expect(api.organisations.templates.update).toHaveBeenCalled();
        expect(api.organisations.templates.publish).toHaveBeenCalledWith(1, 42);
        expect(editorComposable.status.value).toBe('ACTIVE');
    });

    it('should support adding global fields', () => {
        routeParams.value = { id: 'new' };
        const editorComposable = useTemplateEditor();
        expect(editorComposable.globalFields.value).toHaveLength(0);

        editorComposable.addGlobalField('number');

        expect(editorComposable.globalFields.value).toHaveLength(1);
        expect(editorComposable.globalFields.value[0]?.type).toBe('number');
        expect(editorComposable.selectedFieldId.value).toBe(
            editorComposable.globalFields.value[0]?.id
        );
    });

    it('should support inserting inline fields', () => {
        routeParams.value = { id: 'new' };
        const editorComposable = useTemplateEditor();
        expect(editorComposable.inlineFields.value).toHaveLength(0);

        editorComposable.insertInlineField('text');

        expect(editorComposable.inlineFields.value).toHaveLength(1);
        expect(editorComposable.inlineFields.value[0]?.type).toBe('text');
        expect(editorComposable.selectedFieldId.value).toBe(
            editorComposable.inlineFields.value[0]?.id
        );
        expect(mockEditor.chain).toHaveBeenCalled();
    });

    it('should update active field properties and dispatch changes to the editor if inline', async () => {
        routeParams.value = { id: 'new' };
        const editorComposable = useTemplateEditor();

        editorComposable.insertInlineField('text');
        const fieldId = editorComposable.selectedFieldId.value!;

        // Mock descendants callback to simulate finding the node in editor
        mockEditor.state.doc.descendants.mockImplementation((callback: any) => {
            callback(
                {
                    type: { name: 'inlineField' },
                    attrs: { id: fieldId, name: 'inline_text' }
                },
                10
            );
        });

        editorComposable.updateActiveField('name', 'employee_full_name');

        expect(editorComposable.activeField.value?.name).toBe(
            'employee_full_name'
        );
        expect(mockTr.setNodeMarkup).toHaveBeenCalledWith(
            10,
            null,
            expect.objectContaining({
                id: fieldId,
                name: 'employee_full_name'
            })
        );
        expect(mockEditor.view.dispatch).toHaveBeenCalled();
    });

    it('should destroy editor when destroyEditor is called', () => {
        routeParams.value = { id: 'new' };
        const editorComposable = useTemplateEditor();
        editorComposable.destroyEditor();
        expect(mockEditor.destroy).toHaveBeenCalled();
    });
});
