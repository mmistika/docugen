<!--
Docugen — Document Generation & Management Platform
Copyright (C) 2026 Artem Bilous

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
-->

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { CheckCircle, Eye, FileText, Plus, RotateCcw } from '@lucide/vue';
import { RouterLink } from 'vue-router';
import { useOrgStore } from '@/stores/org';
import { api } from '@/api/client';
import type { Document } from '@/types/document';
import TabHeader from '@/components/common/TabHeader.vue';
import SearchFilterBar from '@/components/common/SearchFilterBar.vue';
import DataTable from '@/components/common/DataTable.vue';
import Pagination from '@/components/common/Pagination.vue';

const orgStore = useOrgStore();

const documents = ref<Document[]>([]);
const searchQuery = ref('');
const selectedTemplate = ref<string>('');
const selectedStatus = ref<string>('');

const tableHeaders = [
    { key: 'document', label: 'Document' },
    { key: 'templateName', label: 'Template' },
    { key: 'createdAt', label: 'Date' },
    { key: 'status', label: 'Status' },
    { key: 'actions', label: 'Actions' }
];

const isLoading = ref(true);
const actionLoading = ref<Record<number, boolean>>({});

const PAGE_SIZE = 10;
const page = ref(0);
const totalPages = ref(0);
const totalElements = ref(0);

const fetch = async () => {
    if (!orgStore.currentOrgId) return;
    isLoading.value = true;
    try {
        const res = await api.organisations.documents.all(
            orgStore.currentOrgId,
            {
                page: page.value,
                size: PAGE_SIZE
            }
        );
        documents.value = res.content;
        totalPages.value = res.page.totalPages;
        totalElements.value = res.page.totalElements;
    } catch {
    } finally {
        isLoading.value = false;
    }
};

watch(
    () => orgStore.currentOrgId,
    () => {
        page.value = 0;
        fetch();
    },
    { immediate: true }
);

watch(page, fetch);

const templateName = (doc: Document): string => {
    return doc.templateName;
};

const uniqueTemplates = computed(() =>
    [...new Set(documents.value.map(templateName))].filter(Boolean).sort()
);

const uniqueStatuses = computed(() =>
    [...new Set(documents.value.map((d) => d.status))].filter(Boolean).sort()
);

const filteredDocuments = computed(() => {
    const q = searchQuery.value.toLowerCase().trim();
    return documents.value.filter((doc) => {
        const name = templateName(doc);
        const matchesSearch = !q || doc.name.toLowerCase().includes(q);
        const matchesTemplate =
            !selectedTemplate.value || name === selectedTemplate.value;
        const matchesStatus =
            !selectedStatus.value || doc.status === selectedStatus.value;
        return matchesSearch && matchesTemplate && matchesStatus;
    });
});

const viewDocument = async (doc: Document) => {
    if (!orgStore.currentOrgId) return;
    actionLoading.value[doc.id] = true;
    try {
        const blob = await api.organisations.documents.view(
            orgStore.currentOrgId,
            doc.id
        );
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 15_000);
    } finally {
        actionLoading.value[doc.id] = false;
    }
};

const finalise = async (doc: Document) => {
    if (!orgStore.currentOrgId) return;
    actionLoading.value[doc.id] = true;
    try {
        await api.organisations.documents.finalise(
            orgStore.currentOrgId,
            doc.id
        );
        const found = documents.value.find((d) => d.id === doc.id);
        if (found) found.status = 'FINAL';
    } finally {
        actionLoading.value[doc.id] = false;
    }
};

const revertToDraft = async (doc: Document) => {
    if (!orgStore.currentOrgId) return;
    actionLoading.value[doc.id] = true;
    try {
        await api.organisations.documents.revertToDraft(
            orgStore.currentOrgId,
            doc.id
        );
        const found = documents.value.find((d) => d.id === doc.id);
        if (found) found.status = 'DRAFT';
    } finally {
        actionLoading.value[doc.id] = false;
    }
};

const isFinal = (doc: Document) => doc.status === 'FINAL';

const formatDate = (date: Date | string): string => {
    const d = new Date(date);
    return d.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};
</script>

<template>
    <div class="p-4 lg:p-8">
        <TabHeader description="Generated documents library" title="Documents">
            <template #actions>
                <RouterLink
                    class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 justify-center"
                    to="/documents/generate"
                >
                    <Plus :size="16" />
                    Generate Document
                </RouterLink>
            </template>
        </TabHeader>
        <SearchFilterBar
            v-model="searchQuery"
            placeholder="Search documents..."
        >
            <template #filters>
                <select
                    v-model="selectedTemplate"
                    class="px-4 py-2 border border-gray-300 rounded text-sm bg-white"
                >
                    <option value="">All Templates</option>
                    <option v-for="t in uniqueTemplates" :key="t" :value="t">
                        {{ t }}
                    </option>
                </select>
                <select
                    v-model="selectedStatus"
                    class="px-4 py-2 border border-gray-300 rounded text-sm bg-white"
                >
                    <option value="">All Status</option>
                    <option v-for="s in uniqueStatuses" :key="s" :value="s">
                        {{ s }}
                    </option>
                </select>
            </template>
        </SearchFilterBar>
        <DataTable :headers="tableHeaders" :items="filteredDocuments">
            <!-- Desktop -->
            <template #cell:document="{ item }">
                <div class="flex items-center gap-3">
                    <div
                        class="w-8 h-8 bg-gray-100 rounded flex items-center justify-center shrink-0"
                    >
                        <FileText :size="16" class="text-gray-500" />
                    </div>
                    <span class="font-medium text-sm text-gray-900">{{
                        item.name
                    }}</span>
                </div>
            </template>
            <template #cell:createdAt="{ item }">
                {{ formatDate(item.createdAt) }}
            </template>
            <template #cell:status="{ item }">
                <span
                    :class="
                        isFinal(item)
                            ? 'bg-green-100 text-green-800'
                            : 'bg-yellow-100 text-yellow-800'
                    "
                    class="inline-flex px-2 py-1 text-xs rounded-full font-medium"
                >
                    {{ item.status }}
                </span>
            </template>
            <template #cell:actions="{ item }">
                <div class="flex items-center gap-1">
                    <button
                        :disabled="!!actionLoading[item.id]"
                        class="p-1.5 hover:bg-gray-100 rounded transition-colors disabled:opacity-40"
                        title="View PDF"
                        @click="viewDocument(item)"
                    >
                        <Eye :size="15" class="text-gray-600" />
                    </button>
                    <button
                        v-if="!isFinal(item)"
                        :disabled="!!actionLoading[item.id]"
                        class="p-1.5 hover:bg-green-50 rounded transition-colors disabled:opacity-40"
                        title="Finalise document"
                        @click="finalise(item)"
                    >
                        <CheckCircle :size="15" class="text-green-600" />
                    </button>
                    <button
                        v-if="isFinal(item)"
                        :disabled="!!actionLoading[item.id]"
                        class="p-1.5 hover:bg-yellow-50 rounded transition-colors disabled:opacity-40"
                        title="Revert to draft"
                        @click="revertToDraft(item)"
                    >
                        <RotateCcw :size="15" class="text-yellow-600" />
                    </button>
                </div>
            </template>

            <!-- Mobile -->
            <template #mobile-item="{ item }">
                <div class="flex items-start gap-3 mb-3">
                    <div
                        class="w-10 h-10 bg-gray-100 rounded flex items-center justify-center shrink-0"
                    >
                        <FileText :size="18" class="text-gray-500" />
                    </div>
                    <div class="flex-1 min-w-0">
                        <h3
                            class="font-medium text-sm text-gray-900 mb-0.5 truncate"
                        >
                            {{ item.name }}
                        </h3>
                        <p class="text-xs text-gray-500">
                            {{ item.templateName }}
                        </p>
                    </div>
                    <span
                        :class="
                            isFinal(item)
                                ? 'bg-green-100 text-green-800'
                                : 'bg-yellow-100 text-yellow-800'
                        "
                        class="shrink-0 inline-flex px-2 py-1 text-xs rounded-full font-medium"
                    >
                        {{ item.status }}
                    </span>
                </div>
                <p class="text-xs text-gray-400 mb-3">
                    {{ formatDate(item.createdAt) }}
                </p>
                <div class="flex gap-2 pt-3 border-t border-gray-100">
                    <button
                        :disabled="!!actionLoading[item.id]"
                        class="flex-1 px-3 py-1.5 border border-gray-300 rounded text-xs hover:bg-gray-50 flex items-center justify-center gap-1.5 disabled:opacity-40 transition-colors"
                        @click="viewDocument(item)"
                    >
                        <Eye :size="13" />
                        View
                    </button>
                    <button
                        v-if="!isFinal(item)"
                        :disabled="!!actionLoading[item.id]"
                        class="flex-1 px-3 py-1.5 border border-green-300 text-green-700 rounded text-xs hover:bg-green-50 flex items-center justify-center gap-1.5 disabled:opacity-40 transition-colors"
                        @click="finalise(item)"
                    >
                        <CheckCircle :size="13" />
                        Finalise
                    </button>
                    <button
                        v-if="isFinal(item)"
                        :disabled="!!actionLoading[item.id]"
                        class="flex-1 px-3 py-1.5 border border-yellow-300 text-yellow-700 rounded text-xs hover:bg-yellow-50 flex items-center justify-center gap-1.5 disabled:opacity-40 transition-colors"
                        @click="revertToDraft(item)"
                    >
                        <RotateCcw :size="13" />
                        Draft Back
                    </button>
                </div>
            </template>
        </DataTable>
        <Pagination
            v-model="page"
            :total-elements="totalElements"
            :total-pages="totalPages"
            item-name="document"
            plural-item-name="documents"
        />
    </div>
</template>
