<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useOrgStore } from '@/stores/org'
import { api } from '@/api/client'
import type { AuditLogDTO } from '@/types/audit'

const orgStore = useOrgStore()

const PAGE_SIZE = 10
const fromInput = ref('')
const toInput   = ref('')
const page      = ref(0)

const logs         = ref<AuditLogDTO[]>([])
const totalPages   = ref(0)
const totalElements = ref(0)
const isLoading    = ref(false)
const error        = ref<string | null>(null)

const selectedAction     = ref('')
const selectedEntityType = ref('')

const availableActions = computed(() =>
    [...new Set(logs.value.map((l) => l.action))].sort()
)
const availableEntityTypes = computed(() =>
    [...new Set(logs.value.map((l) => l.entityType))].sort()
)

const filteredLogs = computed(() =>
    logs.value.filter((l) => {
      const matchAction = !selectedAction.value || l.action === selectedAction.value
      const matchType   = !selectedEntityType.value || l.entityType === selectedEntityType.value
      return matchAction && matchType
    })
)

const fetch = async () => {
  if (!orgStore.currentOrgId) return
  isLoading.value = true
  error.value     = null
  selectedAction.value     = ''
  selectedEntityType.value = ''
  try {
    const res = await api.organisations.audit.get(orgStore.currentOrgId, {
      page: page.value,
      size: PAGE_SIZE,
      ...(fromInput.value ? { from: fromInput.value } : {}),
      ...(toInput.value   ? { to:   toInput.value   } : {}),
    })
    logs.value          = res.content
    totalPages.value    = res.page.totalPages
    totalElements.value = res.page.totalElements
  } catch {
    error.value = 'Failed to load audit logs.'
  } finally {
    isLoading.value = false
  }
}

watch(() => orgStore.currentOrgId, () => { page.value = 0; fetch() }, { immediate: true })

watch(page, fetch)

const applyFilters = () => { page.value = 0; fetch() }
const clearFilters = () => {
  fromInput.value = ''
  toInput.value   = ''
  page.value      = 0
  fetch()
}

const pageButtons = computed(() => {
  const total   = totalPages.value
  const current = page.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const start = Math.max(0, Math.min(current - 3, total - 7))
  return Array.from({ length: 7 }, (_, i) => start + i)
})

const formatTs = (ts: string) =>
    new Date(ts).toLocaleString('en-GB', {
      day:    '2-digit',
      month:  'short',
      year:   'numeric',
      hour:   '2-digit',
      minute: '2-digit',
    })

const formatMetadata = (raw: string): string => {
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
}

const BADGE = 'bg-gray-100 text-gray-700 border border-gray-200'
</script>

<template>
  <div class="p-4 lg:p-8">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900 mb-1">Audit Logs</h1>
      <p class="text-sm text-gray-600">
        View all actions performed within this organisation.
      </p>
    </div>
    <div class="bg-white border border-gray-300 rounded-lg p-4 mb-6">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">From</label>
          <input
              v-model="fromInput"
              type="datetime-local"
              class="w-full px-3 py-2 border border-gray-300 rounded text-sm focus:outline-none"
          />
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">To</label>
          <input
              v-model="toInput"
              type="datetime-local"
              class="w-full px-3 py-2 border border-gray-300 rounded text-sm focus:outline-none"
          />
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Action</label>
          <select
              v-model="selectedAction"
              class="w-full px-3 py-2 border border-gray-300 rounded text-sm bg-white focus:outline-none"
          >
            <option value="">All Actions</option>
            <option v-for="a in availableActions" :key="a" :value="a">{{ a }}</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">Entity Type</label>
          <select
              v-model="selectedEntityType"
              class="w-full px-3 py-2 border border-gray-300 rounded text-sm bg-white focus:outline-none"
          >
            <option value="">All Types</option>
            <option v-for="t in availableEntityTypes" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>
      </div>
      <div class="flex gap-2 mt-3">
        <button
            @click="applyFilters"
            class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 transition-colors"
        >
          Apply
        </button>
        <button
            @click="clearFilters"
            class="px-4 py-2 border border-gray-300 rounded text-sm hover:bg-gray-50 transition-colors"
        >
          Clear
        </button>
      </div>
    </div>
    <div v-if="isLoading" class="text-sm text-gray-400 text-center py-12">Loading…</div>
    <div v-else-if="error"  class="text-sm text-red-500  text-center py-12">{{ error }}</div>
    <template v-else>
      <div class="hidden lg:block bg-white border border-gray-300 rounded-lg overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-300">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Timestamp</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">User</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Action</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Entity</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Metadata</th>
          </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
          <tr v-if="filteredLogs.length === 0">
            <td colspan="5" class="px-6 py-10 text-center text-sm text-gray-400">No logs found.</td>
          </tr>
          <tr v-for="log in filteredLogs" :key="log.id" class="hover:bg-gray-50 align-top">
            <td class="px-6 py-4 text-sm text-gray-600 whitespace-nowrap">{{ formatTs(log.timestamp) }}</td>
            <td class="px-6 py-4">
              <div class="text-sm text-gray-900">{{ log.userName }}</div>
              <div class="text-xs text-gray-500">{{ log.userEmail }}</div>
            </td>
            <td class="px-6 py-4">
                <span :class="BADGE" class="inline-flex px-2 py-1 text-xs rounded font-medium">
                  {{ log.action }}
                </span>
            </td>
            <td class="px-6 py-4">
              <div class="text-sm text-gray-900">{{ log.entityType }}</div>
              <div class="text-xs text-gray-500">ID: {{ log.entityId }}</div>
            </td>
            <td class="px-6 py-4 max-w-xs">
              <pre class="text-xs text-gray-600 font-mono whitespace-pre-wrap break-all">{{ formatMetadata(log.metadata) }}</pre>
            </td>
          </tr>
          </tbody>
        </table>
        <div class="flex items-center justify-between px-6 py-4 border-t border-gray-200 bg-gray-50">
          <p class="text-xs text-gray-500">
            {{ totalElements }} total entr{{ totalElements !== 1 ? 'ies' : 'y' }}
          </p>
          <div class="flex items-center gap-1">
            <button
                @click="page = Math.max(0, page - 1)"
                :disabled="page === 0"
                class="px-3 py-1 border border-gray-300 rounded text-sm hover:bg-white
                     disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >Previous</button>
            <button
                v-for="p in pageButtons"
                :key="p"
                @click="page = p"
                class="px-3 py-1 border rounded text-sm transition-colors"
                :class="page === p
                ? 'bg-gray-900 text-white border-gray-900'
                : 'border-gray-300 hover:bg-white'"
            >{{ p + 1 }}</button>
            <button
                @click="page = Math.min(totalPages - 1, page + 1)"
                :disabled="page >= totalPages - 1"
                class="px-3 py-1 border border-gray-300 rounded text-sm hover:bg-white
                     disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >Next</button>
          </div>
        </div>
      </div>
      <div class="lg:hidden space-y-3">
        <div v-if="filteredLogs.length === 0" class="text-center py-10 text-sm text-gray-400">
          No logs found.
        </div>
        <div
            v-for="log in filteredLogs"
            :key="log.id"
            class="bg-white border border-gray-300 rounded-lg p-4"
        >
          <div class="flex items-start justify-between mb-3">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1.5 flex-wrap">
                <div>
                  <div class="text-sm text-gray-900">{{ log.userName }}</div>
                  <div class="text-xs text-gray-500">{{ log.userEmail }}</div>
                </div>
                <span :class="BADGE" class="inline-flex px-2 py-1 text-xs rounded font-medium">
                  {{ log.action }}
                </span>
                <span class="text-xs text-gray-600 font-medium">{{ log.entityType }}
                  <span class="text-xs text-gray-400"> ID: {{ log.entityId }}</span>
                </span>
              </div>
            </div>
            <div class="text-xs text-gray-400 whitespace-nowrap ml-3 shrink-0">
              {{ formatTs(log.timestamp) }}
            </div>
          </div>
          <div class="pt-3 border-t border-gray-100">
            <pre class="text-xs text-gray-600 font-mono whitespace-pre-wrap break-all">{{ formatMetadata(log.metadata) }}</pre>
          </div>
        </div>
        <div class="flex items-center justify-between pt-2">
          <p class="text-xs text-gray-500">Page {{ page + 1 }} of {{ totalPages }}</p>
          <div class="flex gap-2">
            <button
                @click="page = Math.max(0, page - 1)"
                :disabled="page === 0"
                class="px-3 py-1.5 border border-gray-300 rounded text-xs hover:bg-white
                     disabled:opacity-40 disabled:cursor-not-allowed"
            >Previous</button>
            <button
                @click="page = Math.min(totalPages - 1, page + 1)"
                :disabled="page >= totalPages - 1"
                class="px-3 py-1.5 border border-gray-300 rounded text-xs hover:bg-white
                     disabled:opacity-40 disabled:cursor-not-allowed"
            >Next</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>