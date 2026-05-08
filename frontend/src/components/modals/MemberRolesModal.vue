<script lang="ts" setup>
import { ref, watch } from 'vue';
import Modal from '@/components/modals/Modal.vue';
import type { Member } from '@/types/member.ts';

const props = defineProps<{
    show: boolean;
    member: Member | null;
    availableRoles: string[];
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'save', memberId: number, roles: string[]): void;
}>();

const selected = ref<Set<string>>(new Set());
const error = ref<string | null>(null);

watch(
    () => [props.show, props.member] as const,
    ([show, member]) => {
        if (!show) return;
        selected.value = new Set(member?.roles ?? []);
        error.value = null;
    }
);

const toggle = (role: string) => {
    const next = new Set(selected.value);
    next.has(role) ? next.delete(role) : next.add(role);
    selected.value = next;
};

const submit = () => {
    error.value = null;
    if (selected.value.size === 0) {
        error.value = 'At least one role must be assigned.';
        return;
    }
    emit('save', props.member!.id, [...selected.value]);
};
</script>

<template>
    <Modal
        :show="show"
        :title="
            member
                ? `Manage Roles — ${member.name ?? member.email} ${member.surname ?? ''}`
                : 'Manage Roles'
        "
        @close="emit('close')"
    >
        <template #body>
            <div class="space-y-2">
                <p class="text-xs text-gray-500 mb-3">
                    Select all roles this member should have. Changes replace
                    the current assignment.
                </p>
                <label
                    v-for="role in availableRoles"
                    :key="role"
                    :class="
                        selected.has(role)
                            ? 'border-gray-900 bg-gray-50'
                            : 'border-gray-200'
                    "
                    class="flex items-center gap-3 px-3 py-2.5 rounded border cursor-pointer transition-colors hover:bg-gray-50"
                >
                    <div
                        :class="
                            selected.has(role)
                                ? 'bg-gray-900 border-gray-900'
                                : 'border-gray-300'
                        "
                        class="w-4 h-4 rounded border flex items-center justify-center shrink-0 transition-colors"
                        @click.prevent="toggle(role)"
                    >
                        <svg
                            v-if="selected.has(role)"
                            class="w-2.5 h-2.5 text-white"
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
                        :checked="selected.has(role)"
                        class="sr-only"
                        type="checkbox"
                        @change="toggle(role)"
                    />
                    <span class="text-sm text-gray-800 font-medium">{{
                        role
                    }}</span>
                </label>
                <p v-if="error" class="mt-2 text-xs text-red-500">
                    {{ error }}
                </p>
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
                class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 transition-colors"
                @click="submit"
            >
                Save Roles
            </button>
        </template>
    </Modal>
</template>
