<script setup lang="ts">
import { onMounted, onBeforeUnmount } from "vue";
import { EditorContent } from "@tiptap/vue-3";
import { ArrowLeft, Save, FileCheck2 } from "@lucide/vue";
import { useTemplateEditor } from "@/composables/useTemplateEditor";
import FieldSidebar         from "@/components/FieldSidebar.vue";
import FieldPropertiesPanel from "@/components/FieldPropertiesPanel.vue";

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
import { ref } from "vue";
const activeTab = ref<Tab>("document");

const TABS: { key: Tab; label: string }[] = [
  { key: "document", label: "Document View"  },
  { key: "manifest", label: "Manifest View"  },
  { key: "html",     label: "HTML View"      },
];
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
        <div class="bg-white border-b border-gray-300 px-4">
          <nav class="flex gap-6">
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
        </div>
        <div class="flex-1 overflow-auto p-4 lg:p-8">
          <div v-show="activeTab === 'document'" class="flex justify-center">
            <div class="bg-white border border-gray-300 shadow-sm w-[210mm] min-h-[297mm] p-[20mm]">
              <EditorContent :editor="editor" />
            </div>
          </div>
          <div
              v-if="activeTab === 'manifest'"
              class="max-w-4xl mx-auto bg-white border border-gray-300 rounded-lg shadow-sm p-6"
          >
            <pre class="text-sm font-mono text-gray-700 whitespace-pre-wrap">{{ manifestJson }}</pre>
          </div>

          <div
              v-if="activeTab === 'html'"
              class="max-w-4xl mx-auto bg-white border border-gray-300 rounded-lg shadow-sm p-6"
          >
            <pre class="text-sm font-mono text-gray-700 whitespace-pre-wrap">{{
                editor ? editor.getHTML() : ""
              }}</pre>
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
</style>