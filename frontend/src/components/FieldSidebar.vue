<script setup lang="ts">
import { ChevronRight, ChevronDown } from "@lucide/vue";
import { FIELD_TYPE_REGISTRY } from "@/types/field";
import FieldTypeButton from "./FieldTypeButton.vue";
import type { Field, FieldType } from "@/types/field";

defineProps<{
  globalFields: Field[];
  inlineFields: Field[];
  selectedFieldId: string | null;
  fieldsExpanded: boolean;
  inlineFieldsExpanded: boolean;
}>();

const emit = defineEmits<{
  (e: "add-global",   type: FieldType): void;
  (e: "insert-inline", type: FieldType): void;
  (e: "select-field",  id: string): void;
  (e: "update:fieldsExpanded",       v: boolean): void;
  (e: "update:inlineFieldsExpanded", v: boolean): void;
}>();
</script>

<template>
  <div class="hidden lg:flex w-64 bg-white border-r border-gray-300 flex-col overflow-y-auto">
    <div class="p-4 border-b border-gray-200 space-y-4">
      <div>
        <h3 class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
          Global Fields
        </h3>
        <div class="flex flex-col gap-1.5">
          <FieldTypeButton
            v-for="def in FIELD_TYPE_REGISTRY"
            :key="def.type"
            :def="def"
            variant="global"
            @click="emit('add-global', $event as FieldType)"
          />
        </div>
      </div>
      <div>
        <h3 class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
          Inline Embeds
        </h3>
        <div class="flex flex-col gap-1.5">
          <FieldTypeButton
            v-for="def in FIELD_TYPE_REGISTRY"
            :key="def.type"
            :def="def"
            variant="inline"
            @click="emit('insert-inline', $event as FieldType)"
          />
        </div>
      </div>
    </div>
    <div class="p-4 flex-1 overflow-y-auto space-y-4">
      <div>
        <button
          @click="emit('update:fieldsExpanded', !fieldsExpanded)"
          class="flex items-center gap-2 text-sm font-medium text-gray-900 w-full
                 hover:bg-gray-100 px-2 py-1 rounded"
        >
          <ChevronDown v-if="fieldsExpanded" :size="16" />
          <ChevronRight v-else :size="16" />
          Fields
        </button>
        <div v-if="fieldsExpanded" class="ml-4 mt-1 space-y-1">
          <p v-if="globalFields.length === 0" class="text-xs text-gray-400 italic px-2 py-1">
            No global fields added
          </p>
          <button
            v-for="field in globalFields"
            :key="field.id"
            @click="emit('select-field', field.id)"
            class="w-full text-left px-3 py-2 rounded text-xs transition-colors"
            :class="
              selectedFieldId === field.id
                ? 'bg-gray-200 text-gray-900'
                : 'text-gray-600 hover:bg-gray-100'
            "
          >
            <div class="font-medium">{{ field.name }}</div>
            <div class="text-gray-500 capitalize">{{ field.type }}</div>
          </button>
        </div>
      </div>
      <div>
        <button
          @click="emit('update:inlineFieldsExpanded', !inlineFieldsExpanded)"
          class="flex items-center gap-2 text-sm font-medium text-gray-900 w-full
                 hover:bg-gray-100 px-2 py-1 rounded"
        >
          <ChevronDown v-if="inlineFieldsExpanded" :size="16" />
          <ChevronRight v-else :size="16" />
          Inline Fields
        </button>
        <div v-if="inlineFieldsExpanded" class="ml-4 mt-1 space-y-1">
          <p v-if="inlineFields.length === 0" class="text-xs text-gray-400 italic px-2 py-1">
            No inline fields embedded
          </p>
          <button
            v-for="field in inlineFields"
            :key="field.id"
            @click="emit('select-field', field.id)"
            class="w-full text-left px-3 py-2 rounded text-xs transition-colors"
            :class="
              selectedFieldId === field.id
                ? 'bg-blue-100 text-blue-900'
                : 'text-gray-600 hover:bg-gray-100'
            "
          >
            <div class="font-medium font-mono">{{ field.name }}</div>
            <div class="text-gray-500 capitalize">{{ field.type }}</div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
