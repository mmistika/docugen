<script setup lang="ts">
import { X } from "@lucide/vue";

defineProps<{
  show: boolean;
  title: string;
}>();

const emit = defineEmits(['close']);
</script>

<template>
  <Teleport to="body">
    <div
        v-if="show"
        class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
        @click.self="emit('close')"
    >
      <div class="bg-white rounded-lg max-w-md w-full shadow-xl">
        <div class="flex items-center justify-between p-6 border-b border-gray-300">
          <h2 class="text-lg font-semibold text-gray-900">{{ title }}</h2>
          <button
              @click="emit('close')"
              class="p-1 hover:bg-gray-100 rounded transition-colors">
            <X :size="20" class="text-gray-600" />
          </button>
        </div>
        <div class="p-6">
          <slot name="body" />
        </div>
        <div class="flex justify-end gap-3 p-6 border-t border-gray-300">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Teleport>
</template>