<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Eye, FileText, Plus, Search, CheckCircle, RotateCcw } from '@lucide/vue'
import { RouterLink } from 'vue-router'
import { useOrgStore } from '@/stores/org'
import { api } from '@/api/client'
import type {Document, DocumentStatus} from '@/types/document'

const orgStore = useOrgStore()

const documents = ref<Document[]>([])
const searchQuery       = ref('')
const selectedTemplate  = ref<string>('')
const selectedStatus    = ref<string>('')
const isLoading         = ref(true)

const actionLoading     = ref<Record<number, boolean>>({})

watch(
    () => orgStore.currentOrgId,
    async () => {
      if (!orgStore.currentOrgId) return
      isLoading.value = true
      try {
        documents.value = await api.organisations.documents.all(orgStore.currentOrgId)
      } finally {
        isLoading.value = false
      }
    },
    { immediate: true }
)

const templateName = (doc: Document): string => {
  return doc.templateName
}

const uniqueTemplates = computed(() =>
    [...new Set(documents.value.map(templateName))].filter(Boolean).sort()
)

const uniqueStatuses = computed(() =>
    [...new Set(documents.value.map((d) => d.status))].filter(Boolean).sort()
)

const filteredDocuments = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  return documents.value.filter((doc) => {
    const name = templateName(doc)
    const matchesSearch   = !q || doc.name.toLowerCase().includes(q)
    const matchesTemplate = !selectedTemplate.value || name === selectedTemplate.value
    const matchesStatus   = !selectedStatus.value || doc.status === selectedStatus.value
    return matchesSearch && matchesTemplate && matchesStatus
  })
})

const viewDocument = async (doc: Document) => {
  if (!orgStore.currentOrgId) return
  actionLoading.value[doc.id] = true
  try {
    const blob = await api.organisations.documents.view(orgStore.currentOrgId, doc.id)
    const url  = URL.createObjectURL(blob)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 15_000)
  } finally {
    actionLoading.value[doc.id] = false
  }
}

const finalise = async (doc: Document) => {
  if (!orgStore.currentOrgId) return
  actionLoading.value[doc.id] = true
  try {
    await api.organisations.documents.finalise(orgStore.currentOrgId, doc.id)
    const found = documents.value.find((d) => d.id === doc.id)
    if (found) found.status = 'FINAL' as DocumentStatus
  } finally {
    actionLoading.value[doc.id] = false
  }
}

const revertToDraft = async (doc: Document) => {
  if (!orgStore.currentOrgId) return
  actionLoading.value[doc.id] = true
  try {
    await api.organisations.documents.revertToDraft(orgStore.currentOrgId, doc.id)
    const found = documents.value.find((d) => d.id === doc.id)
    if (found) found.status = 'DRAFT'
  } finally {
    actionLoading.value[doc.id] = false
  }
}

const isFinal = (doc: Document) => doc.status === 'FINAL' as DocumentStatus

const formatDate = (date: Date | string): string => {
  const d = new Date(date)
  return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="p-4 lg:p-8">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 mb-1">Documents</h1>
        <p class="text-sm text-gray-600">Generated documents library</p>
      </div>
      <RouterLink
          to="/documents/generate"
          class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800
               flex items-center gap-2 justify-center"
      >
        <Plus :size="16" />
        Generate Document
      </RouterLink>
    </div>
    <div class="bg-white border border-gray-300 rounded-lg p-4 mb-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="flex-1 relative">
          <Search :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Search documents..."
              class="w-full pl-9 pr-4 py-2 border border-gray-300 rounded text-sm
                   focus:outline-none focus:ring-2 focus:ring-gray-300"
          />
        </div>
        <select
            v-model="selectedTemplate"
            class="px-4 py-2 border border-gray-300 rounded text-sm bg-white"
        >
          <option value="">All Templates</option>
          <option v-for="t in uniqueTemplates" :key="t" :value="t">{{ t }}</option>
        </select>
        <select
            v-model="selectedStatus"
            class="px-4 py-2 border border-gray-300 rounded text-sm bg-white"
        >
          <option value="">All Status</option>
          <option v-for="s in uniqueStatuses" :key="s" :value="s">{{ s }}</option>
        </select>
      </div>
    </div>
    <div v-if="isLoading" class="text-center py-12 text-sm text-gray-500">
      Loading documents…
    </div>
    <div
        v-else-if="filteredDocuments.length === 0"
        class="text-center py-12 bg-white border border-dashed border-gray-300 rounded-lg"
    >
      <p class="text-gray-500">No documents found matching your criteria.</p>
    </div>
    <div v-else class="hidden lg:block bg-white border border-gray-300 rounded-lg overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50 border-b border-gray-300">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Document</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Template</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Date</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Status</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Actions</th>
        </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
        <tr v-for="doc in filteredDocuments" :key="doc.id" class="hover:bg-gray-50">
          <td class="px-6 py-4">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 bg-gray-100 rounded flex items-center justify-center shrink-0">
                <FileText :size="16" class="text-gray-500" />
              </div>
              <span class="font-medium text-sm text-gray-900">{{ doc.name }}</span>
            </div>
          </td>
          <td class="px-6 py-4 text-sm text-gray-600">{{ templateName(doc) }}</td>
          <td class="px-6 py-4 text-sm text-gray-600">{{ formatDate(doc.createdAt) }}</td>
          <td class="px-6 py-4">
              <span
                  class="inline-flex px-2 py-1 text-xs rounded-full font-medium"
                  :class="isFinal(doc)
                  ? 'bg-green-100 text-green-800'
                  : 'bg-yellow-100 text-yellow-800'"
              >
                {{ doc.status }}
              </span>
          </td>
          <td class="px-6 py-4">
            <div class="flex items-center gap-1">
              <button
                  @click="viewDocument(doc)"
                  :disabled="!!actionLoading[doc.id]"
                  title="View PDF"
                  class="p-1.5 hover:bg-gray-100 rounded transition-colors disabled:opacity-40"
              >
                <Eye :size="15" class="text-gray-600" />
              </button>
              <button
                  v-if="!isFinal(doc)"
                  @click="finalise(doc)"
                  :disabled="!!actionLoading[doc.id]"
                  title="Finalise document"
                  class="p-1.5 hover:bg-green-50 rounded transition-colors disabled:opacity-40"
              >
                <CheckCircle :size="15" class="text-green-600" />
              </button>
              <button
                  v-if="isFinal(doc)"
                  @click="revertToDraft(doc)"
                  :disabled="!!actionLoading[doc.id]"
                  title="Revert to draft"
                  class="p-1.5 hover:bg-yellow-50 rounded transition-colors disabled:opacity-40"
              >
                <RotateCcw :size="15" class="text-yellow-600" />
              </button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <div class="lg:hidden space-y-3">
      <div
          v-for="doc in filteredDocuments"
          :key="doc.id"
          class="bg-white border border-gray-300 rounded-lg p-4"
      >
        <div class="flex items-start gap-3 mb-3">
          <div class="w-10 h-10 bg-gray-100 rounded flex items-center justify-center shrink-0">
            <FileText :size="18" class="text-gray-500" />
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="font-medium text-sm text-gray-900 mb-0.5 truncate">{{ doc.name }}</h3>
            <p class="text-xs text-gray-500">{{ templateName(doc) }}</p>
          </div>
          <span
              class="shrink-0 inline-flex px-2 py-1 text-xs rounded-full font-medium"
              :class="isFinal(doc)
              ? 'bg-green-100 text-green-800'
              : 'bg-yellow-100 text-yellow-800'"
          >
            {{ doc.status }}
          </span>
        </div>
        <p class="text-xs text-gray-400 mb-3">{{ formatDate(doc.createdAt) }}</p>
        <div class="flex gap-2 pt-3 border-t border-gray-100">
          <button
              @click="viewDocument(doc)"
              :disabled="!!actionLoading[doc.id]"
              class="flex-1 px-3 py-1.5 border border-gray-300 rounded text-xs
                   hover:bg-gray-50 flex items-center justify-center gap-1.5
                   disabled:opacity-40 transition-colors"
          >
            <Eye :size="13" />
            View
          </button>
          <button
              v-if="!isFinal(doc)"
              @click="finalise(doc)"
              :disabled="!!actionLoading[doc.id]"
              class="flex-1 px-3 py-1.5 border border-green-300 text-green-700 rounded text-xs
                   hover:bg-green-50 flex items-center justify-center gap-1.5
                   disabled:opacity-40 transition-colors"
          >
            <CheckCircle :size="13" />
            Finalise
          </button>
          <button
              v-if="isFinal(doc)"
              @click="revertToDraft(doc)"
              :disabled="!!actionLoading[doc.id]"
              class="flex-1 px-3 py-1.5 border border-yellow-300 text-yellow-700 rounded text-xs
                   hover:bg-yellow-50 flex items-center justify-center gap-1.5
                   disabled:opacity-40 transition-colors"
          >
            <RotateCcw :size="13" />
            Draft Back
          </button>
        </div>
      </div>
    </div>
  </div>
</template>