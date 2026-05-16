<script lang="ts" setup>
import { Eraser, Minus, Quote } from '@lucide/vue';

defineProps<{
    editor: any;
}>();
</script>

<template>
    <div
        class="flex items-center gap-0.5 bg-gray-50 p-0.5 rounded-lg border border-gray-200 shrink-0"
    >
        <button
            :class="
                editor.isActive('blockquote')
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Blockquote"
            type="button"
            @click="editor.chain().focus().toggleBlockquote().run()"
        >
            <Quote :size="15" />
        </button>
        <button
            class="p-1.5 rounded-md hover:bg-white hover:shadow-xs transition-all duration-150 text-gray-600 hover:text-gray-900"
            title="Horizontal Divider"
            type="button"
            @click="editor.chain().focus().setHorizontalRule().run()"
        >
            <Minus :size="15" />
        </button>
        <button
            class="p-1.5 rounded-md hover:bg-red-50 hover:shadow-xs transition-all duration-150 text-red-500 hover:text-red-700"
            title="Clear Formatting"
            type="button"
            @click="editor.chain().focus().unsetAllMarks().clearNodes().run()"
        >
            <Eraser :size="15" />
        </button>
    </div>
</template>
