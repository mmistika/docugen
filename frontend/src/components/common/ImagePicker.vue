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
