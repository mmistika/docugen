<script lang="ts" setup>
import { List, ListOrdered } from '@lucide/vue';

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
                editor.isActive('bulletList')
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Bullet list"
            type="button"
            @click="editor.chain().focus().toggleBulletList().run()"
        >
            <List :size="15" />
        </button>
        <button
            :class="
                editor.isActive('orderedList')
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Numbered list"
            type="button"
            @click="editor.chain().focus().toggleOrderedList().run()"
        >
            <ListOrdered :size="15" />
        </button>
    </div>
</template>
