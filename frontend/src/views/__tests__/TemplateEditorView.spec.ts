import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { ref } from 'vue';
import TemplateEditorView from '../TemplateEditorView.vue';

vi.mock('vue-router', () => ({
    RouterLink: {
        template: '<a><slot /></a>'
    }
}));

const mockInit = vi.fn();
const mockHandleSave = vi.fn();
const mockHandlePublish = vi.fn();

vi.mock('@/composables/useTemplateEditor', () => ({
    useTemplateEditor: () => ({
        isNew: ref(false),
        isLoading: ref(false),
        templateName: ref('Wayne Employment Contract'),
        selectedFieldId: ref(null),
        hasChanges: ref(true),
        status: ref('DRAFT'),
        globalFields: ref([]),
        inlineFields: ref([]),
        editor: ref({ getHTML: () => '<h1>Hello</h1>' }),
        manifestJson: ref('{"fields":[]}'),
        activeField: ref(null),
        isInlineFieldActive: ref(false),
        init: mockInit,
        destroyEditor: vi.fn(),
        handleSave: mockHandleSave,
        handlePublish: mockHandlePublish,
        addGlobalField: vi.fn(),
        insertInlineField: vi.fn(),
        updateActiveField: vi.fn()
    })
}));

describe('TemplateEditorView.vue', () => {
    it('calls init composable method on mount', () => {
        mount(TemplateEditorView, {
            global: {
                stubs: {
                    EditorContent: true,
                    FieldSidebar: true,
                    FieldPropertiesPanel: true,
                    DocumentCanvas: true,
                    EditorToolbar: true
                }
            }
        });

        expect(mockInit).toHaveBeenCalled();
    });

    it('renders toolbar buttons and handles clicks', async () => {
        const wrapper = mount(TemplateEditorView, {
            global: {
                stubs: {
                    EditorContent: true,
                    FieldSidebar: true,
                    FieldPropertiesPanel: true,
                    DocumentCanvas: true,
                    EditorToolbar: true
                }
            }
        });

        const input = wrapper.find('input[placeholder="Template Name"]');
        expect((input.element as HTMLInputElement).value).toBe(
            'Wayne Employment Contract'
        );

        const saveBtn = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Save Draft'))[0];
        await saveBtn.trigger('click');
        expect(mockHandleSave).toHaveBeenCalled();

        const publishBtn = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Publish'))[0];
        await publishBtn.trigger('click');
        expect(mockHandlePublish).toHaveBeenCalled();
    });
});
