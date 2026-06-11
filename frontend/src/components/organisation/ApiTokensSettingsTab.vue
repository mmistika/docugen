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
import { ref, watch } from 'vue';
import { Key, Plus, Trash2 } from '@lucide/vue';
import { api } from '@/api/client';
import TokenModal from '@/components/modals/TokenModal.vue';
import DataTable from '@/components/common/DataTable.vue';
import type { PermissionDTO } from '@/types/rbac';
import type { ApiTokenDTO } from '@/types/tokens';

const props = defineProps<{
    orgId: number;
}>();

const apiTokens = ref<ApiTokenDTO[]>([]);
const allPermissions = ref<PermissionDTO[]>([]);
const apiTokensLoading = ref(false);
const showTokenModal = ref(false);
const isGeneratingToken = ref(false);
const tokenModalRef = ref<any>(null);

const tokenHeaders = [
    { key: 'name', label: 'App Name' },
    { key: 'permissions', label: 'Permissions' },
    { key: 'createdAt', label: 'Created' },
    { key: 'expiresAt', label: 'Expires' },
    { key: 'actions', label: 'Actions' }
];

const fetchApiTokensAndPermissions = async () => {
    apiTokensLoading.value = true;
    try {
        const [tokensData, permissionsData] = await Promise.all([
            api.organisations.tokens.list(props.orgId),
            api.organisations.rbac.permissions(props.orgId)
        ]);
        apiTokens.value = tokensData;
        allPermissions.value = permissionsData;
    } catch (err) {
        console.error('Failed to fetch API tokens/permissions:', err);
    } finally {
        apiTokensLoading.value = false;
    }
};

const handleCreateToken = async (
    tokenName: string,
    permissions: string[],
    expiresAt: string | null
) => {
    isGeneratingToken.value = true;
    try {
        const res = await api.organisations.tokens.create(props.orgId, {
            name: tokenName,
            permissions,
            expiresAt
        });
        tokenModalRef.value?.setGeneratedToken(res.rawToken);
        // Refresh token list
        apiTokens.value = await api.organisations.tokens.list(props.orgId);
    } catch (err) {
        console.error('Failed to generate API token:', err);
    } finally {
        isGeneratingToken.value = false;
    }
};

const handleDeleteToken = async (token: ApiTokenDTO) => {
    if (
        !confirm(
            `Delete API token "${token.name}"? Applications using this token will lose access immediately.`
        )
    )
        return;
    try {
        await api.organisations.tokens.delete(props.orgId, token.id);
        apiTokens.value = apiTokens.value.filter((t) => t.id !== token.id);
    } catch (err) {
        console.error('Failed to delete API token:', err);
    }
};

const isExpired = (dateStr: string | null) => {
    if (!dateStr) return false;
    return new Date(dateStr) < new Date();
};

const formatTokenDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
};

watch(
    () => props.orgId,
    (newOrgId) => {
        if (newOrgId) {
            fetchApiTokensAndPermissions();
        }
    },
    { immediate: true }
);
</script>

<template>
    <div class="space-y-6">
        <div class="flex items-start justify-between mb-6 gap-4">
            <p class="text-sm text-gray-600 max-w-lg">
                Generate API tokens for applications to access this
                organization's resources programmatically. API tokens are
                restricted to getting templates and generating documents.
            </p>
            <button
                class="shrink-0 px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 transition-colors"
                @click="showTokenModal = true"
            >
                <Plus :size="16" />
                Generate Token
            </button>
        </div>

        <DataTable
            :headers="tokenHeaders"
            :is-loading="apiTokensLoading"
            :items="apiTokens"
            empty-text="No API tokens generated yet."
        >
            <!-- Desktop -->
            <template #cell:name="{ item }">
                <div class="flex items-center gap-3">
                    <div
                        :class="
                            isExpired(item.expiresAt)
                                ? 'bg-red-50'
                                : 'bg-gray-100'
                        "
                        class="w-8 h-8 rounded flex items-center justify-center shrink-0"
                    >
                        <Key
                            :class="
                                isExpired(item.expiresAt)
                                    ? 'text-red-500'
                                    : 'text-gray-500'
                            "
                            :size="16"
                        />
                    </div>
                    <span
                        :class="
                            isExpired(item.expiresAt)
                                ? 'text-red-600 font-semibold'
                                : 'text-gray-900 font-medium'
                        "
                        class="text-sm"
                    >
                        {{ item.name }}
                        <br />
                        <span
                            v-if="isExpired(item.expiresAt)"
                            class="px-1.5 py-0.5 text-[10px] font-bold bg-red-100 text-red-700 rounded border border-red-200 uppercase"
                            >Expired</span
                        >
                    </span>
                </div>
            </template>

            <template #cell:permissions="{ item }">
                <div class="flex flex-wrap gap-1 max-w-120">
                    <span
                        v-for="perm in item.permissions"
                        :key="perm"
                        class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-700 border border-gray-200"
                    >
                        {{ perm }}
                    </span>
                </div>
            </template>

            <template #cell:createdAt="{ item }">
                <span class="text-sm text-gray-600">{{
                    formatTokenDate(item.createdAt)
                }}</span>
            </template>

            <template #cell:expiresAt="{ item }">
                <span
                    :class="
                        isExpired(item.expiresAt)
                            ? 'text-red-600'
                            : 'text-gray-600'
                    "
                    class="text-sm"
                >
                    {{
                        item.expiresAt
                            ? formatTokenDate(item.expiresAt)
                            : 'Never'
                    }}
                </span>
            </template>

            <template #cell:actions="{ item }">
                <div class="flex items-center gap-2">
                    <button
                        class="p-1.5 hover:bg-red-50 rounded transition-colors"
                        title="Delete token"
                        @click="handleDeleteToken(item)"
                    >
                        <Trash2 :size="16" class="text-red-500" />
                    </button>
                </div>
            </template>

            <!-- Mobile -->
            <template #mobile-item="{ item }">
                <div class="flex items-start gap-3 mb-3">
                    <div
                        :class="
                            isExpired(item.expiresAt)
                                ? 'bg-red-50'
                                : 'bg-gray-100'
                        "
                        class="w-10 h-10 rounded flex items-center justify-center shrink-0"
                    >
                        <Key
                            :class="
                                isExpired(item.expiresAt)
                                    ? 'text-red-500'
                                    : 'text-gray-500'
                            "
                            :size="18"
                        />
                    </div>
                    <div class="flex-1 min-w-0">
                        <h3
                            :class="
                                isExpired(item.expiresAt)
                                    ? 'text-red-600 font-bold'
                                    : 'text-gray-900 font-semibold'
                            "
                            class="text-sm mb-1"
                        >
                            {{ item.name }}
                            <span
                                v-if="isExpired(item.expiresAt)"
                                class="ml-1.5 px-1.5 py-0.2 text-[10px] font-bold bg-red-100 text-red-700 rounded border border-red-200 uppercase"
                                >Expired</span
                            >
                        </h3>
                        <div class="flex flex-wrap gap-1 mb-2">
                            <span
                                v-for="perm in item.permissions"
                                :key="perm"
                                class="inline-flex px-1.5 py-0.5 rounded text-[10px] font-medium bg-gray-100 text-gray-700 border border-gray-200"
                            >
                                {{ perm }}
                            </span>
                        </div>
                        <div class="text-[10px] text-gray-500 space-y-0.5">
                            <div>
                                Created:
                                {{ formatTokenDate(item.createdAt) }}
                            </div>
                            <div>
                                Expires:
                                {{
                                    item.expiresAt
                                        ? formatTokenDate(item.expiresAt)
                                        : 'Never'
                                }}
                            </div>
                        </div>
                    </div>
                </div>
                <div class="flex gap-2 pt-3 border-t border-gray-200">
                    <button
                        class="flex-1 px-3 py-1.5 border border-red-300 text-red-600 rounded text-xs hover:bg-red-50 flex items-center justify-center gap-1.5 transition-colors font-medium"
                        @click="handleDeleteToken(item)"
                    >
                        <Trash2 :size="14" />
                        Delete
                    </button>
                </div>
            </template>
        </DataTable>

        <TokenModal
            ref="tokenModalRef"
            :all-permissions="allPermissions"
            :is-saving="isGeneratingToken"
            :show="showTokenModal"
            @close="showTokenModal = false"
            @save="handleCreateToken"
        />
    </div>
</template>
