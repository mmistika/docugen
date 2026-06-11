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
import { useOrgStore } from '@/stores/org';
import { api } from '@/api/client';
import type { DashboardDataDTO } from '@/types/dashboard';
import { Files, FileText, LayoutDashboard, Users } from '@lucide/vue';
import TabHeader from '@/components/common/TabHeader.vue';

const orgStore = useOrgStore();

const dashboardData = ref<DashboardDataDTO | null>(null);
const isLoading = ref(false);
const accessDenied = ref(false);

const stats = computed(() => {
    if (!dashboardData.value) {
        return [
            {
                label: 'Total Users',
                value: '0',
                icon: Users,
                color: 'text-indigo-600 bg-indigo-50 border-indigo-100'
            },
            {
                label: 'Templates',
                value: '0',
                icon: FileText,
                color: 'text-amber-600 bg-amber-50 border-amber-100'
            },
            {
                label: 'Documents',
                value: '0',
                icon: Files,
                color: 'text-emerald-600 bg-emerald-50 border-emerald-100'
            }
        ];
    }
    return [
        {
            label: 'Total Users',
            value: Number(dashboardData.value.totalUsers).toLocaleString(),
            icon: Users,
            color: 'text-indigo-600 bg-indigo-50 border-indigo-100'
        },
        {
            label: 'Templates',
            value: Number(dashboardData.value.totalTemplates).toLocaleString(),
            icon: FileText,
            color: 'text-amber-600 bg-amber-50 border-amber-100'
        },
        {
            label: 'Documents',
            value: Number(dashboardData.value.totalDocuments).toLocaleString(),
            icon: Files,
            color: 'text-emerald-600 bg-emerald-50 border-emerald-100'
        }
    ];
});

const recentActivity = computed(() => {
    return dashboardData.value?.recentActivity ?? [];
});

const trends = computed(() => {
    const rawTrends = dashboardData.value?.usageTrends ?? [];

    // Fill in last 7 days with 0 counts if missing to show a complete chart
    const days = [];
    for (let i = 6; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        const dateStr = d.toISOString().split('T')[0]!;
        const match = rawTrends.find((t) => t.date === dateStr);
        days.push({
            date: dateStr,
            count: match ? match.count : 0
        });
    }
    return days;
});

const maxTrendCount = computed(() => {
    return Math.max(1, ...trends.value.map((t) => t.count));
});

const fetchDashboard = async () => {
    if (!orgStore.currentOrgId) return;
    isLoading.value = true;
    accessDenied.value = false;
    try {
        dashboardData.value = await api.organisations.dashboard.get(
            orgStore.currentOrgId
        );
    } catch (err: any) {
        if (err?.response?.status === 403) {
            accessDenied.value = true;
        }
        dashboardData.value = null;
    } finally {
        isLoading.value = false;
    }
};

watch(
    () => orgStore.currentOrgId,
    () => {
        fetchDashboard();
    },
    { immediate: true }
);

const formatAction = (act: string) => {
    return act
        .replace(/_/g, ' ')
        .toLowerCase()
        .replace(/\b\w/g, (c) => c.toUpperCase());
};

const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('en-GB', {
        day: '2-digit',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const formatChartDate = (dateStr: string) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-GB', {
        day: 'numeric',
        month: 'short'
    });
};

const formatRelativeTime = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHr = Math.floor(diffMin / 60);
    const diffDays = Math.floor(diffHr / 24);

    if (diffSec < 60) {
        return 'Just now';
    } else if (diffMin < 60) {
        return `${diffMin}m ago`;
    } else if (diffHr < 24) {
        return `${diffHr}h ago`;
    } else if (diffDays === 1) {
        return 'Yesterday';
    } else if (diffDays < 7) {
        return `${diffDays}d ago`;
    } else {
        return date.toLocaleDateString('en-GB', {
            day: '2-digit',
            month: 'short'
        });
    }
};
</script>

<template>
    <div class="p-4 lg:p-8">
        <TabHeader
            description="Overview of your organization stats and usage"
            title="Dashboard"
        />

        <div
            v-if="isLoading"
            class="flex flex-col items-center justify-center h-64 bg-white border border-gray-300 rounded-lg"
        >
            <div
                class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mb-2"
            ></div>
            <p class="text-sm text-gray-500">Loading statistics...</p>
        </div>

        <div
            v-else-if="accessDenied"
            class="p-6 bg-white border border-gray-300 rounded-lg text-center max-w-md mx-auto mt-8"
        >
            <LayoutDashboard :size="40" class="text-gray-400 mx-auto mb-3" />
            <h3 class="text-sm font-semibold text-gray-900 mb-1">
                Access Restrained
            </h3>
            <p class="text-xs text-gray-600 leading-relaxed mb-4">
                You do not have the permission required to read statistics for
                this organisation.
            </p>
        </div>

        <div v-else class="space-y-6">
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div
                    v-for="stat in stats"
                    :key="stat.label"
                    class="bg-white border border-gray-300 rounded-lg p-4 flex items-center justify-between"
                >
                    <div>
                        <span
                            class="text-xs text-gray-600 block mb-1 font-medium"
                            >{{ stat.label }}</span
                        >
                        <span class="text-2xl font-extrabold text-gray-900">{{
                            stat.value
                        }}</span>
                    </div>
                    <div
                        :class="stat.color"
                        class="w-10 h-10 rounded border flex items-center justify-center shrink-0"
                    >
                        <component :is="stat.icon" :size="18" />
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <!-- Usage -->
                <div
                    class="lg:col-span-2 bg-white border border-gray-300 rounded-lg p-4 flex flex-col justify-between"
                >
                    <div>
                        <h2 class="font-semibold text-gray-900 text-sm mb-1">
                            Usage Analytics
                        </h2>
                        <p class="text-xs text-gray-500 mb-4">
                            Daily document generation trend over the last 7 days
                        </p>
                    </div>

                    <div
                        class="h-56 flex items-end justify-between px-2 sm:px-6 pb-2 border-b border-gray-200 gap-1.5 sm:gap-4"
                    >
                        <div
                            v-for="trend in trends"
                            :key="trend.date"
                            class="flex flex-col items-center flex-1 group h-full justify-end"
                        >
                            <div
                                class="w-full flex justify-center mb-1 relative h-6"
                            >
                                <span
                                    class="text-[10px] text-white opacity-0 group-hover:opacity-100 transition-opacity bg-gray-900 px-1.5 py-0.5 rounded absolute bottom-0 z-10 whitespace-nowrap"
                                >
                                    {{ trend.count }} doc{{
                                        trend.count === 1 ? '' : 's'
                                    }}
                                </span>
                            </div>
                            <div
                                class="w-full flex-1 flex items-end justify-center"
                            >
                                <div
                                    :style="{
                                        height: `${(trend.count / maxTrendCount) * 100}%`
                                    }"
                                    class="w-full min-h-1 bg-gray-900 rounded-t hover:bg-gray-700 transition-all duration-300 cursor-pointer"
                                />
                            </div>
                            <span
                                class="text-[10px] text-gray-500 mt-2 font-medium text-center leading-none truncate w-full"
                            >
                                {{ formatChartDate(trend.date) }}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Recent Activity -->
                <div
                    class="bg-white border border-gray-300 rounded-lg p-4 flex flex-col"
                >
                    <h2 class="font-semibold text-gray-900 text-sm mb-1">
                        Recent Activity
                    </h2>
                    <p class="text-xs text-gray-500 mb-4">
                        Simplified feed of recent audit trail events
                    </p>

                    <div
                        v-if="recentActivity.length === 0"
                        class="flex-1 flex flex-col items-center justify-center py-8"
                    >
                        <p class="text-xs text-gray-400">
                            No activity recorded yet
                        </p>
                    </div>
                    <div
                        v-else
                        class="space-y-3 overflow-y-auto max-h-56 flex-1"
                    >
                        <div
                            v-for="activity in recentActivity"
                            :key="activity.id"
                            class="flex gap-2.5 p-2 bg-gray-50 border border-gray-100 rounded text-xs leading-relaxed"
                        >
                            <div class="flex-1 min-w-0">
                                <p class="text-gray-800 font-semibold truncate">
                                    {{ formatAction(activity.action) }}
                                </p>
                                <p
                                    class="text-gray-500 truncate flex items-center gap-1.5 flex-wrap"
                                >
                                    by
                                    <span class="font-medium text-gray-700">{{
                                        activity.userName ||
                                        activity.userEmail ||
                                        'System'
                                    }}</span>
                                    <span
                                        v-if="activity.isToken"
                                        class="inline-flex px-1.5 py-0.5 rounded text-[9px] font-semibold bg-blue-50 text-blue-400 border border-blue-100 uppercase tracking-wide"
                                        >API Token</span
                                    >
                                </p>
                            </div>
                            <span
                                :title="formatDate(activity.timestamp)"
                                class="text-[10px] text-gray-400 shrink-0 self-start text-right cursor-help"
                            >
                                {{ formatRelativeTime(activity.timestamp) }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
