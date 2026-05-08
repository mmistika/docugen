<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import Modal from '@/components/modals/Modal.vue';
import type { PermissionDTO, RoleDTO } from '@/types/rbac.ts';

const props = defineProps<{
    show: boolean;
    // null for create, RoleDTO for edit
    role: RoleDTO | null;
    allPermissions: PermissionDTO[];
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'save', name: string, permissions: string[]): void;
}>();

const name = ref('');
const selectedPerms = ref<Set<string>>(new Set());
const isSaving = ref(false);
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

const groupedPermissions = computed(() => {
    const groups = new Map<string, PermissionDTO[]>();
    for (const p of props.allPermissions) {
        const group = p.name.includes(':') ? p.name.split(':')[0] : 'other';
        if (!groups.has(group!)) groups.set(group!, []);
        groups.get(group!)!.push(p);
    }
    return groups;
});

const formatGroup = (key: string) => key.charAt(0).toUpperCase() + key.slice(1);

const formatPerm = (name: string) => {
    const part = name.includes(':') ? name.split(':')[1] : name;
    return part!.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
};

const toggle = (permName: string) => {
    if (selectedPerms.value.has(permName)) {
        selectedPerms.value.delete(permName);
    } else {
        selectedPerms.value.add(permName);
    }
    selectedPerms.value = new Set(selectedPerms.value);
};

const toggleGroup = (perms: PermissionDTO[]) => {
    const allSelected = perms.every((p) => selectedPerms.value.has(p.name));
    const next = new Set(selectedPerms.value);
    if (allSelected) {
        perms.forEach((p) => next.delete(p.name));
    } else {
        perms.forEach((p) => next.add(p.name));
    }
    selectedPerms.value = next;
};

const groupAllSelected = (perms: PermissionDTO[]) =>
    perms.every((p) => selectedPerms.value.has(p.name));

const groupSomeSelected = (perms: PermissionDTO[]) =>
    perms.some((p) => selectedPerms.value.has(p.name)) &&
    !groupAllSelected(perms);

const isEditMode = computed(() => props.role !== null);

const submit = async () => {
    nameError.value = null;
    if (!name.value.trim()) {
        nameError.value = 'Role name is required.';
        return;
    }
    if (selectedPerms.value.size === 0) {
        nameError.value = 'Select at least one permission.';
        return;
    }
    isSaving.value = true;
    try {
        emit('save', name.value.trim(), [...selectedPerms.value]);
    } finally {
        isSaving.value = false;
    }
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

                    <div class="space-y-3 max-h-72 overflow-y-auto pr-1">
                        <div
                            v-for="[group, perms] in groupedPermissions"
                            :key="group"
                            class="border border-gray-200 rounded-lg overflow-hidden"
                        >
                            <button
                                class="w-full flex items-center gap-2 px-3 py-2 bg-gray-50 hover:bg-gray-100 transition-colors text-left"
                                type="button"
                                @click="toggleGroup(perms)"
                            >
                                <div
                                    :class="
                                        groupAllSelected(perms)
                                            ? 'bg-gray-900 border-gray-900'
                                            : groupSomeSelected(perms)
                                              ? 'bg-gray-400 border-gray-400'
                                              : 'border-gray-300'
                                    "
                                    class="w-3.5 h-3.5 rounded border flex items-center justify-center shrink-0 transition-colors"
                                >
                                    <svg
                                        v-if="groupAllSelected(perms)"
                                        class="w-2 h-2 text-white"
                                        fill="none"
                                        viewBox="0 0 10 8"
                                    >
                                        <path
                                            d="M1 4l3 3 5-6"
                                            stroke="currentColor"
                                            stroke-linecap="round"
                                            stroke-linejoin="round"
                                            stroke-width="1.5"
                                        />
                                    </svg>
                                    <div
                                        v-else-if="groupSomeSelected(perms)"
                                        class="w-1.5 h-0.5 bg-white rounded"
                                    />
                                </div>
                                <span
                                    class="text-xs font-semibold text-gray-700"
                                    >{{ formatGroup(group) }}</span
                                >
                                <span class="ml-auto text-xs text-gray-400">
                                    {{
                                        perms.filter((p) =>
                                            selectedPerms.has(p.name)
                                        ).length
                                    }}/{{ perms.length }}
                                </span>
                            </button>
                            <div class="divide-y divide-gray-100">
                                <label
                                    v-for="perm in perms"
                                    :key="perm.name"
                                    class="flex items-center gap-2.5 px-3 py-2 cursor-pointer hover:bg-gray-50 transition-colors"
                                >
                                    <div
                                        :class="
                                            selectedPerms.has(perm.name)
                                                ? 'bg-gray-900 border-gray-900'
                                                : 'border-gray-300'
                                        "
                                        class="w-3.5 h-3.5 rounded border flex items-center justify-center shrink-0 transition-colors"
                                        @click.prevent="toggle(perm.name)"
                                    >
                                        <svg
                                            v-if="selectedPerms.has(perm.name)"
                                            class="w-2 h-2 text-white"
                                            fill="none"
                                            viewBox="0 0 10 8"
                                        >
                                            <path
                                                d="M1 4l3 3 5-6"
                                                stroke="currentColor"
                                                stroke-linecap="round"
                                                stroke-linejoin="round"
                                                stroke-width="1.5"
                                            />
                                        </svg>
                                    </div>
                                    <input
                                        :checked="selectedPerms.has(perm.name)"
                                        class="sr-only"
                                        type="checkbox"
                                        @change="toggle(perm.name)"
                                    />
                                    <span class="text-xs text-gray-700">{{
                                        formatPerm(perm.name)
                                    }}</span>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </template>
        <template #footer>
            <button
                class="px-4 py-2 border border-gray-300 rounded text-sm hover:bg-gray-50 transition-colors"
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
