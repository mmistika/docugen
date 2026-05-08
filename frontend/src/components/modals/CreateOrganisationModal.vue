<script lang="ts" setup>
import { ref, watch } from 'vue';
import Modal from '@/components/modals/Modal.vue';

const props = defineProps<{
    show: boolean;
    isSubmitting?: boolean;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'create', name: string): void;
}>();

const newOrgName = ref('');

watch(
    () => props.show,
    (show) => {
        if (show) {
            newOrgName.value = '';
        }
    }
);

const submit = () => {
    const nameTrimmed = newOrgName.value.trim();
    if (!nameTrimmed) return;
    emit('create', nameTrimmed);
};
</script>

<template>
    <Modal :show="show" title="Create New Organization" @close="emit('close')">
        <template #body>
            <form id="createOrgForm" class="space-y-4" @submit.prevent="submit">
                <div>
                    <label
                        class="block text-sm font-medium text-gray-700 mb-1"
                        for="orgName"
                    >
                        Organization name *
                    </label>
                    <input
                        id="orgName"
                        v-model="newOrgName"
                        :disabled="isSubmitting"
                        class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Enter organization name"
                        required
                        type="text"
                    />
                </div>
            </form>
        </template>
        <template #footer>
            <button
                :disabled="isSubmitting"
                class="px-4 py-2 border border-gray-300 rounded text-sm font-medium hover:bg-gray-50"
                type="button"
                @click="emit('close')"
            >
                Cancel
            </button>
            <button
                :disabled="isSubmitting || !newOrgName.trim()"
                class="px-4 py-2 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                form="createOrgForm"
                type="submit"
            >
                {{ isSubmitting ? 'Creating...' : 'Create Organization' }}
            </button>
        </template>
    </Modal>
</template>
