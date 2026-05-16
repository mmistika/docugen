<script lang="ts" setup>
import HistoryButtons from './toolbar/HistoryButtons.vue';
import FontPresets from './toolbar/FontPresets.vue';
import TypographyPresets from './toolbar/TypographyPresets.vue';
import FormatButtons from './toolbar/FormatButtons.vue';
import AlignmentButtons from './toolbar/AlignmentButtons.vue';
import ListButtons from './toolbar/ListButtons.vue';
import ExtraButtons from './toolbar/ExtraButtons.vue';

defineProps<{
    editor: any;
}>();
</script>

<template>
    <div
        class="bg-white border-b border-gray-300 px-4 py-1.5 flex flex-wrap items-center gap-1 shrink-0 select-none text-gray-700 shadow-sm"
    >
        <!-- Undo/Redo -->
        <HistoryButtons :editor="editor" />
        <div class="w-px h-5 bg-gray-200 mx-1" />

        <!-- Font Family/Size -->
        <FontPresets :editor="editor" />
        <div class="w-px h-5 bg-gray-200 mx-1" />

        <TypographyPresets :editor="editor" />
        <div class="w-px h-5 bg-gray-200 mx-1" />

        <!-- Bold, Italic, Underline, Strike, Color, Highlight -->
        <FormatButtons :editor="editor" />
        <div class="w-px h-5 bg-gray-200 mx-1" />

        <AlignmentButtons :editor="editor" />
        <div class="w-px h-5 bg-gray-200 mx-1" />

        <ListButtons :editor="editor" />
        <div class="w-px h-5 bg-gray-200 mx-1" />

        <!-- Quote, Horizontal, Clear -->
        <ExtraButtons :editor="editor" />
    </div>
</template>
