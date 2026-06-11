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
import { ref, watch } from 'vue';
import { Save } from '@lucide/vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

const props = defineProps<{
    orgId: number;
}>();

const orgName = ref('');
const orgNameSaving = ref(false);
const orgNameError = ref<string | null>(null);
const orgNameSuccess = ref(false);
const isLoading = ref(true);

const fetchSettings = async () => {
    isLoading.value = true;
    try {
        const orgs = await api.organisations.my();
        const org = orgs.find((o) => o.id === props.orgId);
        if (org) orgName.value = org.name;
    } catch (error) {
        console.error('Failed to fetch user organisations in settings:', error);
    } finally {
        isLoading.value = false;
    }
};

const saveOrgName = async () => {
    orgNameError.value = null;
    orgNameSuccess.value = false;

    if (!orgName.value.trim()) {
        orgNameError.value = 'Organisation name cannot be empty.';
        return;
    }

    orgNameSaving.value = true;
    try {
        await api.organisations.rename(props.orgId, {
            name: orgName.value.trim()
        });
        const orgStore = useOrgStore();
        await orgStore.fetch();
        orgNameSuccess.value = true;
        setTimeout(() => (orgNameSuccess.value = false), 3000);
    } catch {
        orgNameError.value = 'Failed to save. Please try again.';
    } finally {
        orgNameSaving.value = false;
    }
};

watch(
    () => props.orgId,
    (newOrgId) => {
        if (newOrgId) {
            fetchSettings();
        }
    },
    { immediate: true }
);
</script>

<template>
    <div v-if="isLoading" class="text-sm text-gray-400 py-8 text-center">
        Loading settings…
    </div>
    <div v-else class="max-w-md">
        <div class="bg-white border border-gray-300 rounded-lg p-6">
            <h2 class="text-sm font-semibold text-gray-900 mb-4">
                General Settings
            </h2>

            <div class="space-y-4">
                <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">
                        Organisation Name
                    </label>
                    <input
                        v-model="orgName"
                        :class="
                            orgNameError
                                ? 'border-red-400 bg-red-50'
                                : 'border-gray-300'
                        "
                        class="w-full px-3 py-2 border rounded text-sm focus:outline-none transition-colors"
                        placeholder="Organisation name"
                        type="text"
                        @keydown.enter="saveOrgName"
                    />
                    <p v-if="orgNameError" class="mt-1 text-xs text-red-500">
                        {{ orgNameError }}
                    </p>
                    <p
                        v-else-if="orgNameSuccess"
                        class="mt-1 text-xs text-green-600"
                    >
                        Name updated successfully.
                    </p>
                </div>

                <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">
                        Organisation ID
                    </label>
                    <input
                        :value="orgId"
                        class="w-full px-3 py-2 border border-gray-200 rounded text-sm bg-gray-50 text-gray-400 cursor-not-allowed"
                        disabled
                        type="text"
                    />
                </div>
            </div>
            <div class="mt-6 flex justify-end">
                <button
                    :disabled="orgNameSaving"
                    class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 transition-colors flex items-center gap-2 disabled:opacity-50"
                    @click="saveOrgName"
                >
                    <Save :size="14" />
                    {{ orgNameSaving ? 'Saving…' : 'Save Changes' }}
                </button>
            </div>
        </div>
    </div>
</template>
