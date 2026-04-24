<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, FileText, Eye, Wand2, Loader2, ZoomIn, ZoomOut } from '@lucide/vue'
import { api } from '@/api/client'
import { useOrgStore } from '@/stores/org'
import { buildInteractiveContent } from '@/utils/templateParser'
import type { Template, TemplateDetail } from '@/types/template'
import type { Field } from '@/types/field'


const router   = useRouter()
const orgStore = useOrgStore()

const templates  = ref<Template[]>([])
const selectedId = ref<number | null>(null)
const detail     = ref<TemplateDetail | null>(null)
const isLoadingDetail = ref(false)
const isGenerating    = ref(false)
const submitAttempted = ref(false)

const documentName = ref('')
const fieldValues  = ref<Record<string, string>>({})

watch(() => orgStore.currentOrgId, async (newId) => {
  if (newId) {
    try {
      templates.value = await api.organisations.templates.all(newId)
    } catch (error) {
    }
  }
}, { immediate: true });

const selectTemplate = async (template: Template) => {
  if (selectedId.value === template.id) return
  selectedId.value = template.id
  detail.value     = null
  documentName.value   = ''
  fieldValues.value    = {}
  submitAttempted.value = false

  isLoadingDetail.value = true
  try {
    detail.value = await api.organisations.templates.getForEdit(
        orgStore.currentOrgId!,
        template.id
    )
  } finally {
    isLoadingDetail.value = false
  }
}

const isDraft = computed(() => detail.value?.status === 'DRAFT')

const globalFields = computed<Field[]>(() => {
  if (!detail.value?.manifest) return []
  try { return JSON.parse(detail.value.manifest).fields ?? [] }
  catch { return [] }
})

const validationErrors = computed<Record<string, string>>(() => {
  const errors: Record<string, string> = {}

  if (!documentName.value.trim()) {
    errors['documentName'] = 'Document name is required.'
  }

  for (const field of globalFields.value) {
    const raw = fieldValues.value[field.name] ?? ''
    const value = raw.trim()

    if (field.required && !value) {
      errors[field.name] = 'This field is required.'
      continue
    }
    if (!value) continue

    if (field.type === 'text') {
      if (field.minLength != null && value.length < field.minLength)
        errors[field.name] = `Minimum ${field.minLength} characters.`
      else if (field.maxLength != null && value.length > field.maxLength)
        errors[field.name] = `Maximum ${field.maxLength} characters.`
    }

    if (field.type === 'number') {
      const num = Number(value)
      if (isNaN(num)) {
        errors[field.name] = 'Must be a number.'
      } else {
        if (field.minValue != null && num < field.minValue)
          errors[field.name] = `Minimum value is ${field.minValue}.`
        else if (field.maxValue != null && num > field.maxValue)
          errors[field.name] = `Maximum value is ${field.maxValue}.`
      }
    }
  }

  try {
    const inlineFields: Field[] = JSON.parse(detail.value?.manifest ?? '{}').inline_fields ?? []
    for (const field of inlineFields) {
      if (field.required && !(fieldValues.value[field.name] ?? '').trim()) {
        errors[`inline_${field.name}`] = `Inline field "${field.name}" is required.`
      }
    }
  } catch {
  }
  return errors
})

const hasErrors    = computed(() => Object.keys(validationErrors.value).length > 0)
const errorCount   = computed(() => Object.keys(validationErrors.value).length)
const canGenerate  = computed(() => !!selectedId.value && !isGenerating.value)

const fieldError = (key: string) =>
    submitAttempted.value ? (validationErrors.value[key] ?? null) : null

const A4_W      = 794
const A4_H      = 1123
const MIN_SCALE = 0.3
const MAX_SCALE = 2.0
const scale     = ref(0.85)

const sheetStyle = computed(() => ({
  width:           `${A4_W}px`,
  height:          `${A4_H}px`,
  transform:       `scale(${scale.value})`,
  transformOrigin: 'top left',
  position:        'absolute' as const,
}))

const wrapperStyle = computed(() => ({
  width:      `${A4_W * scale.value}px`,
  height:     `${A4_H * scale.value}px`,
  flexShrink: 0,
  position:   'relative' as const,
}))

const previewAreaRef = ref<HTMLElement | null>(null)

const renderedContent = computed(() =>
    detail.value?.content ? buildInteractiveContent(detail.value.content) : ''
)

const onPreviewInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  const name   = target.getAttribute('data-field-name')
  if (name) fieldValues.value[name] = target.value
}

const generate = async () => {
  submitAttempted.value = true
  if (!selectedId.value || !orgStore.currentOrgId) return
  if (hasErrors.value) return

  isGenerating.value = true
  try {
    const blob = await api.organisations.documents.generate(
        orgStore.currentOrgId,
        {
          templateId: selectedId.value,
          name:       documentName.value,
          data:       { ...fieldValues.value },
        }
    )

    if (isDraft.value) {
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
      setTimeout(() => URL.revokeObjectURL(url), 15_000)
    } else {
      await router.push('/documents')
    }
  } finally {
    isGenerating.value = false
  }
}

const inputType = (field: Field) => field.type === 'number' ? 'number' : 'text'

watch(
    [submitAttempted, validationErrors],
    async () => {
      if (!previewAreaRef.value) return
      await nextTick()
      previewAreaRef.value
          .querySelectorAll<HTMLInputElement>('[data-field-name]')
          .forEach((input) => {
            const name     = input.getAttribute('data-field-name')!
            const hasError = submitAttempted.value && !!validationErrors.value[`inline_${name}`]
            input.classList.toggle('inline-doc-input--error', hasError)
          })
    },
    { deep: true }
)
</script>

<template>
  <div class="p-4 lg:p-8 max-w-4xl mx-auto">
    <RouterLink
        to="/documents"
        class="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
    >
      <ArrowLeft :size="16" />
      Back to Documents
    </RouterLink>
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-gray-900 mb-1">Generate Document</h1>
      <p class="text-sm text-gray-600">Select a template and fill in the details</p>
    </div>
    <div class="bg-white border border-gray-300 rounded-lg p-6 space-y-6">
      <div>
        <div class="flex items-center gap-2 mb-4">
          <div class="w-6 h-6 bg-gray-900 text-white rounded-full flex items-center justify-center text-xs font-medium">1</div>
          <h2 class="font-semibold text-gray-900">Select Template</h2>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <button
              v-for="template in templates"
              :key="template.id"
              @click="selectTemplate(template)"
              class="p-4 border-2 rounded-lg text-left transition-colors"
              :class="selectedId === template.id
              ? 'border-gray-900 bg-gray-50'
              : 'border-gray-200 hover:border-gray-400'"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-gray-100 rounded flex items-center justify-center shrink-0">
                <FileText :size="18" class="text-gray-500" />
              </div>
              <div class="font-medium text-sm text-gray-900 truncate">{{ template.name }}</div>
            </div>
          </button>
        </div>
      </div>

      <div class="border-t border-gray-200" />
      <div>
        <div class="flex items-center gap-2 mb-4">
          <div
              class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium transition-colors"
              :class="selectedId ? 'bg-gray-900 text-white' : 'bg-gray-200 text-gray-500'"
          >2</div>
          <h2 class="font-semibold transition-colors" :class="selectedId ? 'text-gray-900' : 'text-gray-400'">
            Document Settings
          </h2>
        </div>
        <div v-if="isLoadingDetail" class="flex items-center gap-2 text-sm text-gray-500 py-6">
          <Loader2 :size="16" class="animate-spin" /> Loading template…
        </div>
        <div v-else-if="detail" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">
              Document Name <span class="text-red-500">*</span>
            </label>
            <input
                v-model="documentName"
                type="text"
                placeholder="e.g. Invoice #INV-2026-003"
                class="w-full px-3 py-2 border rounded text-sm transition-colors focus:outline-none"
                :class="fieldError('documentName') ? 'border-red-400 bg-red-50' : 'border-gray-300'"
            />
            <p v-if="fieldError('documentName')" class="mt-1 text-xs text-red-500">
              {{ fieldError('documentName') }}
            </p>
          </div>
          <div v-for="field in globalFields" :key="field.id">
            <label class="block text-xs font-medium text-gray-600 mb-1">
              {{ field.name }}<span v-if="field.required" class="text-red-500 ml-0.5">*</span>
            </label>
            <input
                v-model="fieldValues[field.name]"
                :type="inputType(field)"
                :placeholder="field.name"
                :required="field.required"
                :maxlength="field.maxLength ?? undefined"
                :min="field.minValue ?? undefined"
                :max="field.maxValue ?? undefined"
                class="w-full px-3 py-2 border rounded text-sm transition-colors focus:outline-none"
                :class="fieldError(field.name) ? 'border-red-400 bg-red-50' : 'border-gray-300'"
            />
            <p v-if="fieldError(field.name)" class="mt-1 text-xs text-red-500">
              {{ fieldError(field.name) }}
            </p>
          </div>
        </div>
        <div v-else class="text-sm text-gray-400 text-center py-8">
          Please select a template to continue
        </div>
      </div>
      <div class="border-t border-gray-200" />
      <div>
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <div
                class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium transition-colors"
                :class="detail ? 'bg-gray-900 text-white' : 'bg-gray-200 text-gray-500'"
            >3</div>
            <h2 class="font-semibold transition-colors" :class="detail ? 'text-gray-900' : 'text-gray-400'">
              Fill Document Details
            </h2>
          </div>
          <div v-if="detail" class="flex items-center gap-2 shrink-0">
            <button
                @click="scale = Math.max(MIN_SCALE, Math.round((scale - 0.1) * 100) / 100)"
                class="p-1 rounded hover:bg-gray-100 text-gray-500"
            ><ZoomOut :size="14" /></button>
            <input
                type="range"
                :min="MIN_SCALE" :max="MAX_SCALE" :step="0.05"
                v-model.number="scale"
                class="w-24 accent-gray-800"
            />
            <button
                @click="scale = Math.min(MAX_SCALE, Math.round((scale + 0.1) * 100) / 100)"
                class="p-1 rounded hover:bg-gray-100 text-gray-500"
            ><ZoomIn :size="14" /></button>
            <span class="text-xs text-gray-400 w-9 text-right tabular-nums">
              {{ Math.round(scale * 100) }}%
            </span>
          </div>
        </div>
        <div
            v-if="submitAttempted && hasErrors"
            class="mb-4 px-4 py-3 bg-red-50 border border-red-200 rounded text-sm text-red-700 flex items-center gap-2"
        >
          <span class="font-medium">{{ errorCount }} field{{ errorCount !== 1 ? 's' : '' }} need attention before generating.</span>
        </div>
        <div
            v-if="detail"
            ref="previewAreaRef"
            class="bg-gray-100 border border-gray-200 rounded-lg overflow-auto"
            style="max-height: 70vh;"
        >
          <div class="flex justify-center py-8 px-4" :style="{ minHeight: `${A4_H * scale + 64}px` }">
            <div :style="wrapperStyle">
              <div
                  :style="sheetStyle"
                  class="bg-white border border-gray-300 shadow-sm prose max-w-none"
                  style="padding: 28mm 20mm;"
                  v-html="renderedContent"
                  @input="onPreviewInput"
              />
            </div>
          </div>
        </div>
        <div v-else class="text-sm text-gray-400 text-center py-8">
          Complete previous steps to preview document
        </div>
      </div>
      <div class="flex flex-col-reverse sm:flex-row items-center gap-3 pt-2">
        <RouterLink
            to="/documents"
            class="w-full sm:w-auto px-6 py-2 border border-gray-300 rounded text-sm
                 text-center hover:bg-gray-50 transition-colors"
        >
          Cancel
        </RouterLink>
        <template v-if="isDraft">
          <span class="px-2.5 py-1 text-xs font-bold uppercase tracking-wide rounded
                       bg-yellow-100 text-yellow-700 border border-yellow-200">
            Draft Test Generation
          </span>
          <button
              :disabled="!canGenerate"
              @click="generate"
              class="w-full sm:w-auto ml-auto px-6 py-2 border border-gray-300 rounded text-sm
                   hover:bg-gray-50 transition-colors flex items-center justify-center gap-2
                   disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <Loader2 v-if="isGenerating" :size="15" class="animate-spin" />
            <Eye v-else :size="15" />
            Preview Draft
          </button>
        </template>
        <button
            v-else
            :disabled="!canGenerate"
            @click="generate"
            class="w-full sm:w-auto ml-auto px-6 py-2 bg-gray-900 text-white rounded text-sm
                 hover:bg-gray-800 transition-colors flex items-center justify-center gap-2
                 disabled:opacity-40 disabled:cursor-not-allowed disabled:bg-gray-700"
        >
          <Loader2 v-if="isGenerating" :size="15" class="animate-spin" />
          <Wand2 v-else :size="15" />
          Generate
        </button>
      </div>
    </div>
  </div>
</template>

<style>
.inline-doc-input {
  display: inline;
  border: none;
  border-bottom: 1.5px dashed #93c5fd;
  background: transparent;
  font: inherit;
  color: #1e3a8a;
  padding: 0 1px;
  field-sizing: content;
  min-width: 2ch;
  outline: none;
  transition: border-color 0.15s;
  vertical-align: baseline;
}
.inline-doc-input:focus {
  border-bottom-color: #2563eb;
  border-bottom-style: solid;
}
.inline-doc-input::placeholder {
  color: #93c5fd;
  font-style: italic;
}
.inline-doc-input--error {
  border-bottom-color: #f87171;
  border-bottom-style: solid;
}
.inline-doc-input--error::placeholder {
  color: #fca5a5;
}
</style>