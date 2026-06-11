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

const { getActiveHeading, setHeading } = useEditorFormatting(
    () => props.editor
);
</script>

<template>
    <select
        :value="getActiveHeading()"
        class="px-2 py-1 text-xs border border-gray-200 rounded-lg bg-white hover:bg-gray-50 hover:border-gray-300 focus:outline-none focus:ring-1 focus:ring-black transition-all duration-150 font-medium text-gray-700 cursor-pointer min-w-[110px] shrink-0"
        title="Text style"
        @change="setHeading(($event.target as HTMLSelectElement).value)"
    >
        <option value="paragraph">Normal Text</option>
        <option value="1">Heading 1</option>
        <option value="2">Heading 2</option>
        <option value="3">Heading 3</option>
    </select>
</template>
