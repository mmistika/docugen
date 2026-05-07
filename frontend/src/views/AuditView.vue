<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {useOrgStore} from '@/stores/org'
import {api} from '@/api/client'
import type {AuditLogDTO} from '@/types/audit'
import TabHeader from "@/components/common/TabHeader.vue";
import DataTable from "@/components/common/DataTable.vue";

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

const tableHeaders = [
  {key: 'timestamp', label: 'Timestamp'},
  {key: 'user', label: 'User'},
  {key: 'action', label: 'Action'},
  {key: 'entity', label: 'Entity'},
  {key: 'metadata', label: 'Metadata'}
]

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
    }) + '\n' + ts.split('T')[1]

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
    <TabHeader description="View all actions performed within this organisation." title="Audit Logs"/>
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
    <DataTable :error :headers="tableHeaders" :isLoading :items="filteredLogs">
      <!-- Desktop -->
      <template #cell:timestamp="{ item }">
        <span class="whitespace-pre-line">{{ formatTs(item.timestamp) }}</span>
      </template>

      <template #cell:user="{ item }">
        <div class="text-sm text-gray-900">{{ item.userName }}</div>
        <div class="text-xs text-gray-500">{{ item.userEmail }}</div>
      </template>

      <template #cell:action="{ item }">
        <span :class="BADGE" class="inline-flex px-2 py-1 text-xs rounded font-medium">{{ item.action }}</span>
      </template>

      <template #cell:entity="{ item }">
        <div class="text-sm text-gray-900">{{ item.entityType }}</div>
        <div class="text-xs text-gray-500">ID: {{ item.entityId }}</div>
      </template>

      <template #cell:metadata="{ item }">
        <pre class="text-xs text-gray-600 font-mono whitespace-pre-wrap break-all">{{
            formatMetadata(item.metadata)
          }}</pre>
      </template>

      <!-- Mobile -->
      <template #mobile-item="{ item }">
        <div class="flex items-start justify-between mb-3">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1.5 flex-wrap">
              <div>
                <div class="text-sm text-gray-900">{{ item.userName }}</div>
                <div class="text-xs text-gray-500">{{ item.userEmail }}</div>
              </div>
              <span :class="BADGE" class="inline-flex px-2 py-1 text-xs rounded font-medium">
                  {{ item.action }}
                </span>
              <span class="text-xs text-gray-600 font-medium">{{ item.entityType }}
                  <span class="text-xs text-gray-400"> ID: {{ item.entityId }}</span>
                </span>
            </div>
          </div>
          <div class="text-xs text-gray-400 whitespace-nowrap ml-3 shrink-0">
            {{ formatTs(item.timestamp) }}
          </div>
        </div>
        <div class="pt-3 border-t border-gray-100">
          <pre class="text-xs text-gray-600 font-mono whitespace-pre-wrap break-all">{{
              formatMetadata(item.metadata)
            }}</pre>
        </div>
      </template>
    </DataTable>
    <div class="flex items-center justify-between px-6 py-4 border-t border-gray-200 bg-gray-50">
      <p class="text-xs text-gray-500">
        {{ totalElements }} total entr{{ totalElements !== 1 ? 'ies' : 'y' }}
      </p>
      <div class="flex items-center gap-1">
        <button
            :disabled="page === 0"
            class="px-3 py-1 border border-gray-300 rounded text-sm hover:bg-white
                     disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            @click="page = Math.max(0, page - 1)"
        >Previous
        </button>
        <button
            v-for="p in pageButtons"
            :key="p"
            :class="page === p
                ? 'bg-gray-900 text-white border-gray-900'
                : 'border-gray-300 hover:bg-white'"
            class="px-3 py-1 border rounded text-sm transition-colors"
            @click="page = p"
        >{{ p + 1 }}
        </button>
        <button
            :disabled="page >= totalPages - 1"
            class="px-3 py-1 border border-gray-300 rounded text-sm hover:bg-white
                     disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            @click="page = Math.min(totalPages - 1, page + 1)"
        >Next
        </button>
      </div>
    </div>
  </div>
</template>