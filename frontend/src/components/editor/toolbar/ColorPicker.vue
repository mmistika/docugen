<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps<{
    value: string;
    colors: Array<{ name: string; value: string }>;
    label: string;
}>();

defineEmits<{
    (e: 'select', color: string): void;
    (e: 'change', color: string): void;
    (e: 'reset'): void;
}>();

const customColorVal = computed(() => {
    if (props.value === 'transparent') {
        return '#ffff00';
    }
    return props.value || '#000000';
});

const previewColorVal = computed(() => {
    if (props.value === 'transparent') {
        return '#ffffff';
    }
    return props.value || '#000000';
});
</script>

<template>
    <div
        class="absolute left-0 mt-1 p-2 bg-white border border-gray-200 rounded-lg shadow-xl z-50 w-36 animate-none"
        @click.stop
    >
        <div class="grid grid-cols-4 gap-1">
            <button
                v-for="color in colors"
                :key="color.value"
                :style="{ backgroundColor: color.value }"
                :title="color.name"
                class="w-6 h-6 rounded border border-gray-200 hover:scale-110 transition-transform cursor-pointer shadow-2xs"
                type="button"
                @click="$emit('select', color.value)"
            />
        </div>

        <div
            class="flex items-center justify-between gap-2 border-t border-gray-100 pt-2 mt-2"
        >
            <span
                class="text-[10px] text-gray-500 font-semibold tracking-wider uppercase"
                >Custom</span
            >
            <div
                class="relative w-8 h-6 rounded border border-gray-200 overflow-hidden cursor-pointer bg-gray-50 flex items-center justify-center hover:bg-gray-100 shadow-2xs"
            >
                <input
                    :value="customColorVal"
                    class="absolute inset-0 opacity-0 w-full h-full cursor-pointer"
                    type="color"
                    @input="
                        $emit(
                            'change',
                            ($event.target as HTMLInputElement).value
                        )
                    "
                />
                <div
                    :style="{ backgroundColor: previewColorVal }"
                    class="w-4 h-4 rounded-sm border border-gray-300"
                />
            </div>
        </div>

        <button
            class="w-full mt-2 text-[10px] text-gray-500 py-1 bg-gray-50 hover:bg-gray-100 rounded border border-gray-200 transition-colors"
            type="button"
            @click="$emit('reset')"
        >
            {{ label }}
        </button>
    </div>
</template>
