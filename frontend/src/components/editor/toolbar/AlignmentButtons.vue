<script lang="ts" setup>
import { AlignCenter, AlignJustify, AlignLeft, AlignRight } from '@lucide/vue';

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
                editor.isActive({ textAlign: 'left' })
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Align left"
            type="button"
            @click="editor.chain().focus().setTextAlign('left').run()"
        >
            <AlignLeft :size="15" />
        </button>
        <button
            :class="
                editor.isActive({ textAlign: 'center' })
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Align center"
            type="button"
            @click="editor.chain().focus().setTextAlign('center').run()"
        >
            <AlignCenter :size="15" />
        </button>
        <button
            :class="
                editor.isActive({ textAlign: 'right' })
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Align right"
            type="button"
            @click="editor.chain().focus().setTextAlign('right').run()"
        >
            <AlignRight :size="15" />
        </button>
        <button
            :class="
                editor.isActive({ textAlign: 'justify' })
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Align justify"
            type="button"
            @click="editor.chain().focus().setTextAlign('justify').run()"
        >
            <AlignJustify :size="15" />
        </button>
    </div>
</template>
