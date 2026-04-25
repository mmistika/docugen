<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Plus, Shield, Trash2, Pencil, Lock } from '@lucide/vue'
import { api } from '@/api/client'
import RoleModal from '@/components/RoleModal.vue'
import type { RoleDTO, PermissionDTO } from '@/types/rbac'

const route    = useRoute()
const orgId    = computed(() => Number(route.params.id))

type Tab = 'general' | 'rbac' | 'api'
const activeTab = ref<Tab>('rbac')

const TABS: { key: Tab; label: string }[] = [
  { key: 'general', label: 'General' },
  { key: 'rbac',    label: 'Roles & Permissions' },
  { key: 'api',     label: 'API Tokens' },
]

const roles          = ref<RoleDTO[]>([])
const allPermissions = ref<PermissionDTO[]>([])
const isLoading      = ref(true)
const error          = ref<string | null>(null)

onMounted(async () => {
  try {
    roles.value = await api.organisations.rbac.roles(orgId.value)
    allPermissions.value = await api.organisations.rbac.permissions(orgId.value)
  } catch {
  } finally {
    isLoading.value = false
  }
})

const isAdmin = (role: RoleDTO) => role.name === 'ADMIN'

const formatPerm = (name: string) => {
  const part = name.includes(':') ? name.split(':')[1] : name
  return part!.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())
}

const formatGroup = (name: string) =>
    name.charAt(0).toUpperCase() + name.slice(1)

const permissionGroups = computed(() => {
  const groups = new Map<string, PermissionDTO[]>()
  for (const p of allPermissions.value) {
    const group = p.name.includes(':') ? p.name.split(':')[0] : 'other'
    if (!groups.has(group!)) groups.set(group!, [])
    groups.get(group!)!.push(p)
  }
  return groups
})

const showModal  = ref(false)
const editTarget = ref<RoleDTO | null>(null)

const openCreate = () => {
  editTarget.value = null
  showModal.value  = true
}

const openEdit = (role: RoleDTO) => {
  editTarget.value = role
  showModal.value  = true
}

const closeModal = () => {
  showModal.value  = false
  editTarget.value = null
}

const saveRole = async (name: string, permissions: string[]) => {
  if (editTarget.value) {
    await api.organisations.rbac.updateRole(orgId.value, { name, permissions })
    const found = roles.value.find((r) => r.name === editTarget.value!.name)
    if (found) found.permissions = permissions
  } else {
    await api.organisations.rbac.createRole(orgId.value, { name, permissions })
    roles.value = await api.organisations.rbac.roles(orgId.value)
  }
  closeModal()
}

const deleteRole = async (role: RoleDTO) => {
  if (!confirm(`Delete role "${role.name}"? This cannot be undone.`)) return
  await api.organisations.rbac.deleteRole(orgId.value, role.name)
  roles.value = roles.value.filter((r) => r.name !== role.name)
}
</script>

<template>
  <div class="p-4 lg:p-8">
    <RouterLink
        to="/organizations"
        class="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-4"
    >
      <ArrowLeft :size="16" />
      Back to Organizations
    </RouterLink>
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900 mb-1">Organization Settings</h1>
      <p class="text-sm text-gray-600">ID: {{ orgId }}</p>
    </div>
    <div class="border-b border-gray-300 mb-6">
      <nav class="flex gap-6">
        <button
            v-for="tab in TABS"
            :key="tab.key"
            @click="activeTab = tab.key"
            class="pb-3 px-1 border-b-2 text-sm font-medium transition-colors"
            :class="activeTab === tab.key
            ? 'border-gray-900 text-gray-900'
            : 'border-transparent text-gray-600 hover:text-gray-900'"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>
    <div v-if="activeTab === 'general'" class="text-sm text-gray-400">
    </div>
    <div v-else-if="activeTab === 'api'" class="text-sm text-gray-400">
    </div>
    <div v-else-if="activeTab === 'rbac'">
      <div class="flex items-start justify-between mb-6 gap-4">
        <p class="text-sm text-gray-600 max-w-lg">
          Define roles and their permissions. Roles control what members can do within this
          organisation.
        </p>
        <button
            @click="openCreate"
            class="shrink-0 px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800
                 flex items-center gap-2 transition-colors"
        >
          <Plus :size="16" />
          Create Role
        </button>
      </div>
      <div v-if="isLoading" class="text-sm text-gray-400 py-8 text-center">
        Loading roles…
      </div>
      <div v-else-if="error" class="text-sm text-red-500 py-8 text-center">
        {{ error }}
      </div>
      <div
          v-else-if="roles.length === 0"
          class="text-center py-12 border border-dashed border-gray-300 rounded-lg"
      >
        <p class="text-sm text-gray-500">No roles defined yet.</p>
      </div>
      <div v-else class="space-y-4">
        <div
            v-for="role in roles"
            :key="role.name"
            class="bg-white border border-gray-300 rounded-lg p-6"
        >
          <div class="flex items-start justify-between mb-5">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-gray-100 rounded flex items-center justify-center shrink-0">
                <Lock v-if="isAdmin(role)" :size="18" class="text-gray-400" />
                <Shield v-else :size="18" class="text-gray-500" />
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <h3 class="font-semibold text-gray-900">{{ role.name }}</h3>
                  <span
                      v-if="isAdmin(role)"
                      class="px-1.5 py-px text-[10px] font-bold uppercase tracking-wide
                           rounded bg-gray-100 text-gray-500 border border-gray-200"
                  >System</span>
                </div>
                <p class="text-xs text-gray-500">
                  {{ role.permissions.length }} permission{{ role.permissions.length !== 1 ? 's' : '' }}
                </p>
              </div>
            </div>
            <div v-if="!isAdmin(role)" class="flex items-center gap-1">
              <button
                  @click="openEdit(role)"
                  title="Edit role"
                  class="p-1.5 hover:bg-gray-100 rounded transition-colors"
              >
                <Pencil :size="15" class="text-gray-500" />
              </button>
              <button
                  @click="deleteRole(role)"
                  title="Delete role"
                  class="p-1.5 hover:bg-red-50 rounded transition-colors"
              >
                <Trash2 :size="15" class="text-red-500" />
              </button>
            </div>
          </div>
          <div class="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-6 gap-y-4">
            <div v-for="[group, perms] in permissionGroups" :key="group">
              <p class="text-[10px] font-semibold text-gray-400 uppercase tracking-wider mb-1.5">
                {{ formatGroup(group) }}
              </p>
              <div class="flex flex-col gap-1">
                <span
                    v-for="perm in perms"
                    :key="perm.name"
                    class="inline-flex items-center gap-1.5 px-2 py-1 rounded text-xs font-medium"
                    :class="role.permissions.includes(perm.name)
                    ? 'bg-green-50 text-green-800 border border-green-200'
                    : 'bg-gray-50 text-gray-400 border border-gray-200'"
                >
                  <span
                      class="w-1.5 h-1.5 rounded-full shrink-0"
                      :class="role.permissions.includes(perm.name) ? 'bg-green-500' : 'bg-gray-300'"
                  />
                  {{ formatPerm(perm.name) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <RoleModal
        :show="showModal"
        :role="editTarget"
        :allPermissions="allPermissions"
        @close="closeModal"
        @save="saveRole"
    />
  </div>
</template>