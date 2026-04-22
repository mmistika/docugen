<script setup lang="ts">
import { Type, Hash } from "@lucide/vue";
import type { FieldTypeDef } from "@/types/field";

defineProps<{
  def: FieldTypeDef;
  variant: "global" | "inline";
}>();

const emit = defineEmits<{
  (e: "click", type: string): void;
}>();

const ICONS: Record<string, unknown> = { Type, Hash };
</script>

<template>
  <button
    @click="emit('click', def.type)"
    class="flex items-center gap-1.5 w-full px-3 py-2 rounded border text-xs transition-colors"
    :class="
      variant === 'inline'
        ? 'bg-blue-50 hover:bg-blue-100 border-blue-200 text-blue-800'
        : 'bg-gray-50 hover:bg-gray-100 border-gray-200 text-gray-700'
    "
  >
    <component :is="ICONS[def.icon]" :size="13" />
    {{ def.label }}
  </button>
</template>
