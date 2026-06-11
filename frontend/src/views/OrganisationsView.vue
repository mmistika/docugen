<!--
Docugen — Document Generation & Management Platform
Copyright (C) 2026 Artem Bilous

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
-->

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { Building2, Plus, Settings, Users } from '@lucide/vue';
import { RouterLink } from 'vue-router';
import { useOrgStore } from '@/stores/org';
import { api } from '@/api/client.ts';
import CreateOrganisationModal from '@/components/modals/CreateOrganisationModal.vue';
import TabHeader from '@/components/common/TabHeader.vue';
import SearchFilterBar from '@/components/common/SearchFilterBar.vue';
import DataTable from '@/components/common/DataTable.vue';

const orgStore = useOrgStore();

const searchQuery = ref('');
const isModalOpen = ref(false);
const isSubmitting = ref(false);

const filteredOrganisations = computed(() => {
    if (!searchQuery.value) return orgStore.organisations;

    const query = searchQuery.value.toLowerCase();
    return orgStore.organisations.filter((org) =>
        org.name.toLowerCase().includes(query)
    );
});

const tableHeaders = [
    { key: 'organisation', label: 'Organisation' },
    { key: 'memberCount', label: 'Members' },
    { key: 'actions', label: 'Actions' }
];

const handleClose = () => {
    isModalOpen.value = false;
};

const createOrganisation = async (name: string) => {
    if (isSubmitting.value) return;

    isSubmitting.value = true;
    try {
        await api.organisations.create({ name });
        await orgStore.fetch();
        handleClose();
    } catch (error) {
        console.error('Failed to create organisation:', error);
    } finally {
        isSubmitting.value = false;
    }
};
</script>

<template>
    <div class="p-4 lg:p-8">
        <TabHeader title="Organisations">
            <template #actions>
                <button
                    class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 justify-center"
                    @click="isModalOpen = true"
                >
                    <Plus :size="16" />
                    New Organisation
                </button>
            </template>
        </TabHeader>
        <SearchFilterBar
            v-model="searchQuery"
            placeholder="Search organisations..."
        />
        <DataTable :headers="tableHeaders" :items="filteredOrganisations">
            <!-- Desktop -->
            <template #cell:organisation="{ item }">
                <RouterLink
                    :to="`/organisations/${item.id}`"
                    class="flex items-center gap-3 hover:underline"
                >
                    <div
                        class="w-10 h-10 bg-gray-300 rounded flex items-center justify-center"
                    >
                        <Building2 :size="20" class="text-gray-600" />
                    </div>
                    <span class="font-medium text-gray-900">{{
                        item.name
                    }}</span>
                </RouterLink>
            </template>

            <template #cell:actions="{ item }">
                <RouterLink
                    :to="`/organisations/${item.id}/settings`"
                    class="flex items-center gap-1.5 whitespace-nowrap p-1.5 text-sm text-gray-600 hover:bg-gray-200 rounded transition-colors w-fit"
                >
                    <Settings :size="15" />
                    <span>Settings</span>
                </RouterLink>
            </template>

            <!-- Mobile -->
            <template #mobile-item="{ item }">
                <RouterLink :to="`/organisations/${item.id}`" class="block">
                    <div class="flex items-start justify-between mb-3">
                        <div class="flex items-center gap-3">
                            <div
                                class="w-12 h-12 bg-gray-300 rounded flex items-center justify-center"
                            >
                                <Building2 :size="20" class="text-gray-600" />
                            </div>
                            <div class="font-medium text-gray-900">
                                {{ item.name }}
                            </div>
                        </div>
                    </div>
                    <div class="flex gap-4 text-sm">
                        <div class="flex items-center gap-1 text-gray-600">
                            <Users :size="14" />
                            <span>{{ item.memberCount }} members</span>
                        </div>
                    </div>
                </RouterLink>
                <RouterLink
                    :to="`/organisations/${item.id}/settings`"
                    class="absolute top-4 right-4 flex items-center gap-1.5 whitespace-nowrap p-1.5 text-sm text-gray-600 hover:bg-gray-200 rounded transition-colors w-fit"
                >
                    <Settings :size="15" />
                    <span>Settings</span>
                </RouterLink>
            </template>
        </DataTable>

        <CreateOrganisationModal
            :is-submitting="isSubmitting"
            :show="isModalOpen"
            @close="handleClose"
            @create="createOrganisation"
        />
    </div>
</template>
