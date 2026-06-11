<!--
Docugen — Document Generation & Management Platform
Copyright (C) 2026 Artem Bilous

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
-->

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
