<script setup lang="ts">
import {computed} from "vue";
import {type Field, FIELD_TYPE_MAP, type FieldType} from "@/types/field.ts";

const props = defineProps<{
  activeField: Field | null;
  isInline: boolean;
}>();

const emit = defineEmits<{
  (e: "update", key: string, value: unknown): void;
}>();

const extraProperties = computed(() =>
  props.activeField ? (FIELD_TYPE_MAP[props.activeField.type]?.properties ?? []) : []
);

const val = (key: string): unknown =>
  props.activeField ? (props.activeField as unknown as Record<string, unknown>)[key] : undefined;
</script>

<template>
  <div class="hidden lg:flex w-64 bg-white border-l border-gray-300 flex-col overflow-y-auto">
    <div class="p-4">
      <template v-if="activeField">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xs font-semibold text-gray-500 uppercase tracking-wide">
            Field Properties
          </h3>
          <span
            class="px-2 py-0.5 text-[10px] rounded font-bold uppercase tracking-wide"
            :class="isInline ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'"
          >
            {{ isInline ? "Inline" : "Global" }}
          </span>
        </div>
        <div class="space-y-4">
          <div>
            <label class="block text-xs text-gray-600 mb-1">Field Name (Variable)</label>
            <input
              type="text"
              :value="activeField.name"
              @input="emit('update', 'name', ($event.target as HTMLInputElement).value)"
              class="w-full px-3 py-1.5 border border-gray-300 rounded text-sm font-mono"
            />
          </div>
          <div>
            <label class="block text-xs text-gray-600 mb-1">Field Type</label>
            <select
              :value="activeField.type"
              @change="emit('update', 'type', ($event.target as HTMLSelectElement).value)"
              class="w-full px-3 py-1.5 border border-gray-300 rounded text-sm"
            >
              <option v-for="key in (Object.keys(FIELD_TYPE_MAP) as FieldType[])" :key="key" :value="key">
                {{ FIELD_TYPE_MAP[key].label }}
              </option>
            </select>
          </div>
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              type="checkbox"
              :checked="activeField.required"
              @change="emit('update', 'required', ($event.target as HTMLInputElement).checked)"
              class="rounded text-gray-900 focus:ring-gray-900"
            />
            <span>Required</span>
          </label>
          <template v-for="prop in extraProperties" :key="prop.key">
            <div v-if="prop.kind === 'text'">
              <label class="block text-xs text-gray-600 mb-1">{{ prop.label }}</label>
              <input
                type="text"
                :value="(val(prop.key) as string) || ''"
                :placeholder="prop.placeholder"
                @input="emit('update', prop.key, ($event.target as HTMLInputElement).value || null)"
                class="w-full px-3 py-1.5 border border-gray-300 rounded text-sm"
              />
            </div>
            <div v-else-if="prop.kind === 'number'">
              <label class="block text-xs text-gray-600 mb-1">{{ prop.label }}</label>
              <input
                type="number"
                :value="(val(prop.key) as number | null) ?? ''"
                :placeholder="prop.placeholder"
                @input="
                  emit(
                    'update',
                    prop.key,
                    ($event.target as HTMLInputElement).value
                      ? Number(($event.target as HTMLInputElement).value)
                      : null
                  )
                "
                class="w-full px-3 py-1.5 border border-gray-300 rounded text-sm"
              />
            </div>
            <div v-else-if="prop.kind === 'boolean'">
              <label class="flex items-center gap-2 text-sm cursor-pointer">
                <input
                  type="checkbox"
                  :checked="Boolean(val(prop.key))"
                  @change="emit('update', prop.key, ($event.target as HTMLInputElement).checked)"
                  class="rounded text-gray-900 focus:ring-gray-900"
                />
                <span>{{ prop.label }}</span>
              </label>
            </div>
          </template>
        </div>
      </template>
      <template v-else>
        <div class="text-center py-10">
          <p class="text-sm text-gray-500 mb-2">No field selected</p>
          <p class="text-xs text-gray-400">
            Click a field on the left or select an inline chip in the document to edit its
            properties.
          </p>
        </div>
      </template>
    </div>
  </div>
</template>
