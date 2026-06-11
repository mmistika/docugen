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
