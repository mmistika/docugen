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
import Modal from '@/components/modals/Modal.vue';

const props = defineProps<{
    show: boolean;
    isSubmitting?: boolean;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'create', name: string): void;
}>();

const newOrgName = ref('');

watch(
    () => props.show,
    (show) => {
        if (show) {
            newOrgName.value = '';
        }
    }
);

const submit = () => {
    const nameTrimmed = newOrgName.value.trim();
    if (!nameTrimmed) return;
    emit('create', nameTrimmed);
};
</script>

<template>
    <Modal :show="show" title="Create New Organization" @close="emit('close')">
        <template #body>
            <form id="createOrgForm" class="space-y-4" @submit.prevent="submit">
                <div>
                    <label
                        class="block text-sm font-medium text-gray-700 mb-1"
                        for="orgName"
                    >
                        Organization name *
                    </label>
                    <input
                        id="orgName"
                        v-model="newOrgName"
                        :disabled="isSubmitting"
                        class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Enter organization name"
                        required
                        type="text"
                    />
                </div>
            </form>
        </template>
        <template #footer>
            <button
                :disabled="isSubmitting"
                class="px-4 py-2 border border-gray-300 rounded text-sm font-medium hover:bg-gray-50"
                type="button"
                @click="emit('close')"
            >
                Cancel
            </button>
            <button
                :disabled="isSubmitting || !newOrgName.trim()"
                class="px-4 py-2 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                form="createOrgForm"
                type="submit"
            >
                {{ isSubmitting ? 'Creating...' : 'Create Organization' }}
            </button>
        </template>
    </Modal>
</template>
