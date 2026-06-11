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
import { ArrowLeft } from '@lucide/vue';
import { RouterLink } from 'vue-router';

defineProps<{
    title: string;
    description?: string;
    backTo?: string;
    backLabel?: string;
}>();
</script>

<template>
    <div class="mb-6 flex flex-row items-center justify-between gap-4">
        <div class="flex-1 min-w-0">
            <RouterLink
                v-if="backTo"
                :to="backTo"
                class="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-4 transition-colors"
            >
                <ArrowLeft :size="16" />
                {{ backLabel ?? 'Back' }}
            </RouterLink>
            <h1
                class="text-2xl font-bold text-gray-900 tracking-tight leading-8 mb-1"
            >
                {{ title }}
            </h1>
            <p v-if="description" class="text-sm text-gray-600">
                {{ description }}
            </p>
        </div>
        <div v-if="$slots.actions" class="flex shrink-0 gap-2 items-center">
            <slot name="actions" />
        </div>
    </div>
</template>
