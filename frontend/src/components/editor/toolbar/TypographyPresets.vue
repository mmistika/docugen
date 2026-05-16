<script lang="ts" setup>
import { useEditorFormatting } from '@/composables/useEditorFormatting';

const props = defineProps<{
    editor: any;
}>();

const { getActiveHeading, setHeading } = useEditorFormatting(
    () => props.editor
);
</script>

<template>
    <select
        :value="getActiveHeading()"
        class="px-2 py-1 text-xs border border-gray-200 rounded-lg bg-white hover:bg-gray-50 hover:border-gray-300 focus:outline-none focus:ring-1 focus:ring-black transition-all duration-150 font-medium text-gray-700 cursor-pointer min-w-[110px] shrink-0"
        title="Text style"
        @change="setHeading(($event.target as HTMLSelectElement).value)"
    >
        <option value="paragraph">Normal Text</option>
        <option value="1">Heading 1</option>
        <option value="2">Heading 2</option>
        <option value="3">Heading 3</option>
    </select>
</template>
