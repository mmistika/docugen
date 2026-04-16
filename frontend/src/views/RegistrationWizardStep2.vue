<script setup lang="ts">
import {computed, ref} from 'vue'
import { useRouter } from 'vue-router'
import {useAuthStore} from "@/stores/auth.ts";

const name = ref<string>('')

const authStore = useAuthStore()
const router = useRouter()

const MAX_LENGTH = 255;

const isFormValid = computed(() => {
  const trimmedName = name.value.trim();
  return trimmedName.length > 0 && trimmedName.length <= MAX_LENGTH;
});

const submit = async (): Promise<void> => {
  if (isFormValid.value) {
    await authStore.completeOrg(name.value)
    await router.push('/')
  }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center p-4">
    <div class="w-full max-w-md">

      <div class="text-center mb-8">
        <div class="inline-flex items-center gap-2 mb-2">
          <img src="@/assets/images/docugen.svg" alt="Docugen Logo" class="w-10 h-10" />
          <span class="text-2xl font-semibold text-gray-900">Docugen</span>
        </div>
        <p class="text-gray-600 text-sm">Document Generation Platform</p>
      </div>

      <div class="bg-white border border-gray-300 rounded-lg p-8 shadow-sm">
        <h1 class="text-xl font-semibold text-gray-900 mb-2">Create your organisation</h1>
        <p class="text-sm text-gray-500 mb-6">Set up a workspace for your team and documents.</p>
        <form @submit.prevent="submit" class="space-y-4">
          <div>
            <label for="orgName" class="block text-sm font-medium text-gray-700 mb-1">
              Organisation Name
            </label>
            <input
                v-model="name"
                type="text"
                id="orgName"
                required
                :maxlength="MAX_LENGTH"
                class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Acme Corp"
            />
            <div class="flex justify-between mt-1">
              <p class="text-xs text-gray-500">This can be changed later.</p>
              <span class="text-xs text-gray-400">
                {{ name.length }}/{{ MAX_LENGTH }}
              </span>
            </div>
          </div>

          <button
              type="submit"
              :disabled="!isFormValid"
              class="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-opacity mt-2"
          >
            Create Organisation
          </button>
        </form>

        <div class="mt-8 pt-6 border-t border-gray-100">
          <div class="bg-blue-50 rounded-md p-4">
            <div class="flex">
              <div class="shrink-0">
                <svg class="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm text-blue-700">
                  Have an invite? Use an invitation link to join an existing organisation instead.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer Progress -->
      <div class="mt-6 text-center">
        <p class="text-xs text-gray-400 uppercase tracking-wider font-semibold">
          Final Step
        </p>
      </div>
    </div>
  </div>
</template>