<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useOrgStore } from '@/stores/org.ts';
import TabHeader from '@/components/common/TabHeader.vue';
import GeneralSettingsTab from '@/components/organisation/GeneralSettingsTab.vue';
import RbacSettingsTab from '@/components/organisation/RbacSettingsTab.vue';
import ApiTokensSettingsTab from '@/components/organisation/ApiTokensSettingsTab.vue';

const route = useRoute();
const router = useRouter();
const orgStore = useOrgStore();
const orgId = computed(() => Number(route.params.id));

type Tab = 'general' | 'rbac' | 'api';
const activeTab = ref<Tab>('general');

const TABS: { key: Tab; label: string }[] = [
    { key: 'general', label: 'General' },
    { key: 'rbac', label: 'Roles & Permissions' },
    { key: 'api', label: 'API Tokens' }
];

watch(
    orgId,
    (newOrgId) => {
        if (newOrgId) {
            if (orgStore.currentOrgId !== newOrgId) {
                orgStore.setCurrentOrg(newOrgId);
            }
        }
    },
    { immediate: true }
);

watch(
    () => orgStore.currentOrgId,
    (newOrgId) => {
        if (
            newOrgId &&
            newOrgId !== orgId.value &&
            route.name === 'org-settings'
        ) {
            router.push(`/organisations/${newOrgId}/settings`);
        }
    }
);
</script>

<template>
    <div class="p-4 lg:p-8">
        <TabHeader
            :description="`ID: ${orgId}`"
            back-label="Back to Organizations"
            back-to="/organizations"
            title="Organization Settings"
        />
        <div class="border-b border-gray-300 mb-6">
            <nav class="flex gap-6">
                <button
                    v-for="tab in TABS"
                    :key="tab.key"
                    :class="
                        activeTab === tab.key
                            ? 'border-gray-900 text-gray-900'
                            : 'border-transparent text-gray-600 hover:text-gray-900'
                    "
                    class="pb-3 px-1 border-b-2 text-sm font-medium transition-colors"
                    @click="activeTab = tab.key"
                >
                    {{ tab.label }}
                </button>
            </nav>
        </div>

        <div class="mt-4">
            <GeneralSettingsTab
                v-if="activeTab === 'general'"
                :org-id="orgId"
            />

            <RbacSettingsTab v-else-if="activeTab === 'rbac'" :org-id="orgId" />

            <ApiTokensSettingsTab
                v-else-if="activeTab === 'api'"
                :org-id="orgId"
            />
        </div>
    </div>
</template>
