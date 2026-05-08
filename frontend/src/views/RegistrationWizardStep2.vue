<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.ts';

const name = ref<string>('');

const authStore = useAuthStore();
const router = useRouter();

const MAX_LENGTH = 255;

const isFormValid = computed(() => {
    const trimmedName = name.value.trim();
    return trimmedName.length > 0 && trimmedName.length <= MAX_LENGTH;
});

const submit = async (): Promise<void> => {
    if (isFormValid.value) {
        await authStore.completeOrg(name.value);
        await router.push('/');
    }
};
</script>

<template>
    <div class="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div class="w-full max-w-md">
            <div class="text-center mb-8">
                <div class="inline-flex items-center gap-2 mb-2">
                    <img
                        alt="Docugen Logo"
                        class="w-10 h-10"
                        src="@/assets/images/docugen.svg"
                    />
                    <span class="text-2xl font-semibold text-gray-900"
                        >Docugen</span
                    >
                </div>
                <p class="text-gray-600 text-sm">
                    Document Generation Platform
                </p>
            </div>

            <div
                class="bg-white border border-gray-300 rounded-lg p-8 shadow-sm"
            >
                <h1 class="text-xl font-semibold text-gray-900 mb-2">
                    Create your organisation
                </h1>
                <p class="text-sm text-gray-500 mb-6">
                    Set up a workspace for your team and documents.
                </p>
                <form class="space-y-4" @submit.prevent="submit">
                    <div>
                        <label
                            class="block text-sm font-medium text-gray-700 mb-1"
                            for="orgName"
                        >
                            Organisation Name
                        </label>
                        <input
                            id="orgName"
                            v-model="name"
                            :maxlength="MAX_LENGTH"
                            class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Acme Corp"
                            required
                            type="text"
                        />
                        <div class="flex justify-between mt-1">
                            <p class="text-xs text-gray-500">
                                This can be changed later.
                            </p>
                            <span class="text-xs text-gray-400">
                                {{ name.length }}/{{ MAX_LENGTH }}
                            </span>
                        </div>
                    </div>

                    <button
                        :disabled="!isFormValid"
                        class="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-opacity mt-2"
                        type="submit"
                    >
                        Create Organisation
                    </button>
                </form>
            </div>
            <div class="mt-6 text-center">
                <p
                    class="text-xs text-gray-400 uppercase tracking-wider font-semibold"
                >
                    Final Step
                </p>
            </div>
        </div>
    </div>
</template>
