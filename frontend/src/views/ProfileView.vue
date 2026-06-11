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
import { computed, ref } from 'vue';
import { Loader2, Save } from '@lucide/vue';
import { useAuthStore } from '@/stores/auth';
import TabHeader from '@/components/common/TabHeader.vue';
import ImagePicker from '@/components/common/ImagePicker.vue';

const authStore = useAuthStore();
const user = authStore.user;

const name = ref(user?.name || '');
const surname = ref(user?.surname || '');
const imageUrl = ref<string | null>(user?.image || null);

const hasChanges = computed(() => {
    return (
        name.value.trim() !== (user?.name || '') ||
        surname.value.trim() !== (user?.surname || '') ||
        imageUrl.value !== (user?.image || null)
    );
});

const isSaving = ref(false);
const saveError = ref<string | null>(null);
const saveSuccess = ref(false);

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
                <ImagePicker v-model="imageUrl" :initials="getInitials()" />
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
                :disabled="
                    isSaving || !hasChanges || !name.trim() || !surname.trim()
                "
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
