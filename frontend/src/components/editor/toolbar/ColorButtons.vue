<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useEditorFormatting } from '@/composables/useEditorFormatting';
import { Baseline, Highlighter } from '@lucide/vue';
import ColorPicker from './ColorPicker.vue';

const props = defineProps<{
    editor: any;
}>();

const {
    getActiveColor,
    setTextColor,
    resetTextColor,
    getActiveHighlight,
    setHighlightColor,
    resetHighlightColor
} = useEditorFormatting(() => props.editor);

const TEXT_COLORS = [
    { name: 'Dark Navy', value: '#1e3a8a' },
    { name: 'Royal Blue', value: '#2563eb' },
    { name: 'Emerald Green', value: '#059669' },
    { name: 'Crimson Red', value: '#dc2626' },
    { name: 'Warm Amber', value: '#d97706' },
    { name: 'Purple Reign', value: '#7c3aed' },
    { name: 'Charcoal', value: '#374151' },
    { name: 'Soft Gray', value: '#9ca3af' }
];

const HIGHLIGHT_COLORS = [
    { name: 'Yellow Highlight', value: '#fef08a' },
    { name: 'Green Highlight', value: '#bbf7d0' },
    { name: 'Blue Highlight', value: '#bfdbfe' },
    { name: 'Pink Highlight', value: '#fbcfe8' },
    { name: 'Purple Highlight', value: '#e9d5ff' },
    { name: 'Orange Highlight', value: '#fed7aa' },
    { name: 'Gray Highlight', value: '#e5e7eb' },
    { name: 'Dark Charcoal Highlight', value: '#d1d5db' }
];

const isColorPickerOpen = ref(false);
const isHighlightPickerOpen = ref(false);

const closePickers = (e: MouseEvent) => {
    const target = e.target as HTMLElement;
    if (!target.closest('.relative')) {
        isColorPickerOpen.value = false;
        isHighlightPickerOpen.value = false;
    }
};

onMounted(() => {
    window.addEventListener('click', closePickers);
});

onBeforeUnmount(() => {
    window.removeEventListener('click', closePickers);
});

function throttle<T extends (...args: any[]) => void>(
    fn: T,
    delay: number
): (...args: Parameters<T>) => void {
    let timer: any = null;
    let lastCall = 0;

    return function (this: any, ...args: Parameters<T>) {
        const now = Date.now();
        const remaining = delay - (now - lastCall);

        clearTimeout(timer);

        if (remaining <= 0) {
            lastCall = now;
            fn.apply(this, args);
        } else {
            timer = setTimeout(() => {
                lastCall = Date.now();
                fn.apply(this, args);
            }, remaining);
        }
    };
}

const throttledSetTextColor = throttle((color: string) => {
    setTextColor(color);
}, 100);

const throttledSetHighlightColor = throttle((color: string) => {
    setHighlightColor(color);
}, 100);
</script>

<template>
    <div class="flex items-center gap-0.5">
        <div class="relative">
            <button
                class="p-1.5 rounded-md hover:bg-white hover:shadow-xs transition-all duration-150 text-gray-600 hover:text-gray-900 flex flex-col items-center justify-center relative shrink-0"
                title="Text Color"
                type="button"
                @click.stop="
                    isColorPickerOpen = !isColorPickerOpen;
                    isHighlightPickerOpen = false;
                "
            >
                <Baseline :size="15" />
                <span
                    :style="{ backgroundColor: getActiveColor() }"
                    class="absolute bottom-1 left-1.5 right-1.5 h-0.5 rounded-full"
                />
            </button>
            <ColorPicker
                v-if="isColorPickerOpen"
                :colors="TEXT_COLORS"
                :value="getActiveColor()"
                label="Reset to Default"
                @change="throttledSetTextColor"
                @reset="
                    () => {
                        resetTextColor();
                        isColorPickerOpen = false;
                    }
                "
                @select="
                    (color) => {
                        setTextColor(color);
                        isColorPickerOpen = false;
                    }
                "
            />
        </div>

        <div class="relative">
            <button
                class="p-1.5 rounded-md hover:bg-white hover:shadow-xs transition-all duration-150 text-gray-600 hover:text-gray-900 flex flex-col items-center justify-center relative shrink-0"
                title="Highlight Color"
                type="button"
                @click.stop="
                    isHighlightPickerOpen = !isHighlightPickerOpen;
                    isColorPickerOpen = false;
                "
            >
                <Highlighter :size="15" />
                <span
                    :style="{ backgroundColor: getActiveHighlight() }"
                    class="absolute bottom-1 left-1.5 right-1.5 h-0.5 rounded-full"
                />
            </button>
            <ColorPicker
                v-if="isHighlightPickerOpen"
                :colors="HIGHLIGHT_COLORS"
                :value="getActiveHighlight()"
                label="No Highlight"
                @change="throttledSetHighlightColor"
                @reset="
                    () => {
                        resetHighlightColor();
                        isHighlightPickerOpen = false;
                    }
                "
                @select="
                    (color) => {
                        setHighlightColor(color);
                        isHighlightPickerOpen = false;
                    }
                "
            />
        </div>
    </div>
</template>
