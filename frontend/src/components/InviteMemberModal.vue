<script setup lang="ts">
import { ref, watch } from 'vue'
import Modal from '@/components/Modal.vue'

const props = defineProps<{
  show: boolean
  availableRoles: string[]
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'invite', email: string, role: string): void
}>()

const email      = ref('')
const role       = ref('')
const isSaving   = ref(false)
const emailError = ref<string | null>(null)
const roleError  = ref<string | null>(null)

watch(() => props.show, (show) => {
  if (!show) return
  email.value      = ''
  role.value       = props.availableRoles[0] ?? ''
  emailError.value = null
  roleError.value  = null
})

const submit = async () => {
  emailError.value = null
  roleError.value  = null

  const emailTrimmed = email.value.trim()
  if (!emailTrimmed) {
    emailError.value = 'Email is required.'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrimmed)) {
    emailError.value = 'Enter a valid email address.'
    return
  }
  if (!role.value) {
    roleError.value = 'Select a role.'
    return
  }

  isSaving.value = true
  try {
    emit('invite', emailTrimmed, role.value)
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <Modal :show="show" title="Invite Member" @close="emit('close')">
    <template #body>
      <div class="space-y-4">
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">
            Email Address <span class="text-red-500">*</span>
          </label>
          <input
              v-model="email"
              type="email"
              placeholder="user@example.com"
              class="w-full px-3 py-2 border rounded text-sm focus:outline-none transition-colors"
              :class="emailError ? 'border-red-400 bg-red-50' : 'border-gray-300'"
              @keydown.enter="submit"
          />
          <p v-if="emailError" class="mt-1 text-xs text-red-500">{{ emailError }}</p>
          <p v-else class="mt-1 text-xs text-gray-400">
            If the user isn't registered yet, their account will be pre-created automatically.
          </p>
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">
            Role <span class="text-red-500">*</span>
          </label>
          <select
              v-model="role"
              class="w-full px-3 py-2 border rounded text-sm focus:outline-none transition-colors bg-white"
              :class="roleError ? 'border-red-400 bg-red-50' : 'border-gray-300'"
          >
            <option value="" disabled>Select a role…</option>
            <option v-for="r in availableRoles" :key="r" :value="r">{{ r }}</option>
          </select>
          <p v-if="roleError" class="mt-1 text-xs text-red-500">{{ roleError }}</p>
        </div>
      </div>
    </template>
    <template #footer>
      <button
          @click="emit('close')"
          class="px-4 py-2 border border-gray-300 rounded text-sm hover:bg-gray-50 transition-colors"
      >
        Cancel
      </button>
      <button
          @click="submit"
          :disabled="isSaving"
          class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800
               transition-colors disabled:opacity-50"
      >
        Invite
      </button>
    </template>
  </Modal>
</template>