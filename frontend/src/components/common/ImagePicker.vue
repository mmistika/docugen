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
import { ref } from 'vue';
import { Trash2 } from '@lucide/vue';

const model = defineModel<string | null>({ required: true });

defineProps<{
    initials: string;
}>();

const fileInputRef = ref<HTMLInputElement | null>(null);

const triggerFileInput = () => {
    fileInputRef.value?.click();
};

const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
        const file = target.files[0];
        const reader = new FileReader();
        reader.onload = (e) => {
            if (e.target?.result) {
                model.value = e.target.result as string;
            }
        };
        reader.readAsDataURL(file);
    }
};

const removePicture = () => {
    model.value = null;
    if (fileInputRef.value) {
        fileInputRef.value.value = '';
    }
};
</script>

<template>
    <div class="flex items-center gap-6">
        <div class="relative group">
            <img
                v-if="model"
                :src="model"
                alt="Profile Avatar"
                class="w-20 h-20 rounded-full object-cover border border-gray-300 shadow-sm"
            />
            <div
                v-else
                class="w-20 h-20 rounded-full bg-linear-to-tr from-gray-100 to-gray-200 flex items-center justify-center text-lg font-bold text-gray-700 border border-gray-300 shadow-sm select-none"
            >
                {{ initials }}
            </div>
            <input
                ref="fileInputRef"
                accept="image/*"
                class="hidden"
                type="file"
                @change="handleFileChange"
            />
        </div>
        <div class="flex flex-col gap-2">
            <div class="flex items-center gap-2">
                <button
                    class="px-4 py-2 border border-gray-300 hover:bg-gray-50 rounded text-sm font-medium text-gray-700 transition-colors cursor-pointer bg-white"
                    type="button"
                    @click="triggerFileInput"
                >
                    Upload picture
                </button>
                <button
                    v-if="model"
                    class="px-3 py-2 border border-red-200 hover:bg-red-50 rounded text-sm font-medium text-red-600 transition-colors cursor-pointer flex items-center gap-1.5 bg-white"
                    type="button"
                    @click="removePicture"
                >
                    <Trash2 :size="14" />
                    Remove
                </button>
            </div>
            <p class="text-xs text-gray-400">
                JPG, PNG or GIF (max size 1MB recommended)
            </p>
        </div>
    </div>
</template>
