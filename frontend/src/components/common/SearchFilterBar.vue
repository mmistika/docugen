<script lang="ts" setup>
import { Search, X } from '@lucide/vue';

const model = defineModel<string>({ required: true });

withDefaults(
    defineProps<{
        placeholder?: string;
    }>(),
    {
        placeholder: 'Search...'
    }
);
</script>

<template>
    <div class="bg-white border border-gray-300 rounded-lg p-4 mb-6">
        <div class="flex flex-col sm:flex-row gap-3">
            <div class="flex-1 relative">
                <Search
                    :size="16"
                    class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                />
                <input
                    v-model="model"
                    :placeholder="placeholder"
                    class="w-full pl-9 pr-9 py-2 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-gray-400 transition-all"
                    type="text"
                />
                <button
                    v-if="model"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors p-0.5 rounded-full hover:bg-gray-100"
                    title="Clear search"
                    type="button"
                    @click="model = ''"
                >
                    <X :size="14" />
                </button>
            </div>
            <div
                v-if="$slots.filters"
                class="flex flex-col sm:flex-row gap-2 shrink-0"
            >
                <slot name="filters" />
            </div>
        </div>
    </div>
</template>
