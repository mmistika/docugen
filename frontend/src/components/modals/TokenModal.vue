<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import Modal from '@/components/modals/Modal.vue';
import { AlertTriangle, Check, Clipboard } from '@lucide/vue';
import type { PermissionDTO } from '@/types/rbac.ts';

const props = defineProps<{
    show: boolean;
    allPermissions: PermissionDTO[];
    isSaving?: boolean;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (
        e: 'save',
        name: string,
        permissions: string[],
        expiresAt: string | null
    ): void;
}>();

const name = ref('');
const selectedPerms = ref<Set<string>>(new Set());
const setExpiry = ref(false);
const expiryDate = ref('');
const nameError = ref<string | null>(null);

const generatedToken = ref<string | null>(null);
const copied = ref(false);

watch(
    () => props.show,
    (show) => {
        if (!show) return;
        name.value = '';
        selectedPerms.value = new Set();
        setExpiry.value = false;
        expiryDate.value = '';
        nameError.value = null;
        generatedToken.value = null;
        copied.value = false;
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

const copyToClipboard = async () => {
    if (!generatedToken.value) return;
    try {
        await navigator.clipboard.writeText(generatedToken.value);
        copied.value = true;
        setTimeout(() => (copied.value = false), 2000);
    } catch (err) {
        console.error('Failed to copy token:', err);
    }
};

const submit = () => {
    nameError.value = null;
    if (!name.value.trim()) {
        nameError.value = 'Token name is required.';
        return;
    }
    if (selectedPerms.value.size === 0) {
        nameError.value = 'Select at least one permission.';
        return;
    }
    const expiresVal =
        setExpiry.value && expiryDate.value
            ? new Date(expiryDate.value).toISOString()
            : null;
    emit('save', name.value.trim(), [...selectedPerms.value], expiresVal);
};

defineExpose({
    setGeneratedToken(token: string) {
        generatedToken.value = token;
    }
});
</script>

<template>
    <Modal
        :show="show"
        :title="generatedToken ? 'Token Generated' : 'Generate API Token'"
        @close="emit('close')"
    >
        <template #body>
            <div v-if="generatedToken" class="space-y-4">
                <div
                    class="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-start gap-2.5 text-amber-800 text-xs"
                >
                    <AlertTriangle
                        :size="16"
                        class="shrink-0 text-amber-600 mt-0.5"
                    />
                    <div>
                        <p class="font-semibold mb-0.5">
                            Make sure to copy your API token now.
                        </p>
                        <p>
                            You won't be able to see it again for security
                            reasons.
                        </p>
                    </div>
                </div>

                <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">
                        API Token
                    </label>
                    <div class="flex gap-2">
                        <input
                            :value="generatedToken"
                            class="flex-1 px-3 py-2 border border-gray-300 rounded text-xs font-mono bg-gray-50 focus:outline-none select-all"
                            readonly
                            type="text"
                        />
                        <button
                            class="px-3 py-2 border border-gray-300 rounded hover:bg-gray-50 flex items-center justify-center shrink-0 transition-colors"
                            title="Copy to Clipboard"
                            type="button"
                            @click="copyToClipboard"
                        >
                            <Check
                                v-if="copied"
                                :size="16"
                                class="text-green-600"
                            />
                            <Clipboard
                                v-else
                                :size="16"
                                class="text-gray-600"
                            />
                        </button>
                    </div>
                </div>
            </div>

            <div v-else class="space-y-5">
                <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">
                        Application Name <span class="text-red-500">*</span>
                    </label>
                    <input
                        v-model="name"
                        :class="
                            nameError
                                ? 'border-red-400 bg-red-50'
                                : 'border-gray-300'
                        "
                        class="w-full px-3 py-2 border rounded text-sm focus:outline-none transition-colors"
                        placeholder="e.g. Telegram Bot"
                        type="text"
                    />
                    <p v-if="nameError" class="mt-1 text-xs text-red-500">
                        {{ nameError }}
                    </p>
                </div>

                <div>
                    <label class="flex items-center gap-2 cursor-pointer mb-2">
                        <input
                            v-model="setExpiry"
                            class="rounded border-gray-300 text-gray-900 focus:ring-0"
                            type="checkbox"
                        />
                        <span class="text-xs font-medium text-gray-600"
                            >Set Expiration Date</span
                        >
                    </label>
                    <input
                        v-if="setExpiry"
                        v-model="expiryDate"
                        class="w-full px-3 py-2 border border-gray-300 rounded text-sm focus:outline-none bg-white"
                        type="datetime-local"
                    />
                </div>

                <div>
                    <label class="block text-xs font-medium text-gray-600 mb-2">
                        Permissions <span class="text-red-500">*</span>
                    </label>

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
                v-if="generatedToken"
                class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 transition-colors"
                @click="emit('close')"
            >
                Done
            </button>
            <template v-else>
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
                    {{ isSaving ? 'Generating...' : 'Generate Token' }}
                </button>
            </template>
        </template>
    </Modal>
</template>
