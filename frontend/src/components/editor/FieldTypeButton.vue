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
import { Hash, Type } from '@lucide/vue';
import type { FieldTypeDef } from '@/types/field.ts';

defineProps<{
    def: FieldTypeDef;
    variant: 'global' | 'inline';
}>();

const emit = defineEmits<{
    (e: 'click', type: string): void;
}>();

const ICONS: Record<string, unknown> = { Type, Hash };
</script>

<template>
    <button
        :class="
            variant === 'inline'
                ? 'bg-blue-50 hover:bg-blue-100 border-blue-200 text-blue-800'
                : 'bg-gray-50 hover:bg-gray-100 border-gray-200 text-gray-700'
        "
        class="flex items-center gap-1.5 w-full px-3 py-2 rounded border text-xs transition-colors"
        @click="emit('click', def.type)"
    >
        <component :is="ICONS[def.icon]" :size="13" />
        {{ def.label }}
    </button>
</template>
