<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Eye, FileText, Loader2, Wand2 } from '@lucide/vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';
import { buildInteractiveContent } from '@/utils/templateParser';
import type { Template, TemplateDetail } from '@/types/template';
import type { Field } from '@/types/field';
import TabHeader from '@/components/common/TabHeader.vue';
import DocumentCanvas from '@/components/common/DocumentCanvas.vue';
import { useDocumentValidator } from '@/composables/useDocumentValidator';

const router = useRouter();
const orgStore = useOrgStore();

const templates = ref<Template[]>([]);
const selectedId = ref<number | null>(null);
const detail = ref<TemplateDetail | null>(null);
const isLoadingDetail = ref(false);
const isGenerating = ref(false);
const submitAttempted = ref(false);

const documentName = ref('');
const fieldValues = ref<Record<string, string>>({});

const { globalFields, validationErrors, hasErrors, errorCount } =
    useDocumentValidator(
        documentName,
        fieldValues,
        computed(() => detail.value?.manifest)
    );

watch(
    () => orgStore.currentOrgId,
    async (newId) => {
        if (newId) {
            try {
                templates.value = await api.organisations.templates.all(newId);
            } catch (error) {}
        }
    },
    { immediate: true }
);

const selectTemplate = async (template: Template) => {
    if (selectedId.value === template.id) return;
    selectedId.value = template.id;
    detail.value = null;
    documentName.value = '';
    fieldValues.value = {};
    submitAttempted.value = false;

    isLoadingDetail.value = true;
    try {
        detail.value = await api.organisations.templates.getForEdit(
            orgStore.currentOrgId!,
            template.id
        );
    } finally {
        isLoadingDetail.value = false;
    }
};

const isDraft = computed(() => detail.value?.status === 'DRAFT');

const canGenerate = computed(() => !!selectedId.value && !isGenerating.value);

const fieldError = (key: string) =>
    submitAttempted.value ? (validationErrors.value[key] ?? null) : null;

const previewAreaRef = ref<HTMLElement | null>(null);

const renderedContent = computed(() =>
    detail.value?.content ? buildInteractiveContent(detail.value.content) : ''
);

const onPreviewInput = (e: Event) => {
    const target = e.target as HTMLInputElement;
    const name = target.getAttribute('data-field-name');
    if (name) fieldValues.value[name] = target.value;
};

const generate = async () => {
    submitAttempted.value = true;
    if (!selectedId.value || !orgStore.currentOrgId) return;
    if (hasErrors.value) return;

    isGenerating.value = true;
    try {
        const blob = await api.organisations.documents.generate(
            orgStore.currentOrgId,
            {
                templateId: selectedId.value,
                name: documentName.value,
                data: { ...fieldValues.value }
            }
        );

        if (isDraft.value) {
            const url = URL.createObjectURL(blob);
            window.open(url, '_blank');
            setTimeout(() => URL.revokeObjectURL(url), 15_000);
        } else {
            await router.push('/documents');
        }
    } finally {
        isGenerating.value = false;
    }
};

const inputType = (field: Field) =>
    field.type === 'number' ? 'number' : 'text';

watch(
    [submitAttempted, validationErrors],
    async () => {
        if (!previewAreaRef.value) return;
        await nextTick();
        previewAreaRef.value
            .querySelectorAll<HTMLInputElement>('[data-field-name]')
            .forEach((input) => {
                const name = input.getAttribute('data-field-name')!;
                const error = submitAttempted.value
                    ? (validationErrors.value[`inline_${name}`] ?? null)
                    : null;
                input.classList.toggle('inline-doc-input--error', !!error);
                if (error) {
                    input.setAttribute('title', error);
                } else {
                    input.removeAttribute('title');
                }
            });
    },
    { deep: true }
);
</script>

<template>
    <div class="p-4 lg:p-8 max-w-4xl mx-auto">
        <TabHeader
            back-label="Back to Documents"
            back-to="/documents"
            description="Select a template and fill in the details"
            title="Generate Document"
        />
        <div class="bg-white border border-gray-300 rounded-lg p-6 space-y-6">
            <div>
                <div class="flex items-center gap-2 mb-4">
                    <div
                        class="w-6 h-6 bg-gray-900 text-white rounded-full flex items-center justify-center text-xs font-medium"
                    >
                        1
                    </div>
                    <h2 class="font-semibold text-gray-900">Select Template</h2>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <button
                        v-for="template in templates"
                        :key="template.id"
                        :class="
                            selectedId === template.id
                                ? 'border-gray-900 bg-gray-50'
                                : 'border-gray-200 hover:border-gray-400'
                        "
                        class="p-4 border-2 rounded-lg text-left transition-colors"
                        @click="selectTemplate(template)"
                    >
                        <div class="flex items-center gap-3">
                            <div
                                class="w-10 h-10 bg-gray-100 rounded flex items-center justify-center shrink-0"
                            >
                                <FileText :size="18" class="text-gray-500" />
                            </div>
                            <div
                                class="font-medium text-sm text-gray-900 truncate"
                            >
                                {{ template.name }}
                            </div>
                        </div>
                    </button>
                </div>
            </div>

            <div class="border-t border-gray-200" />
            <div>
                <div class="flex items-center gap-2 mb-4">
                    <div
                        :class="
                            selectedId
                                ? 'bg-gray-900 text-white'
                                : 'bg-gray-200 text-gray-500'
                        "
                        class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium transition-colors"
                    >
                        2
                    </div>
                    <h2
                        :class="selectedId ? 'text-gray-900' : 'text-gray-400'"
                        class="font-semibold transition-colors"
                    >
                        Document Settings
                    </h2>
                </div>
                <div
                    v-if="isLoadingDetail"
                    class="flex items-center gap-2 text-sm text-gray-500 py-6"
                >
                    <Loader2 :size="16" class="animate-spin" /> Loading
                    template…
                </div>
                <div
                    v-else-if="detail"
                    class="grid grid-cols-1 sm:grid-cols-2 gap-4"
                >
                    <div>
                        <label
                            class="block text-xs font-medium text-gray-600 mb-1"
                        >
                            Document Name <span class="text-red-500">*</span>
                        </label>
                        <input
                            v-model="documentName"
                            :class="
                                fieldError('documentName')
                                    ? 'border-red-400 bg-red-50'
                                    : 'border-gray-300'
                            "
                            class="w-full px-3 py-2 border rounded text-sm transition-colors focus:outline-none"
                            placeholder="e.g. Invoice #INV-2026-003"
                            type="text"
                        />
                        <p
                            v-if="fieldError('documentName')"
                            class="mt-1 text-xs text-red-500"
                        >
                            {{ fieldError('documentName') }}
                        </p>
                    </div>
                    <div v-for="field in globalFields" :key="field.id">
                        <label
                            class="block text-xs font-medium text-gray-600 mb-1"
                        >
                            {{ field.name
                            }}<span
                                v-if="field.required"
                                class="text-red-500 ml-0.5"
                                >*</span
                            >
                        </label>
                        <input
                            v-model="fieldValues[field.name]"
                            :class="
                                fieldError(field.name)
                                    ? 'border-red-400 bg-red-50'
                                    : 'border-gray-300'
                            "
                            :max="field.maxValue ?? undefined"
                            :maxlength="field.maxLength ?? undefined"
                            :min="field.minValue ?? undefined"
                            :placeholder="field.name"
                            :required="field.required"
                            :type="inputType(field)"
                            class="w-full px-3 py-2 border rounded text-sm transition-colors focus:outline-none"
                        />
                        <p
                            v-if="fieldError(field.name)"
                            class="mt-1 text-xs text-red-500"
                        >
                            {{ fieldError(field.name) }}
                        </p>
                    </div>
                </div>
                <div v-else class="text-sm text-gray-400 text-center py-8">
                    Please select a template to continue
                </div>
            </div>
            <div class="border-t border-gray-200" />
            <div>
                <div class="flex items-center justify-between mb-4">
                    <div class="flex items-center gap-2">
                        <div
                            :class="
                                detail
                                    ? 'bg-gray-900 text-white'
                                    : 'bg-gray-200 text-gray-500'
                            "
                            class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium transition-colors"
                        >
                            3
                        </div>
                        <h2
                            :class="detail ? 'text-gray-900' : 'text-gray-400'"
                            class="font-semibold transition-colors"
                        >
                            Fill Document Details
                        </h2>
                    </div>
                </div>
                <div
                    v-if="submitAttempted && hasErrors"
                    class="mb-4 px-4 py-3 bg-red-50 border border-red-200 rounded text-sm text-red-700 flex items-center gap-2"
                >
                    <span class="font-medium"
                        >{{ errorCount }} field{{
                            errorCount !== 1 ? 's' : ''
                        }}
                        need attention before generating.</span
                    >
                </div>
                <div
                    v-if="detail"
                    ref="previewAreaRef"
                    class="border border-gray-200 rounded-lg overflow-hidden"
                    style="height: 70vh; min-height: 400px"
                >
                    <DocumentCanvas>
                        <div
                            class="h-full w-full prose max-w-none"
                            style="padding: 28mm 20mm"
                            @input="onPreviewInput"
                            v-html="renderedContent"
                        />
                    </DocumentCanvas>
                </div>
                <div v-else class="text-sm text-gray-400 text-center py-8">
                    Complete previous steps to preview document
                </div>
            </div>
            <div
                class="flex flex-col-reverse sm:flex-row items-center gap-3 pt-2"
            >
                <RouterLink
                    class="w-full sm:w-auto px-6 py-2 border border-gray-300 rounded text-sm text-center hover:bg-gray-50 transition-colors"
                    to="/documents"
                >
                    Cancel
                </RouterLink>
                <template v-if="isDraft">
                    <span
                        class="px-2.5 py-1 text-xs font-bold uppercase tracking-wide rounded bg-yellow-100 text-yellow-700 border border-yellow-200"
                    >
                        Draft Test Generation
                    </span>
                    <button
                        :disabled="!canGenerate"
                        class="w-full sm:w-auto ml-auto px-6 py-2 border border-gray-300 rounded text-sm hover:bg-gray-50 transition-colors flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
                        @click="generate"
                    >
                        <Loader2
                            v-if="isGenerating"
                            :size="15"
                            class="animate-spin"
                        />
                        <Eye v-else :size="15" />
                        Preview Draft
                    </button>
                </template>
                <button
                    v-else
                    :disabled="!canGenerate"
                    class="w-full sm:w-auto ml-auto px-6 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 transition-colors flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed disabled:bg-gray-700"
                    @click="generate"
                >
                    <Loader2
                        v-if="isGenerating"
                        :size="15"
                        class="animate-spin"
                    />
                    <Wand2 v-else :size="15" />
                    Generate
                </button>
            </div>
        </div>
    </div>
</template>

<style>
.inline-doc-input {
    display: inline;
    border: none;
    border-bottom: 1.5px dashed #93c5fd;
    background: transparent;
    font: inherit;
    color: #1e3a8a;
    padding: 0 1px;
    field-sizing: content;
    min-width: 2ch;
    outline: none;
    transition: border-color 0.15s;
    vertical-align: baseline;
}
.inline-doc-input:focus {
    border-bottom-color: #2563eb;
    border-bottom-style: solid;
}
.inline-doc-input::placeholder {
    color: #93c5fd;
    font-style: italic;
}
.inline-doc-input--error {
    border-bottom-color: #f87171;
    border-bottom-style: solid;
}
.inline-doc-input--error::placeholder {
    color: #fca5a5;
}
</style>
