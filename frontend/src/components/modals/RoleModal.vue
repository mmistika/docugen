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
import { computed, ref, watch } from 'vue';
import Modal from '@/components/modals/Modal.vue';
import PermissionSelector from '@/components/common/PermissionSelector.vue';
import type { PermissionDTO, RoleDTO } from '@/types/rbac.ts';

const props = defineProps<{
    show: boolean;
    // null for create, RoleDTO for edit
    role: RoleDTO | null;
    allPermissions: PermissionDTO[];
    isSaving?: boolean;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'save', name: string, permissions: string[]): void;
}>();

const name = ref('');
const selectedPerms = ref<Set<string>>(new Set());
const nameError = ref<string | null>(null);

watch(
    () => [props.show, props.role] as const,
    ([show, role]) => {
        if (!show) return;
        name.value = role?.name ?? '';
        selectedPerms.value = new Set(role?.permissions ?? []);
        nameError.value = null;
    },
    { immediate: true }
);

const isEditMode = computed(() => props.role !== null);

const submit = () => {
    nameError.value = null;
    if (!name.value.trim()) {
        nameError.value = 'Role name is required.';
        return;
    }
    if (selectedPerms.value.size === 0) {
        nameError.value = 'Select at least one permission.';
        return;
    }
    emit('save', name.value.trim(), [...selectedPerms.value]);
};
</script>

<template>
    <Modal
        :show="show"
        :title="isEditMode ? 'Edit Role' : 'Create Role'"
        @close="emit('close')"
    >
        <template #body>
            <div class="space-y-5">
                <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">
                        Role Name <span class="text-red-500">*</span>
                    </label>
                    <input
                        v-model="name"
                        :class="
                            nameError
                                ? 'border-red-400 bg-red-50'
                                : isEditMode
                                  ? 'border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed'
                                  : 'border-gray-300'
                        "
                        :disabled="isEditMode"
                        class="w-full px-3 py-2 border rounded text-sm focus:outline-none transition-colors"
                        placeholder="e.g. Reviewer"
                        type="text"
                    />
                    <p v-if="isEditMode" class="mt-1 text-xs text-gray-400">
                        Role name cannot be changed after creation.
                    </p>
                    <p v-else-if="nameError" class="mt-1 text-xs text-red-500">
                        {{ nameError }}
                    </p>
                </div>
                <div>
                    <label class="block text-xs font-medium text-gray-600 mb-2">
                        Permissions <span class="text-red-500">*</span>
                    </label>
                    <PermissionSelector
                        v-model="selectedPerms"
                        :all-permissions="allPermissions"
                    />
                </div>
            </div>
        </template>
        <template #footer>
            <button
                :disabled="isSaving"
                class="px-4 py-2 border border-gray-300 rounded text-sm hover:bg-gray-50 transition-colors disabled:opacity-50"
                @click="emit('close')"
            >
                Cancel
            </button>
            <button
                :disabled="isSaving"
                class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 transition-colors disabled:opacity-50"
                @click="submit"
            >
                {{ isEditMode ? 'Save Changes' : 'Create Role' }}
            </button>
        </template>
    </Modal>
</template>
