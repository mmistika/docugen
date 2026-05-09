<script lang="ts" setup>
import { useNotificationStore } from '@/stores/notification';
import { AlertCircle, AlertTriangle, CheckCircle2, Info, X } from '@lucide/vue';

const notificationStore = useNotificationStore();
</script>

<template>
    <div
        class="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none"
    >
        <TransitionGroup
            enter-active-class="transform ease-out duration-300 transition"
            enter-from-class="translate-y-2 opacity-0 sm:translate-y-0 sm:translate-x-2"
            enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
            leave-active-class="transition ease-in duration-200"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
        >
            <div
                v-for="toast in notificationStore.toasts"
                :key="toast.id"
                :class="{
                    'border-green-500 shadow-green-950/20':
                        toast.type === 'success',
                    'border-red-500 shadow-red-950/20': toast.type === 'error',
                    'border-amber-500 shadow-amber-950/20':
                        toast.type === 'warning',
                    'border-blue-500 shadow-blue-950/20': toast.type === 'info'
                }"
                class="pointer-events-auto flex items-start gap-3 p-4 bg-gray-900 border rounded-lg shadow-xl text-white w-80 sm:w-96 select-none"
            >
                <div class="shrink-0 pt-0.5">
                    <CheckCircle2
                        v-if="toast.type === 'success'"
                        class="text-green-400 w-5 h-5"
                    />
                    <AlertCircle
                        v-else-if="toast.type === 'error'"
                        class="text-red-400 w-5 h-5"
                    />
                    <AlertTriangle
                        v-else-if="toast.type === 'warning'"
                        class="text-amber-400 w-5 h-5"
                    />
                    <Info v-else class="text-blue-400 w-5 h-5" />
                </div>

                <div
                    class="flex-1 text-sm font-medium text-gray-100 leading-snug wrap-break-word"
                >
                    {{ toast.text }}
                </div>

                <button
                    class="shrink-0 p-0.5 rounded-full hover:bg-white/10 text-gray-400 hover:text-white transition-colors cursor-pointer"
                    type="button"
                    @click="notificationStore.removeToast(toast.id)"
                >
                    <X class="w-4 h-4" />
                </button>
            </div>
        </TransitionGroup>
    </div>
</template>
