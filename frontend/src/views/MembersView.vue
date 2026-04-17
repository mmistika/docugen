<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import {Plus, Search} from "@lucide/vue";
import {api} from '@/api/client';
import type {Member} from "@/types/member.ts";
import {useOrgStore} from "@/stores/org.ts";

const orgStore = useOrgStore();

const members = ref<Member[]>([]);
const searchQuery = ref('');
const selectedRole = ref('All Roles');

watch(() => orgStore.currentOrgId, async (newId) => {
  if (newId) {
    members.value = await api.organisations.members(newId);
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
</script>

<template>
  <div class="p-4 lg:p-8">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 mb-1">Members</h1>
      </div>
      <button class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 justify-center">
        <Plus :size="16" />
        Add Member
      </button>
    </div>

    <div class="bg-white border border-gray-300 rounded-lg p-4 mb-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="flex-1 relative">
          <Search :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Search members by name or email..."
              class="w-full pl-9 pr-4 py-2 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <select
            v-model="selectedRole"
            class="px-4 py-2 border border-gray-300 rounded text-sm bg-white outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option>All Roles</option>
          <option v-for="role in availableRoles" :key="role" :value="role">
            {{ role }}
          </option>
        </select>
      </div>
    </div>

    <div class="hidden lg:block bg-white border border-gray-300 rounded-lg overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50 border-b border-gray-300">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Member</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Roles</th>
        </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
        <tr v-for="member in filteredMembers" :key="member.id" class="hover:bg-gray-50">
          <td class="px-6 py-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-gray-200 border border-gray-300 rounded-full shrink-0 flex items-center justify-center text-gray-500 font-bold text-xs">
                {{ member.name[0] }}{{ member.surname[0] }}
              </div>
              <div>
                <div class="font-medium text-sm text-gray-900">{{ member.name }} {{ member.surname }}</div>
                <div class="text-xs text-gray-600">{{ member.email }}</div>
              </div>
            </div>
          </td>
          <td class="px-6 py-4">
            <div class="flex flex-wrap gap-1">
                  <span
                      v-for="role in member.roles"
                      :key="role"
                      class="inline-flex px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800"
                  >
                    {{ role }}
                  </span>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <div class="lg:hidden space-y-3">
      <div v-for="member in filteredMembers" :key="member.id" class="bg-white border border-gray-300 rounded-lg p-4">
        <div class="flex items-start gap-3 mb-3">
          <div class="w-12 h-12 bg-gray-200 border border-gray-300 rounded-full shrink-0 flex items-center justify-center text-gray-500 font-bold">
            {{ member.name[0] }}{{ member.surname[0] }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-gray-900">{{ member.name }} {{ member.surname }}</div>
            <div class="text-xs text-gray-600 flex items-center gap-1 mt-1">
              {{ member.email }}
            </div>
          </div>
        </div>
        <div class="flex flex-wrap items-center gap-2 text-xs">
          <span
              v-for="role in member.roles"
              :key="role"
              class="px-2 py-1 rounded-full bg-blue-100 text-blue-800"
          >
            {{ role }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>