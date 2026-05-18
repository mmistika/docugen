<script lang="ts" setup>
import { ref } from 'vue';
import { Camera, Loader2, Save, Trash2 } from '@lucide/vue';
import { useAuthStore } from '@/stores/auth';
import TabHeader from '@/components/common/TabHeader.vue';

const authStore = useAuthStore();
const user = authStore.user;

const name = ref(user?.name || '');
const surname = ref(user?.surname || '');
const imageUrl = ref<string | null>(user?.image || null);
const imageFile = ref<File | null>(null);

const isSaving = ref(false);
const saveError = ref<string | null>(null);
const saveSuccess = ref(false);

const fileInputRef = ref<HTMLInputElement | null>(null);

const triggerFileInput = () => {
    fileInputRef.value?.click();
};

const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
        const file = target.files[0];
        imageFile.value = file;

        const reader = new FileReader();
        reader.onload = (e) => {
            if (e.target?.result) {
                imageUrl.value = e.target.result as string;
            }
        };
        reader.readAsDataURL(file);
    }
};

const removePicture = () => {
    imageUrl.value = null;
    imageFile.value = null;
    if (fileInputRef.value) {
        fileInputRef.value.value = '';
    }
};

const saveProfile = async () => {
    isSaving.value = true;
    saveError.value = null;
    saveSuccess.value = false;

    try {
        await authStore.updateProfile(
            name.value.trim(),
            surname.value.trim(),
            imageUrl.value
        );
        saveSuccess.value = true;
        setTimeout(() => {
            saveSuccess.value = false;
        }, 3000);
    } catch (err: any) {
        saveError.value = err.message || 'Failed to update profile settings.';
    } finally {
        isSaving.value = false;
    }
};

const getInitials = () => {
    const f = name.value ? name.value.charAt(0) : '';
    const l = surname.value ? surname.value.charAt(0) : '';
    return (f + l).toUpperCase() || '?';
};
</script>

<template>
    <div class="p-6 max-w-4xl mx-auto">
        <TabHeader
            description="Manage your personal information and account settings"
            title="Profile Settings"
        />
        <div class="bg-white border border-gray-300 rounded-lg">
            <div class="p-6 border-b border-gray-300">
                <h2 class="text-sm font-semibold text-gray-900 mb-4">
                    Profile Picture
                </h2>
                <div class="flex items-center gap-6">
                    <div class="relative group">
                        <img
                            v-if="imageUrl"
                            :src="imageUrl"
                            alt="Profile Avatar"
                            class="w-20 h-20 rounded-full object-cover border border-gray-300 shadow-sm"
                        />
                        <div
                            v-else
                            class="w-20 h-20 rounded-full bg-linear-to-tr from-gray-100 to-gray-200 flex items-center justify-center text-lg font-bold text-gray-700 border border-gray-300 shadow-sm select-none"
                        >
                            {{ getInitials() }}
                        </div>
                        <button
                            class="absolute bottom-0 right-0 bg-white border border-gray-300 rounded-full p-1.5 cursor-pointer shadow-sm hover:bg-gray-50 transition-colors"
                            type="button"
                            @click="triggerFileInput"
                        >
                            <Camera :size="16" class="text-gray-600" />
                        </button>
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
                                class="px-4 py-2 border border-gray-300 hover:bg-gray-50 rounded text-sm font-medium text-gray-700 transition-colors cursor-pointer"
                                type="button"
                                @click="triggerFileInput"
                            >
                                Upload new picture
                            </button>
                            <button
                                v-if="imageUrl"
                                class="px-3 py-2 border border-red-200 hover:bg-red-50 rounded text-sm font-medium text-red-600 transition-colors cursor-pointer flex items-center gap-1.5"
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
            </div>
            <div class="p-6 border-b border-gray-300">
                <h2 class="text-sm font-semibold text-gray-900 mb-4">
                    Personal Information
                </h2>
                <div class="space-y-4">
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label
                                class="block text-xs font-semibold text-gray-600 mb-1"
                                >Name</label
                            >
                            <input
                                v-model="name"
                                class="w-full px-3 py-2 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-black transition-all duration-150"
                                placeholder="Name"
                                type="text"
                            />
                        </div>
                        <div>
                            <label
                                class="block text-xs font-semibold text-gray-600 mb-1"
                                >Surname</label
                            >
                            <input
                                v-model="surname"
                                class="w-full px-3 py-2 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-black transition-all duration-150"
                                placeholder="Surname"
                                type="text"
                            />
                        </div>
                    </div>
                    <div>
                        <label
                            class="block text-xs font-semibold text-gray-600 mb-1"
                            >Email address</label
                        >
                        <input
                            :value="user?.email"
                            class="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded text-sm text-gray-400 cursor-not-allowed"
                            disabled
                            type="email"
                        />
                    </div>
                </div>
            </div>
            <div class="p-6">
                <h2 class="text-sm font-semibold text-gray-900 mb-4">
                    Session
                </h2>
                <div class="flex items-start justify-between">
                    <div>
                        <p class="text-sm text-gray-700 font-medium mb-1">
                            Log out of your account
                        </p>
                        <p class="text-xs text-gray-500">
                            You will need to log in again to access the platform
                        </p>
                    </div>
                    <button
                        class="px-4 py-2 border border-red-300 text-red-700 rounded text-sm font-medium hover:bg-red-50 cursor-pointer transition-colors"
                        type="button"
                        @click="authStore.logout()"
                    >
                        Log out
                    </button>
                </div>
            </div>
        </div>
        <div
            class="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6"
        >
            <div>
                <p
                    v-if="saveSuccess"
                    class="text-sm font-medium text-green-600"
                >
                    Profile settings saved successfully.
                </p>
                <p v-if="saveError" class="text-sm font-medium text-red-600">
                    {{ saveError }}
                </p>
            </div>
            <button
                :disabled="isSaving || !name.trim() || !surname.trim()"
                class="w-full sm:w-auto px-5 py-2.5 bg-gray-900 text-white rounded text-sm font-semibold hover:bg-gray-800 transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                type="button"
                @click="saveProfile"
            >
                <Loader2 v-if="isSaving" :size="16" class="animate-spin" />
                <Save v-else :size="16" />
                {{ isSaving ? 'Saving...' : 'Save Changes' }}
            </button>
        </div>
    </div>
</template>
