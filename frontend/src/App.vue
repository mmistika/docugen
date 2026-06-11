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
