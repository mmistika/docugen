<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { EditorContent } from "@tiptap/vue-3";
import { ArrowLeft, Save, FileCheck2, ZoomIn, ZoomOut } from "@lucide/vue";
import hljs from "highlight.js/lib/core";
import hljsJson from "highlight.js/lib/languages/json";
import hljsXml from "highlight.js/lib/languages/xml";
import "highlight.js/styles/github.css";
import { useTemplateEditor } from "@/composables/useTemplateEditor";
import FieldSidebar         from "@/components/FieldSidebar.vue";
import FieldPropertiesPanel from "@/components/FieldPropertiesPanel.vue";

hljs.registerLanguage("json", hljsJson);
hljs.registerLanguage("xml",  hljsXml);


const {
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
} = useTemplateEditor();

onMounted(init);
onBeforeUnmount(destroyEditor);

type Tab = "document" | "manifest" | "html";
const activeTab = ref<Tab>("document");

const TABS: { key: Tab; label: string }[] = [
  { key: "document", label: "Document View"  },
  { key: "manifest", label: "Manifest View"  },
  { key: "html",     label: "HTML View"      },
];

const A4_W      = 794;
const A4_H      = 1123;
const MIN_SCALE = 0.3;
const MAX_SCALE = 2.0;
const scale     = ref(0.9);

const sheetStyle = computed(() => ({
  width:           `${A4_W}px`,
  height:          `${A4_H}px`,
  transform:       `scale(${scale.value})`,
  transformOrigin: "top left",
  position:        "absolute" as const,
}));

const wrapperStyle = computed(() => ({
  width:    `${A4_W * scale.value}px`,
  height:   `${A4_H * scale.value}px`,
  flexShrink: 0,
  position: "relative" as const,
}));

const documentAreaRef = ref<HTMLElement | null>(null);

const highlightedManifest = computed(() =>
    hljs.highlight(manifestJson.value, { language: "json" }).value
);

const highlightedHtml = computed(() =>
    hljs.highlight(editor.value?.getHTML() ?? "", { language: "xml" }).value
);
</script>

<template>
  <div v-if="isLoading" class="flex-1 flex items-center justify-center bg-gray-50 text-gray-500">
    Loading Editor...
  </div>

  <div v-else class="h-full flex flex-col bg-gray-50">
    <div class="bg-white border-b border-gray-300 px-4 py-3 shrink-0">
      <div class="flex items-center justify-between mb-3">
        <RouterLink
            to="/templates"
            class="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft :size="16" />
          Back to Templates
        </RouterLink>
        <div class="flex items-center gap-2">
          <button
              @click="handleSave"
              :disabled="!hasChanges"
              class="px-4 py-1.5 border border-gray-300 rounded text-sm hover:bg-gray-50
                   flex items-center gap-2 transition-colors disabled:opacity-50"
          >
            <Save :size="16" />
            <span class="hidden sm:inline">{{ hasChanges ? "Save Draft" : "Saved" }}</span>
          </button>
          <button
              v-if="!isNew"
              @click="handlePublish"
              :disabled="status === 'ACTIVE' && !hasChanges"
              class="px-4 py-1.5 bg-gray-900 text-white rounded text-sm hover:bg-gray-800
                   flex items-center gap-2 transition-colors disabled:opacity-50 disabled:bg-gray-700"
          >
            <FileCheck2 :size="16" />
            <span class="hidden sm:inline">
              {{ status === "ACTIVE" && !hasChanges ? "Active" : "Publish" }}
            </span>
          </button>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <input
            v-model="templateName"
            type="text"
            placeholder="Template Name"
            :disabled="!isNew"
            class="flex-1 text-xl font-bold border-none outline-none bg-transparent
                 focus:ring-0 p-0 disabled:text-gray-500"
        />
        <span
            v-if="!isNew"
            class="px-2 py-0.5 text-xs font-bold uppercase rounded tracking-wide border"
            :class="{
            'bg-yellow-50 text-yellow-700 border-yellow-200': status === 'DRAFT',
            'bg-green-50  text-green-700  border-green-200':  status === 'ACTIVE',
            'bg-red-50    text-red-700    border-red-200':    status === 'RETIRED',
          }"
        >
          {{ status }}
        </span>
      </div>
    </div>
    <div class="flex flex-1 overflow-hidden">
      <FieldSidebar
          :globalFields="globalFields"
          :inlineFields="inlineFields"
          :selectedFieldId="selectedFieldId"
          :fieldsExpanded="fieldsExpanded"
          :inlineFieldsExpanded="inlineFieldsExpanded"
          @add-global="addGlobalField"
          @insert-inline="insertInlineField"
          @select-field="selectedFieldId = $event"
          @update:fieldsExpanded="fieldsExpanded = $event"
          @update:inlineFieldsExpanded="inlineFieldsExpanded = $event"
      />
      <div class="flex-1 flex flex-col overflow-hidden bg-gray-100">
        <div class="bg-white border-b border-gray-300 px-4 flex items-center">
          <nav class="flex gap-6 flex-1">
            <button
                v-for="tab in TABS"
                :key="tab.key"
                @click="activeTab = tab.key"
                class="py-3 px-1 border-b-2 text-sm font-medium transition-colors"
                :class="
                activeTab === tab.key
                  ? 'border-gray-900 text-gray-900'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              "
            >
              {{ tab.label }}
            </button>
          </nav>
          <div class="flex items-center gap-2 py-2 ml-4 shrink-0">
            <button
                @click="scale = Math.max(MIN_SCALE, Math.round((scale - 0.1) * 100) / 100)"
                class="p-1 rounded hover:bg-gray-100 text-gray-500"
                title="Zoom out"
            ><ZoomOut :size="15" /></button>

            <input
                type="range"
                :min="MIN_SCALE"
                :max="MAX_SCALE"
                :step="0.05"
                v-model.number="scale"
                class="w-24 accent-gray-800"
            />

            <button
                @click="scale = Math.min(MAX_SCALE, Math.round((scale + 0.1) * 100) / 100)"
                class="p-1 rounded hover:bg-gray-100 text-gray-500"
                title="Zoom in"
            ><ZoomIn :size="15" /></button>

            <span class="text-xs text-gray-500 w-10 text-right tabular-nums">
              {{ Math.round(scale * 100) }}%
            </span>
          </div>
        </div>
        <div ref="documentAreaRef" class="flex-1 overflow-auto">
          <div
              v-show="activeTab === 'document'"
              class="flex justify-center py-8 px-4"
              :style="{ minHeight: `${A4_H * scale + 64}px` }"
          >
            <div :style="wrapperStyle">
              <div
                  :style="sheetStyle"
                  class="bg-white border border-gray-300 shadow-sm p-[20mm]"
              >
                <EditorContent :editor="editor" />
              </div>
            </div>
          </div>
          <div v-if="activeTab === 'manifest'" class="p-4 lg:p-8">
            <div class="max-w-4xl mx-auto bg-white border border-gray-300 rounded-lg shadow-sm overflow-hidden">
              <pre class="p-6 text-sm overflow-auto"><code
                  class="language-json hljs"
                  v-html="highlightedManifest"
              /></pre>
            </div>
          </div>

          <div v-if="activeTab === 'html'" class="p-4 lg:p-8">
            <div class="max-w-4xl mx-auto bg-white border border-gray-300 rounded-lg shadow-sm overflow-hidden">
              <pre class="p-6 text-sm overflow-auto"><code
                  class="language-xml hljs font-mono whitespace-pre-wrap"
                  v-html="highlightedHtml"
              /></pre>
            </div>
          </div>
        </div>
      </div>
      <FieldPropertiesPanel
          :activeField="activeField"
          :isInline="isInlineFieldActive"
          @update="updateActiveField"
      />
    </div>
  </div>
</template>

<style>
.ProseMirror { outline: none; }
.ProseMirror p { margin-top: 0.5em; margin-bottom: 0.5em; line-height: 1.5; }
pre code.hljs { padding: 0; background: transparent; }
</style>