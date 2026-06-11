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
import {
    Bold,
    Italic,
    Strikethrough,
    Underline as UnderlineIcon
} from '@lucide/vue';
import ColorButtons from './ColorButtons.vue';

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
                editor.isActive('bold')
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Bold"
            type="button"
            @click="editor.chain().focus().toggleBold().run()"
        >
            <Bold :size="15" />
        </button>
        <button
            :class="
                editor.isActive('italic')
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Italic"
            type="button"
            @click="editor.chain().focus().toggleItalic().run()"
        >
            <Italic :size="15" />
        </button>
        <button
            :class="
                editor.isActive('underline')
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Underline"
            type="button"
            @click="editor.chain().focus().toggleUnderline().run()"
        >
            <UnderlineIcon :size="15" />
        </button>
        <button
            :class="
                editor.isActive('strike')
                    ? 'bg-white shadow-xs text-black font-semibold ring-1 ring-black/20'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-white/50'
            "
            class="p-1.5 rounded-md transition-all duration-150"
            title="Strikethrough"
            type="button"
            @click="editor.chain().focus().toggleStrike().run()"
        >
            <Strikethrough :size="15" />
        </button>

        <div class="w-px h-4 bg-gray-200 mx-1" />

        <ColorButtons :editor="editor" />
    </div>
</template>
