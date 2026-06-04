<script lang="ts" setup>
import { computed } from 'vue';

const page = defineModel<number>({ required: true });

const props = withDefaults(
    defineProps<{
        totalPages: number;
        totalElements: number;
        itemName?: string;
        pluralItemName?: string;
    }>(),
    {
        itemName: 'item',
        pluralItemName: 'items'
    }
);

const pageButtons = computed(() => {
    const total = props.totalPages;
    const current = page.value;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const start = Math.max(0, Math.min(current - 3, total - 7));
    return Array.from({ length: 7 }, (_, i) => start + i);
});
</script>

<template>
    <div
        class="flex items-center justify-between px-6 py-4 border-t border-gray-200 bg-gray-50"
    >
        <p class="text-xs text-gray-500">
            {{ totalElements }} total
            {{ totalElements === 1 ? itemName : pluralItemName }}
        </p>
        <div class="flex items-center gap-1">
            <button
                :disabled="page === 0"
                class="px-3 py-1 border border-gray-300 rounded text-sm hover:bg-white disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                @click="page = Math.max(0, page - 1)"
            >
                Previous
            </button>
            <button
                v-for="p in pageButtons"
                :key="p"
                :class="
                    page === p
                        ? 'bg-gray-900 text-white border-gray-900'
                        : 'border-gray-300 hover:bg-white'
                "
                class="px-3 py-1 border rounded text-sm transition-colors"
                @click="page = p"
            >
                {{ p + 1 }}
            </button>
            <button
                :disabled="page >= totalPages - 1"
                class="px-3 py-1 border border-gray-300 rounded text-sm hover:bg-white disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                @click="page = Math.min(totalPages - 1, page + 1)"
            >
                Next
            </button>
        </div>
    </div>
</template>
