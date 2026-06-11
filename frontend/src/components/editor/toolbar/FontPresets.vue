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
import { useEditorFormatting } from '@/composables/useEditorFormatting';

const props = defineProps<{
    editor: any;
}>();

const { getActiveFontFamily, setFontFamily, getActiveFontSize, setFontSize } =
    useEditorFormatting(() => props.editor);
</script>

<template>
    <div class="flex items-center gap-1.5 shrink-0">
        <select
            :value="getActiveFontFamily()"
            class="px-2 py-1 text-xs border border-gray-200 rounded-lg bg-white hover:bg-gray-50 hover:border-gray-300 focus:outline-none focus:ring-1 focus:ring-black transition-all duration-150 font-medium text-gray-700 cursor-pointer min-w-[120px]"
            title="Font Family"
            @change="setFontFamily(($event.target as HTMLSelectElement).value)"
        >
            <option value="Inter">Default (Inter)</option>
            <option value="Georgia, serif">Serif (Georgia)</option>
            <option value="'Playfair Display', serif">Playfair Display</option>
            <option value="'Fira Code', monospace">Monospace (Fira)</option>
            <option value="'Courier New', monospace">Courier New</option>
            <option value="Arial, sans-serif">Arial</option>
            <option value="'Times New Roman', serif">Times New Roman</option>
        </select>

        <select
            :value="getActiveFontSize()"
            class="px-2 py-1 text-xs border border-gray-200 rounded-lg bg-white hover:bg-gray-50 hover:border-gray-300 focus:outline-none focus:ring-1 focus:ring-black transition-all duration-150 font-medium text-gray-700 cursor-pointer w-[75px]"
            title="Font Size"
            @change="setFontSize(($event.target as HTMLSelectElement).value)"
        >
            <option value="12px">12px</option>
            <option value="14px">14px</option>
            <option value="16px">16px</option>
            <option value="18px">18px</option>
            <option value="20px">20px</option>
            <option value="24px">24px</option>
            <option value="28px">28px</option>
            <option value="32px">32px</option>
            <option value="48px">48px</option>
        </select>
    </div>
</template>
