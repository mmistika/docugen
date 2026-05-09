<script lang="ts" setup>
import { useAuth0 } from '@auth0/auth0-vue';
import ToastContainer from '@/components/common/ToastContainer.vue';

const auth0 = useAuth0();
</script>

<template>
    <div
        v-if="auth0.isLoading.value"
        class="flex h-screen items-center justify-center"
    >
        <div
            class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"
        />
        <span class="ml-3">Authenticating...</span>
    </div>
    <template v-else>
        <router-view />
        <ToastContainer />
    </template>
</template>
