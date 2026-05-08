<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.ts';

const name = ref<string>('');
const surname = ref<string>('');

const authStore = useAuthStore();
const router = useRouter();

const MAX_LENGTH = 25;

const isFormValid = computed(() => {
    return (
        name.value.trim().length > 0 &&
        name.value.length <= MAX_LENGTH &&
        surname.value.trim().length > 0 &&
        surname.value.length <= MAX_LENGTH
    );
});

const submit = async (): Promise<void> => {
    if (isFormValid.value) {
        await authStore.completeProfile(name.value, surname.value);
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
                    Complete your profile
                </h1>
                <p class="text-sm text-gray-500 mb-6">
                    Tell us a bit more about yourself to get started.
                </p>
                <form class="space-y-4" @submit.prevent="submit">
                    <div>
                        <label
                            class="block text-sm font-medium text-gray-700 mb-1"
                            for="name"
                        >
                            First Name
                        </label>
                        <input
                            id="name"
                            v-model="name"
                            :maxlength="MAX_LENGTH"
                            class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Jane"
                            required
                            type="text"
                        />
                        <div class="flex justify-end mt-1">
                            <span class="text-xs text-gray-400">
                                {{ name.length }}/{{ MAX_LENGTH }}
                            </span>
                        </div>
                    </div>

                    <div>
                        <label
                            class="block text-sm font-medium text-gray-700 mb-1"
                            for="surname"
                        >
                            Last Name
                        </label>
                        <input
                            id="surname"
                            v-model="surname"
                            :maxlength="MAX_LENGTH"
                            class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Doe"
                            required
                            type="text"
                        />
                        <div class="flex justify-end mt-1">
                            <span class="text-xs text-gray-400">
                                {{ surname.length }}/{{ MAX_LENGTH }}
                            </span>
                        </div>
                    </div>

                    <button
                        :disabled="!isFormValid"
                        class="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-opacity mt-2"
                        type="submit"
                    >
                        Finish Setup
                    </button>
                </form>
            </div>
            <div class="mt-6 text-center">
                <p
                    class="text-xs text-gray-400 uppercase tracking-wider font-semibold"
                >
                    Step 1 of 2
                </p>
            </div>
        </div>
    </div>
</template>
