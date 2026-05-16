<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { ZoomIn, ZoomOut } from '@lucide/vue';

const props = withDefaults(
    defineProps<{
        initialScale?: number;
    }>(),
    {
        initialScale: 0.85
    }
);

const A4_W = 794;
const A4_H = 1123;
const MIN_SCALE = 0.3;
const MAX_SCALE = 2.0;

const scale = ref(props.initialScale);

const contentRef = ref<HTMLElement | null>(null);
const contentHeight = ref(A4_H);

let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
    if (contentRef.value) {
        resizeObserver = new ResizeObserver((entries) => {
            for (const entry of entries) {
                contentHeight.value = Math.max(A4_H, entry.target.clientHeight);
            }
        });
        resizeObserver.observe(contentRef.value);
    }
});

onBeforeUnmount(() => {
    if (resizeObserver) {
        resizeObserver.disconnect();
    }
});

const sheetStyle = computed(() => ({
    width: `${A4_W}px`,
    height: `${contentHeight.value}px`,
    transform: `scale(${scale.value})`,
    transformOrigin: 'top left',
    position: 'absolute' as const
}));

const wrapperStyle = computed(() => ({
    width: `${A4_W * scale.value}px`,
    height: `${contentHeight.value * scale.value}px`,
    flexShrink: 0,
    position: 'relative' as const
}));
</script>

<template>
    <div class="flex flex-col h-full w-full overflow-hidden">
        <!-- Zoom toolbar -->
        <div
            class="bg-white border-b border-gray-300 px-4 py-2 flex items-center justify-end gap-2 shrink-0 select-none"
        >
            <button
                class="p-1 rounded hover:bg-gray-100 text-gray-500 transition-colors"
                title="Zoom out"
                type="button"
                @click="
                    scale = Math.max(
                        MIN_SCALE,
                        Math.round((scale - 0.1) * 100) / 100
                    )
                "
            >
                <ZoomOut :size="14" />
            </button>
            <input
                v-model.number="scale"
                :max="MAX_SCALE"
                :min="MIN_SCALE"
                :step="0.05"
                class="w-24 accent-gray-800 cursor-pointer"
                type="range"
            />
            <button
                class="p-1 rounded hover:bg-gray-100 text-gray-500 transition-colors"
                title="Zoom in"
                type="button"
                @click="
                    scale = Math.min(
                        MAX_SCALE,
                        Math.round((scale + 0.1) * 100) / 100
                    )
                "
            >
                <ZoomIn :size="14" />
            </button>
            <span
                class="text-xs text-gray-400 w-9 text-right tabular-nums font-mono"
            >
                {{ Math.round(scale * 100) }}%
            </span>
        </div>

        <!-- Canvas -->
        <div class="flex-1 overflow-auto bg-gray-100 p-8 flex justify-center">
            <div :style="wrapperStyle">
                <div
                    :style="sheetStyle"
                    class="bg-white border border-gray-300 shadow-md rounded-sm"
                >
                    <div
                        ref="contentRef"
                        class="w-full document-canvas-content"
                    >
                        <slot />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style>
/* Document unified styling and typography */
.document-canvas-content {
    box-sizing: border-box;
    font-family:
        'Inter',
        system-ui,
        -apple-system,
        sans-serif;
    font-size: 14px;
    line-height: 1.6;
    color: #1f2937;
}

.document-canvas-content > * {
    padding: 28mm 20mm;
    box-sizing: border-box;
    min-height: 1123px;
}

.document-canvas-content p {
    margin-top: 0;
    margin-bottom: 0.75rem;
}

.document-canvas-content h1 {
    font-size: 1.8rem;
    font-weight: 700;
    margin-top: 1.5rem;
    margin-bottom: 0.75rem;
    color: #111827;
    line-height: 1.25;
}

.document-canvas-content h2 {
    font-size: 1.4rem;
    font-weight: 600;
    margin-top: 1.25rem;
    margin-bottom: 0.5rem;
    color: #111827;
    line-height: 1.25;
}

.document-canvas-content h3 {
    font-size: 1.2rem;
    font-weight: 600;
    margin-top: 1rem;
    margin-bottom: 0.5rem;
    color: #111827;
    line-height: 1.25;
}

.document-canvas-content ul {
    list-style-type: disc;
    padding-left: 1.5rem;
    margin-top: 0;
    margin-bottom: 0.75rem;
}

.document-canvas-content ol {
    list-style-type: decimal;
    padding-left: 1.5rem;
    margin-top: 0;
    margin-bottom: 0.75rem;
}

.document-canvas-content li {
    margin-bottom: 0.25rem;
}

.document-canvas-content strong {
    font-weight: 600;
    color: #111827;
}

.document-canvas-content em {
    font-style: italic;
}

/* Prevent collapse of empty paragraphs in both editor (ProseMirror) and static view */
.document-canvas-content p:empty::before,
.document-canvas-content .ProseMirror p:empty::before {
    content: '\00a0'; /* Non-breaking space */
}
</style>
