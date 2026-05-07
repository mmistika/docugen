<script setup lang="ts">
import {computed, ref} from 'vue';
import {Building2, Plus, Settings, Users} from "@lucide/vue";
import {RouterLink} from 'vue-router';
import {useOrgStore} from '@/stores/org';
import {api} from "@/api/client.ts";
import Modal from "@/components/modals/Modal.vue";
import TabHeader from "@/components/common/TabHeader.vue";
import SearchFilterBar from "@/components/common/SearchFilterBar.vue";
import DataTable from "@/components/common/DataTable.vue";

const orgStore = useOrgStore();

const searchQuery = ref('');
const isModalOpen = ref(false);
const isSubmitting = ref(false);

const newOrgName = ref('');

const filteredOrganisations = computed(() => {
  if (!searchQuery.value) return orgStore.organisations;

  const query = searchQuery.value.toLowerCase();
  return orgStore.organisations.filter(org =>
      org.name.toLowerCase().includes(query)
  );
});

const tableHeaders = [
  {key: 'organisation', label: 'Organisation'},
  {key: 'memberCount', label: 'Members'},
  {key: 'actions', label: 'Actions'}
]

const handleClose = () => {
  isModalOpen.value = false;
  newOrgName.value = '';
};

const createOrganisation = async () => {
  if (!newOrgName.value.trim() || isSubmitting.value) return;

  isSubmitting.value = true;
  try {
    await api.organisations.create({name: newOrgName.value});
    await orgStore.fetch();
    handleClose();
  } catch (error) {
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div class="p-4 lg:p-8">
    <TabHeader title="Organisations">
      <template v-slot:actions>
        <button class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 justify-center"
                @click="isModalOpen = true">
          <Plus :size="16"/>
          New Organisation
        </button>
      </template>
    </TabHeader>
    <SearchFilterBar v-model="searchQuery" placeholder="Search organisations..."/>
    <DataTable :headers="tableHeaders" :items="filteredOrganisations">
      <!-- Desktop -->
      <template #cell:organisation="{ item }">
        <RouterLink :to="`/organisations/${item.id}`" class="flex items-center gap-3 hover:underline">
          <div class="w-10 h-10 bg-gray-300 rounded flex items-center justify-center">
            <Building2 :size="20" class="text-gray-600"/>
          </div>
          <span class="font-medium text-gray-900">{{ item.name }}</span>
        </RouterLink>
      </template>

      <template #cell:actions="{ item }">
        <RouterLink
            :to="`/organisations/${item.id}/settings`"
            class="flex items-center gap-1.5 whitespace-nowrap p-1.5 text-sm text-gray-600 hover:bg-gray-200 rounded transition-colors w-fit"
        >
          <Settings :size="15"/>
          <span>Settings</span>
        </RouterLink>
      </template>

      <!-- Mobile -->
      <template #mobile-item="{ item }">
        <RouterLink :to="`/organisations/${item.id}`" class="block">
          <div class="flex items-start justify-between mb-3">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-gray-300 rounded flex items-center justify-center">
                <Building2 :size="20" class="text-gray-600" />
              </div>
              <div class="font-medium text-gray-900">{{ item.name }}</div>
            </div>
          </div>
          <div class="flex gap-4 text-sm">
            <div class="flex items-center gap-1 text-gray-600">
              <Users :size="14" />
              <span>{{ item.memberCount }} members</span>
            </div>
          </div>
        </RouterLink>
        <RouterLink :to="`/organisations/${item.id}/settings`"
                    class="absolute top-4 right-4 flex items-center gap-1.5 whitespace-nowrap p-1.5 text-sm text-gray-600 hover:bg-gray-200 rounded transition-colors w-fit">
          <Settings :size="15"/>
          <span>Settings</span>
        </RouterLink>
      </template>
    </DataTable>

    <Modal
        :show="isModalOpen"
        title="Create New Organization"
        @close="handleClose"
    >
      <template #body>
        <form @submit.prevent="createOrganisation" id="createOrgForm" class="space-y-4">
          <div>
            <label for="orgName" class="block text-sm font-medium text-gray-700 mb-1">
              Organization name *
            </label>
            <input
                v-model="newOrgName"
                type="text"
                id="orgName"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Enter organization name"
                :disabled="isSubmitting"
            />
          </div>
        </form>
      </template>
      <template #footer>
        <button
            @click="handleClose"
            type="button"
            class="px-4 py-2 border border-gray-300 rounded text-sm font-medium hover:bg-gray-50"
            :disabled="isSubmitting"
        >
          Cancel
        </button>
        <button
            form="createOrgForm"
            type="submit"
            class="px-4 py-2 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="isSubmitting || !newOrgName.trim()"
        >
          {{ isSubmitting ? 'Creating...' : 'Create Organization' }}
        </button>
      </template>
    </Modal>
  </div>
</template>