<script lang="ts" setup>
import { Camera } from '@lucide/vue';
import { useAuthStore } from '@/stores/auth';
import TabHeader from '@/components/common/TabHeader.vue';

const authStore = useAuthStore();
const user = authStore.user;
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
                <div class="flex items-center gap-4">
                    <div class="relative">
                        <div class="w-20 h-20 bg-gray-300 rounded-full" />
                        <div
                            class="absolute bottom-0 right-0 bg-white border border-gray-300 rounded-full p-1.5 cursor-not-allowed"
                        >
                            <Camera :size="16" class="text-gray-400" />
                        </div>
                    </div>
                    <div>
                        <button
                            class="px-4 py-2 border border-gray-200 text-gray-400 rounded text-sm font-medium cursor-not-allowed"
                            disabled
                        >
                            Upload new picture
                        </button>
                        <p class="text-xs text-gray-400 mt-2">
                            JPG, PNG or GIF
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
                                class="block text-sm font-medium text-gray-700 mb-1"
                                >Name</label
                            >
                            <input
                                :value="user?.name"
                                class="w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded text-gray-500 cursor-not-allowed"
                                disabled
                                type="text"
                            />
                        </div>
                        <div>
                            <label
                                class="block text-sm font-medium text-gray-700 mb-1"
                                >Surname</label
                            >
                            <input
                                :value="user?.surname"
                                class="w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded text-gray-500 cursor-not-allowed"
                                disabled
                                type="text"
                            />
                        </div>
                    </div>
                    <div>
                        <label
                            class="block text-sm font-medium text-gray-700 mb-1"
                            >Email address</label
                        >
                        <input
                            :value="user?.email"
                            class="w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded text-gray-500 cursor-not-allowed"
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
                        class="px-4 py-2 border border-red-300 text-red-700 rounded text-sm font-medium hover:bg-red-50"
                        @click="authStore.logout()"
                    >
                        Log out
                    </button>
                </div>
            </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
            <button
                class="hidden px-4 py-2 bg-blue-600 text-white rounded text-sm font-medium opacity-50 cursor-not-allowed"
                disabled
            >
                Save changes
            </button>
        </div>
    </div>
</template>
