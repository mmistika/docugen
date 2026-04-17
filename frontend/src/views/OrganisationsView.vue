<script setup lang="ts">
import { ref, computed } from 'vue';
import { Search, Plus, Building2, Users } from "@lucide/vue";
import { RouterLink } from 'vue-router';
import { useOrgStore } from '@/stores/org';

const orgStore = useOrgStore();

const searchQuery = ref('');

const filteredOrganisations = computed(() => {
  if (!searchQuery.value) return orgStore.organisations;

  const query = searchQuery.value.toLowerCase();
  return orgStore.organisations.filter(org =>
      org.name.toLowerCase().includes(query)
  );
});

const createOrganisation = () => {
  //stub
};
</script>

<template>
  <div class="p-4 lg:p-8">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 mb-1">Organisations</h1>
      </div>
      <button
          @click="createOrganisation"
          class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 justify-center"
      >
        <Plus :size="16" />
        New Organisation
      </button>
    </div>

    <div class="bg-white border border-gray-300 rounded-lg p-4 mb-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="flex-1 relative">
          <Search :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Search organisations..."
              class="w-full pl-9 pr-4 py-2 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>
    </div>
    <div v-if="filteredOrganisations.length === 0" class="text-center py-12 bg-white border border-dashed border-gray-300 rounded-lg">
      <p class="text-gray-500">No organisations found.</p>
    </div>
    <div v-else class="hidden lg:block bg-white border border-gray-300 rounded-lg overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50 border-b border-gray-300">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Organisation</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Members</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase">Actions</th>
        </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
        <tr v-for="org in filteredOrganisations" :key="org.id" class="hover:bg-gray-50">
          <td class="px-6 py-4">
            <RouterLink :to="`/organisations/${org.id}`" class="flex items-center gap-3 hover:underline">
              <div class="w-10 h-10 bg-gray-300 rounded flex items-center justify-center">
                <Building2 :size="20" class="text-gray-600" />
              </div>
              <span class="font-medium text-gray-900">{{ org.name }}</span>
            </RouterLink>
          </td>
          <td class="px-6 py-4 text-sm text-gray-600">
            {{ org.memberCount}}
          </td>
          <td class="px-6 py-4">
            <RouterLink :to="`/organisations/${org.id}/settings`" class="text-sm text-gray-600 hover:text-gray-900">
              Settings
            </RouterLink>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <div v-if="filteredOrganisations.length > 0" class="lg:hidden space-y-3">
      <div
          v-for="org in filteredOrganisations"
          :key="org.id"
          class="relative block bg-white border border-gray-300 rounded-lg p-4 hover:shadow"
      >
        <RouterLink :to="`/organisations/${org.id}`" class="block">
          <div class="flex items-start justify-between mb-3">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-gray-300 rounded flex items-center justify-center">
                <Building2 :size="20" class="text-gray-600" />
              </div>
              <div class="font-medium text-gray-900">{{ org.name }}</div>
            </div>
          </div>
          <div class="flex gap-4 text-sm">
            <div class="flex items-center gap-1 text-gray-600">
              <Users :size="14" />
              <span>{{ org.memberCount }} members</span>
            </div>
          </div>
        </RouterLink>
        <RouterLink :to="`/organisations/${org.id}/settings`" class="absolute top-4 right-4 text-sm text-gray-600 hover:text-gray-900">
          Settings
        </RouterLink>
      </div>
    </div>
  </div>
</template>