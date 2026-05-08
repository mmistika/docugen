<script lang="ts" setup>
import { Loader2 } from '@lucide/vue';

defineProps<{
    items: any[];
    headers: Array<{
        key: string;
        label: string;
    }>;
    isLoading?: boolean;
    error?: string | null;
    emptyText?: string;
}>();
</script>

<template>
    <div>
        <div
            v-if="isLoading"
            class="flex items-center justify-center gap-2 text-sm text-gray-500 py-12 bg-white border border-gray-300 rounded-lg shadow-sm"
        >
            <Loader2 :size="16" class="animate-spin text-gray-600" />
            Loading records…
        </div>

        <div
            v-else-if="error"
            class="text-sm text-red-500 text-center py-12 bg-white border border-gray-300 rounded-lg shadow-sm"
        >
            {{ error }}
        </div>

        <div
            v-else-if="items.length === 0"
            class="text-center py-12 bg-white border border-dashed border-gray-300 rounded-lg shadow-sm"
        >
            <slot name="empty">
                <p class="text-sm text-gray-500">
                    {{ emptyText ?? 'No records found.' }}
                </p>
            </slot>
        </div>

        <template v-else>
            <!-- Desktop Table -->
            <div
                class="hidden lg:block bg-white border border-gray-300 rounded-lg overflow-hidden shadow-sm"
            >
                <table class="w-full">
                    <thead class="bg-gray-50 border-b border-gray-300">
                        <tr>
                            <th
                                v-for="header in headers"
                                :key="header.key"
                                class="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider"
                            >
                                {{ header.label }}
                            </th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-200">
                        <tr
                            v-for="(item, index) in items"
                            :key="item.id ?? index"
                            class="hover:bg-gray-50 transition-colors align-middle"
                        >
                            <td
                                v-for="header in headers"
                                :key="header.key"
                                class="px-6 py-4 text-sm text-gray-900"
                            >
                                <slot
                                    :index="index"
                                    :item="item"
                                    :name="`cell:${header.key}`"
                                >
                                    {{ item[header.key] }}
                                </slot>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Mobile List -->
            <div class="lg:hidden space-y-3">
                <div
                    v-for="(item, index) in items"
                    :key="item.id ?? index"
                    class="relative bg-white border border-gray-300 rounded-lg p-4 hover:shadow-sm"
                >
                    <slot :index="index" :item="item" name="mobile-item">
                        <div class="space-y-1.5">
                            <div
                                v-for="header in headers"
                                :key="header.key"
                                class="flex justify-between items-start text-xs border-b border-gray-100 pb-1"
                            >
                                <span
                                    class="font-medium text-gray-500 uppercase tracking-wider"
                                    >{{ header.label }}</span
                                >
                                <span
                                    class="text-gray-900 font-medium text-right"
                                    >{{ item[header.key] }}</span
                                >
                            </div>
                        </div>
                    </slot>
                </div>
            </div>
        </template>
    </div>
</template>
