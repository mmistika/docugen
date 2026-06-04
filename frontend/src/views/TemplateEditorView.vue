<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { EditorContent } from '@tiptap/vue-3';
import { ArrowLeft, FileCheck2, Save } from '@lucide/vue';
import hljs from 'highlight.js/lib/core';
import hljsJson from 'highlight.js/lib/languages/json';
import hljsXml from 'highlight.js/lib/languages/xml';
import 'highlight.js/styles/github.css';
import { useTemplateEditor } from '@/composables/useTemplateEditor';
import FieldSidebar from '@/components/editor/FieldSidebar.vue';
import FieldPropertiesPanel from '@/components/editor/FieldPropertiesPanel.vue';
import DocumentCanvas from '@/components/common/DocumentCanvas.vue';
import EditorToolbar from '@/components/editor/EditorToolbar.vue';

hljs.registerLanguage('json', hljsJson);
hljs.registerLanguage('xml', hljsXml);

const {
    isNew,
    isLoading,
    templateName,
    selectedFieldId,
    fieldsExpanded,
    inlineFieldsExpanded,
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
} = useTemplateEditor();

onMounted(init);
onBeforeUnmount(destroyEditor);

type Tab = 'document' | 'manifest' | 'html';
const activeTab = ref<Tab>('document');

const TABS: { key: Tab; label: string }[] = [
    { key: 'document', label: 'Document View' },
    { key: 'manifest', label: 'Manifest View' },
    { key: 'html', label: 'HTML View' }
];

const documentAreaRef = ref<HTMLElement | null>(null);

const highlightedManifest = computed(
    () => hljs.highlight(manifestJson.value, { language: 'json' }).value
);

const highlightedHtml = computed(
    () =>
        hljs.highlight(editor.value?.getHTML() ?? '', { language: 'xml' }).value
);
</script>

<template>
    <div
        v-if="isLoading"
        class="flex-1 flex items-center justify-center bg-gray-50 text-gray-500"
    >
        Loading Editor...
    </div>

    <div v-else class="h-full flex flex-col bg-gray-50">
        <div class="bg-white border-b border-gray-300 px-4 py-3 shrink-0">
            <div class="flex items-center justify-between mb-3">
                <RouterLink
                    class="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900"
                    to="/templates"
                >
                    <ArrowLeft :size="16" />
                    Back to Templates
                </RouterLink>
                <div class="flex items-center gap-2">
                    <button
                        :disabled="!hasChanges"
                        class="px-4 py-1.5 border border-gray-300 rounded text-sm hover:bg-gray-50 flex items-center gap-2 transition-colors disabled:opacity-50"
                        @click="handleSave"
                    >
                        <Save :size="16" />
                        <span class="hidden sm:inline">{{
                            hasChanges ? 'Save Draft' : 'Saved'
                        }}</span>
                    </button>
                    <button
                        v-if="!isNew"
                        :disabled="status === 'ACTIVE' && !hasChanges"
                        class="px-4 py-1.5 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 transition-colors disabled:opacity-50 disabled:bg-gray-700"
                        @click="handlePublish"
                    >
                        <FileCheck2 :size="16" />
                        <span class="hidden sm:inline">
                            {{
                                status === 'ACTIVE' && !hasChanges
                                    ? 'Active'
                                    : 'Publish'
                            }}
                        </span>
                    </button>
                </div>
            </div>
            <div class="flex items-center gap-3">
                <input
                    v-model="templateName"
                    :disabled="!isNew"
                    class="flex-1 text-xl font-bold border-none outline-none bg-transparent focus:ring-0 p-0 disabled:text-gray-500"
                    placeholder="Template Name"
                    type="text"
                />
                <span
                    v-if="!isNew"
                    :class="{
                        'bg-yellow-50 text-yellow-700 border-yellow-200':
                            status === 'DRAFT',
                        'bg-green-50  text-green-700  border-green-200':
                            status === 'ACTIVE',
                        'bg-red-50    text-red-700    border-red-200':
                            status === 'RETIRED'
                    }"
                    class="px-2 py-0.5 text-xs font-bold uppercase rounded tracking-wide border"
                >
                    {{ status }}
                </span>
            </div>
        </div>
        <div class="flex flex-1 overflow-hidden">
            <FieldSidebar
                :fields-expanded="fieldsExpanded"
                :global-fields="globalFields"
                :inline-fields="inlineFields"
                :inline-fields-expanded="inlineFieldsExpanded"
                :selected-field-id="selectedFieldId"
                @add-global="addGlobalField"
                @insert-inline="insertInlineField"
                @select-field="selectedFieldId = $event"
                @update:fields-expanded="fieldsExpanded = $event"
                @update:inline-fields-expanded="inlineFieldsExpanded = $event"
            />
            <div class="flex-1 flex flex-col overflow-hidden bg-gray-100">
                <div
                    class="bg-white border-b border-gray-300 px-4 flex items-center"
                >
                    <nav class="flex gap-6 flex-1">
                        <button
                            v-for="tab in TABS"
                            :key="tab.key"
                            :class="
                                activeTab === tab.key
                                    ? 'border-gray-900 text-gray-900'
                                    : 'border-transparent text-gray-600 hover:text-gray-900'
                            "
                            class="py-3 px-1 border-b-2 text-sm font-medium transition-colors"
                            @click="activeTab = tab.key"
                        >
                            {{ tab.label }}
                        </button>
                    </nav>
                </div>
                <EditorToolbar
                    v-if="activeTab === 'document' && editor"
                    :editor="editor"
                />
                <div ref="documentAreaRef" class="flex-1 overflow-hidden">
                    <DocumentCanvas v-show="activeTab === 'document'">
                        <EditorContent :editor="editor" />
                    </DocumentCanvas>
                    <DocumentCanvas v-show="activeTab === 'manifest'">
                        <pre class="p-6 text-sm font-mono text-wrap"><code
                            class="language-json hljs"
                            v-html="highlightedManifest"
                        /></pre>
                    </DocumentCanvas>
                    <DocumentCanvas v-show="activeTab === 'html'">
                        <pre class="p-6 text-sm font-mono text-wrap"><code
                            class="language-xml hljs font-mono"
                            v-html="highlightedHtml"
                        /></pre>
                    </DocumentCanvas>
                </div>
            </div>
            <FieldPropertiesPanel
                :active-field="activeField"
                :is-inline="isInlineFieldActive"
                @update="updateActiveField"
            />
        </div>
    </div>
</template>

<style>
.ProseMirror {
    outline: none;
}
pre code.hljs {
    padding: 0;
    background: transparent;
}
</style>
