<script lang="ts" setup>
import { computed } from 'vue';
import type { PermissionDTO } from '@/types/rbac.ts';

const model = defineModel<Set<string>>({ required: true });

const props = defineProps<{
    allPermissions: PermissionDTO[];
}>();

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
    const next = new Set(model.value);
    if (next.has(permName)) {
        next.delete(permName);
    } else {
        next.add(permName);
    }
    model.value = next;
};

const toggleGroup = (perms: PermissionDTO[]) => {
    const allSelected = perms.every((p) => model.value.has(p.name));
    const next = new Set(model.value);
    if (allSelected) {
        perms.forEach((p) => next.delete(p.name));
    } else {
        perms.forEach((p) => next.add(p.name));
    }
    model.value = next;
};

const groupAllSelected = (perms: PermissionDTO[]) =>
    perms.every((p) => model.value.has(p.name));

const groupSomeSelected = (perms: PermissionDTO[]) =>
    perms.some((p) => model.value.has(p.name)) && !groupAllSelected(perms);
</script>

<template>
    <div class="space-y-3 max-h-72 overflow-y-auto pr-1">
        <div
            v-for="[group, perms] in groupedPermissions"
            :key="group"
            class="border border-gray-200 rounded-lg overflow-hidden bg-white"
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
                <span class="text-xs font-semibold text-gray-700">{{
                    formatGroup(group)
                }}</span>
                <span class="ml-auto text-xs text-gray-400">
                    {{ perms.filter((p) => model.has(p.name)).length }}/{{
                        perms.length
                    }}
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
                            model.has(perm.name)
                                ? 'bg-gray-900 border-gray-900'
                                : 'border-gray-300'
                        "
                        class="w-3.5 h-3.5 rounded border flex items-center justify-center shrink-0 transition-colors"
                        @click.prevent="toggle(perm.name)"
                    >
                        <svg
                            v-if="model.has(perm.name)"
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
                        :checked="model.has(perm.name)"
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
</template>
