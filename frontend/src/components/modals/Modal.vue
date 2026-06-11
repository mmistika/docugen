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
import { X } from '@lucide/vue';

defineProps<{
    show: boolean;
    title: string;
}>();

const emit = defineEmits(['close']);
</script>

<template>
    <Teleport to="body">
        <div
            v-if="show"
            class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
            @click.self="emit('close')"
        >
            <div class="bg-white rounded-lg max-w-md w-full shadow-xl">
                <div
                    class="flex items-center justify-between p-6 border-b border-gray-300"
                >
                    <h2 class="text-lg font-semibold text-gray-900">
                        {{ title }}
                    </h2>
                    <button
                        class="p-1 hover:bg-gray-100 rounded transition-colors"
                        @click="emit('close')"
                    >
                        <X :size="20" class="text-gray-600" />
                    </button>
                </div>
                <div class="p-6">
                    <slot name="body" />
                </div>
                <div
                    class="flex justify-end gap-3 p-6 border-t border-gray-300"
                >
                    <slot name="footer" />
                </div>
            </div>
        </div>
    </Teleport>
</template>
