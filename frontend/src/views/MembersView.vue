<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import {Plus, UserCog} from "@lucide/vue";
import {api} from '@/api/client';
import type {Member} from "@/types/member.ts";
import {useOrgStore} from "@/stores/org.ts";
import InviteMemberModal from '@/components/modals/InviteMemberModal.vue'
import ManageMemberRolesModal from '@/components/modals/MemberRolesModal.vue'
import TabHeader from "@/components/common/TabHeader.vue";
import SearchFilterBar from "@/components/common/SearchFilterBar.vue";
import DataTable from "@/components/common/DataTable.vue";

const orgStore = useOrgStore();

const members = ref<Member[]>([]);
const orgRoles      = ref<string[]>([])

const searchQuery = ref('');
const selectedRole = ref('All Roles');

const tableHeaders = [
  {key: 'member', label: 'Member'},
  {key: 'roles', label: 'Roles'},
  {key: 'actions', label: 'Actions'}
]

const showInvite      = ref(false)
const showManageRoles = ref(false)
const manageTarget    = ref<Member | null>(null)

watch(() => orgStore.currentOrgId, async (newId) => {
  if (newId) {
    members.value = await api.organisations.members.all(newId)
    orgRoles.value = (await api.organisations.rbac.roles(newId))
            .map((r: any) => r.name as string)
            .sort()
  }
}, { immediate: true });

const availableRoles = computed(() => {
  const roles = new Set<string>();
  members.value.forEach(m => {
    m.roles.forEach(role => roles.add(role));
  });
  return Array.from(roles).sort();
});

const filteredMembers = computed(() => {
  return members.value.filter(member => {
    const fullName = `${member.name} ${member.surname}`.toLowerCase();
    const matchesSearch = fullName.includes(searchQuery.value.toLowerCase()) ||
        member.email.toLowerCase().includes(searchQuery.value.toLowerCase());

    const matchesRole = selectedRole.value === 'All Roles' ||
        member.roles.includes(selectedRole.value);

    return matchesSearch && matchesRole;
  });
});

const handleInvite = async (email: string, role: string) => {
  await api.users.invite({
    orgId: orgStore.currentOrgId!,
    email,
    role,
  })
  members.value = await api.organisations.members.all(orgStore.currentOrgId!)
  showInvite.value = false
}

const openManageRoles = (member: Member) => {
  manageTarget.value    = member
  showManageRoles.value = true
}

const handleSaveRoles = async (memberId: number, roles: string[]) => {
  await api.organisations.members.updateRoles(orgStore.currentOrgId!, memberId, roles)
  const found = members.value.find((m) => m.id === memberId)
  if (found) found.roles = roles
  showManageRoles.value = false
  manageTarget.value    = null
}
</script>

<template>
  <div class="p-4 lg:p-8">
    <TabHeader title="Members">
      <template v-slot:actions>
        <button class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800
               flex items-center gap-2 justify-center transition-colors" @click="showInvite = true">
          <Plus :size="16"/>
          Add Member
        </button>
      </template>
    </TabHeader>
    <SearchFilterBar v-model="searchQuery" placeholder="Search members by name or email...">
      <template v-slot:filters>
        <select
            v-model="selectedRole"
            class="px-4 py-2 border border-gray-300 rounded text-sm bg-white outline-none
                 focus:ring-2 focus:ring-gray-300"
        >
          <option>All Roles</option>
          <option v-for="role in availableRoles" :key="role" :value="role">
            {{ role }}
          </option>
        </select>
      </template>
    </SearchFilterBar>
    <DataTable :headers="tableHeaders" :items="filteredMembers">
      <!-- Desktop -->
      <template #cell:member="{ item }">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-gray-200 border border-gray-300 rounded-full shrink-0
                            flex items-center justify-center text-gray-500 font-bold text-xs">
            {{ item.name?.[0] ?? '?' }}{{ item?.surname?.[0] ?? '' }}
          </div>
          <div>
            <div class="font-medium text-sm text-gray-900">{{ item.name }} {{ item.surname }}</div>
            <div class="text-xs text-gray-500">{{ item.email }}</div>
          </div>
        </div>
      </template>

      <template #cell:roles="{ item }">
        <div class="flex flex-wrap gap-1">
          <span v-for="role in item.roles" :key="role"
                class="inline-flex px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">
            {{ role }}
          </span>
        </div>
      </template>

      <template #cell:actions="{ item }">
        <button
            class="p-1.5 hover:bg-gray-200 rounded transition-colors flex items-center gap-1.5
                       text-sm text-gray-600 cursor-pointer"
            title="Manage roles"
            @click="openManageRoles(item)">
          <UserCog :size="15"/>
          Manage Roles
        </button>
      </template>

      <!-- Mobile -->
      <template #mobile-item="{ item }">
        <div class="flex items-start gap-3 mb-3">
          <div class="w-12 h-12 bg-gray-200 border border-gray-300 rounded-full shrink-0 flex items-center justify-center text-gray-500 font-bold">
            {{ item.name?.[0] ?? '?' }}{{ item.surname?.[0] ?? '' }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-gray-900">{{ item.name }} {{ item.surname }}</div>
            <div class="text-xs text-gray-500 mt-0.5">{{ item.email }}</div>
          </div>
        </div>
        <div class="flex flex-wrap gap-1.5 mb-3">
          <span
              v-for="role in item.roles"
              :key="role"
              class="px-2 py-1 rounded-full text-xs bg-blue-100 text-blue-800"
          >{{ role }}</span>
        </div>
        <div class="pt-3 border-t border-gray-100">
          <button
              class="w-full px-3 py-1.5 border border-gray-300 rounded text-sm
                   hover:bg-gray-50 flex items-center justify-center gap-1.5 transition-colors"
              @click="openManageRoles(item)"
          >
            <UserCog :size="15"/>
            Manage Roles
          </button>
        </div>
      </template>
    </DataTable>

    <InviteMemberModal
        :show="showInvite"
        :availableRoles="orgRoles"
        @close="showInvite = false"
        @invite="handleInvite"
    />

    <ManageMemberRolesModal
        :show="showManageRoles"
        :member="manageTarget"
        :availableRoles="orgRoles"
        @close="showManageRoles = false; manageTarget = null"
        @save="handleSaveRoles"
    />
  </div>
</template>