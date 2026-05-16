<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { RouterLink, RouterView, useRoute } from 'vue-router';
import {
    Building2,
    ChevronDown,
    Files,
    FileText,
    LayoutDashboard,
    Menu,
    PanelLeftClose,
    PanelLeftOpen,
    ScrollText,
    Settings,
    Users,
    X
} from '@lucide/vue';
import { useAuthStore } from '@/stores/auth';
import { useOrgStore } from '@/stores/org.ts';

const route = useRoute();
const authStore = useAuthStore();
const orgStore = useOrgStore();

const mobileMenuOpen = ref(false);
const sidebarCollapsed = ref(
    localStorage.getItem('sidebar_collapsed') === 'true'
);

const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value;
    localStorage.setItem('sidebar_collapsed', String(sidebarCollapsed.value));
};

const navigation = [
    { name: 'Organisations', path: '/organisations', icon: Building2 }
];

const orgNavigation = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Members', path: '/members', icon: Users },
    { name: 'Templates', path: '/templates', icon: FileText },
    { name: 'Documents', path: '/documents', icon: Files },
    { name: 'Audit Logs', path: '/audit', icon: ScrollText }
];

onMounted(async () => {
    if (orgStore.organisations.length === 0) {
        await orgStore.fetch();
    }
});

const isActive = (path: string) => {
    return route.path.startsWith(path);
};
</script>

<template>
    <div class="h-screen flex flex-col bg-gray-50">
        <header
            class="lg:hidden bg-white border-b border-gray-300 px-4 py-3 flex items-center justify-between shrink-0"
        >
            <div class="flex items-center gap-3">
                <button
                    class="p-2 hover:bg-gray-100 rounded"
                    @click="mobileMenuOpen = !mobileMenuOpen"
                >
                    <X v-if="mobileMenuOpen" :size="20" />
                    <Menu v-else :size="20" />
                </button>
                <div class="flex items-center gap-2">
                    <img
                        alt="Docugen Logo"
                        class="w-8 h-8 rounded"
                        src="@/assets/images/docugen.svg"
                    />
                    <span class="font-semibold text-gray-900">Docugen</span>
                </div>
            </div>
            <div class="flex items-center gap-3">
                <RouterLink
                    class="flex items-center gap-3 text-sm text-gray-600"
                    to="/profile"
                >
                    <div class="flex flex-col items-end">
                        <span class="font-medium text-gray-900">
                            {{ authStore.user?.name }}
                            {{ authStore.user?.surname }}
                        </span>
                        <span class="text-xs text-gray-500">{{
                            authStore.user?.email
                        }}</span>
                    </div>
                    <div class="w-8 h-8 bg-gray-300 rounded-full shrink-0" />
                </RouterLink>
            </div>
        </header>

        <div class="flex flex-1 overflow-hidden">
            <aside
                :class="[
                    sidebarCollapsed ? 'w-16' : 'w-64',
                    'hidden lg:flex bg-white border-r border-gray-300 flex-col transition-all duration-300 ease-in-out shrink-0'
                ]"
            >
                <!-- Top -->
                <div
                    v-if="!sidebarCollapsed"
                    class="flex items-center justify-between p-4 border-b border-gray-200 shrink-0"
                >
                    <div class="flex items-center gap-2">
                        <img
                            alt="Docugen Logo"
                            class="w-8 h-8 rounded shrink-0"
                            src="@/assets/images/docugen.svg"
                        />
                        <span
                            class="font-semibold text-gray-900 whitespace-nowrap"
                            >Docugen</span
                        >
                    </div>
                    <button
                        class="p-1.5 hover:bg-gray-100 rounded text-gray-600 hover:text-gray-900 transition-colors shrink-0"
                        title="Collapse Sidebar"
                        @click="toggleSidebar"
                    >
                        <PanelLeftClose :size="18" />
                    </button>
                </div>
                <div
                    v-else
                    class="flex flex-col items-center py-4 border-b border-gray-200 shrink-0"
                >
                    <div
                        class="relative group cursor-pointer w-8 h-8 flex items-center justify-center"
                        title="Expand Sidebar"
                        @click="toggleSidebar"
                    >
                        <img
                            alt="Docugen Logo"
                            class="w-8 h-8 rounded absolute transition-opacity duration-200 group-hover:opacity-0"
                            src="@/assets/images/docugen.svg"
                        />
                        <div
                            class="absolute opacity-0 group-hover:opacity-100 transition-opacity duration-200 text-gray-600 hover:text-gray-900"
                        >
                            <PanelLeftOpen :size="18" />
                        </div>
                    </div>
                </div>

                <!-- Middle -->
                <nav class="p-3 flex-1 overflow-y-auto overflow-x-hidden">
                    <div class="space-y-1 mb-4">
                        <RouterLink
                            v-for="item in navigation"
                            :key="item.path"
                            :class="[
                                'flex items-center gap-3 px-3 py-2 rounded text-sm transition-all duration-300',
                                sidebarCollapsed ? 'justify-center px-0' : '',
                                isActive(item.path)
                                    ? 'bg-gray-200 text-gray-900 font-medium'
                                    : 'text-gray-600 hover:bg-gray-100'
                            ]"
                            :title="sidebarCollapsed ? item.name : undefined"
                            :to="item.path"
                        >
                            <component
                                :is="item.icon"
                                :size="18"
                                class="shrink-0"
                            />
                            <span
                                v-show="!sidebarCollapsed"
                                class="whitespace-nowrap"
                                >{{ item.name }}</span
                            >
                        </RouterLink>
                    </div>
                    <div class="border-t border-gray-300 pt-4 mb-4">
                        <div
                            v-if="!sidebarCollapsed"
                            class="flex items-center gap-2"
                        >
                            <div class="relative flex-1">
                                <select
                                    :value="orgStore.currentOrgId"
                                    class="w-full pr-8 pl-3 py-2 border border-gray-300 rounded text-sm bg-white appearance-none cursor-pointer"
                                    @change="
                                        orgStore.setCurrentOrg(
                                            Number(
                                                (
                                                    $event.target as HTMLInputElement
                                                ).value
                                            )
                                        )
                                    "
                                >
                                    <option
                                        v-for="org in orgStore.organisations"
                                        :key="org.id"
                                        :value="org.id"
                                    >
                                        {{ org.name }}
                                    </option>
                                </select>
                                <ChevronDown
                                    :size="16"
                                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none"
                                />
                            </div>
                            <RouterLink
                                v-if="orgStore.currentOrgId"
                                :to="`/organisations/${orgStore.currentOrgId}/settings`"
                                class="p-2 border border-gray-300 rounded hover:bg-gray-100 text-gray-500 hover:text-gray-900 transition-colors flex items-center justify-center shrink-0"
                                title="Organisation Settings"
                            >
                                <Settings :size="18" />
                            </RouterLink>
                        </div>
                        <div v-else class="flex flex-col items-center">
                            <!-- Org/Settings hover -->
                            <div
                                class="relative group w-8 h-8 flex items-center justify-center"
                            >
                                <div
                                    :title="
                                        orgStore.organisations.find(
                                            (o) =>
                                                o.id === orgStore.currentOrgId
                                        )?.name
                                    "
                                    class="w-8 h-8 rounded bg-gray-950 text-white flex items-center justify-center font-bold text-xs shadow-xs absolute transition-opacity duration-200 group-hover:opacity-0"
                                >
                                    {{
                                        orgStore.organisations
                                            .find(
                                                (o) =>
                                                    o.id ===
                                                    orgStore.currentOrgId
                                            )
                                            ?.name?.charAt(0)
                                            .toUpperCase() || 'O'
                                    }}
                                </div>
                                <RouterLink
                                    v-if="orgStore.currentOrgId"
                                    :to="`/organisations/${orgStore.currentOrgId}/settings`"
                                    class="w-8 h-8 rounded-lg bg-gray-100 text-gray-600 hover:text-gray-900 border border-gray-200 transition-opacity duration-200 flex items-center justify-center absolute opacity-0 group-hover:opacity-100 shadow-xs animate-none"
                                    title="Organisation Settings"
                                >
                                    <Settings :size="15" />
                                </RouterLink>
                            </div>
                        </div>
                    </div>

                    <div class="space-y-1">
                        <RouterLink
                            v-for="item in orgNavigation"
                            :key="item.path"
                            :class="[
                                'flex items-center gap-3 px-3 py-2 rounded text-sm transition-all duration-300',
                                sidebarCollapsed ? 'justify-center px-0' : '',
                                isActive(item.path)
                                    ? 'bg-gray-200 text-gray-900 font-medium'
                                    : 'text-gray-600 hover:bg-gray-100'
                            ]"
                            :title="sidebarCollapsed ? item.name : undefined"
                            :to="item.path"
                        >
                            <component
                                :is="item.icon"
                                :size="18"
                                class="shrink-0"
                            />
                            <span
                                v-show="!sidebarCollapsed"
                                class="whitespace-nowrap"
                                >{{ item.name }}</span
                            >
                        </RouterLink>
                    </div>
                </nav>

                <!-- Bottom -->
                <div
                    class="mt-auto border-t border-gray-300 p-2 bg-gray-50/50 shrink-0"
                >
                    <RouterLink
                        :class="sidebarCollapsed ? 'justify-center px-0' : ''"
                        :title="
                            sidebarCollapsed
                                ? `${authStore.user?.name} ${authStore.user?.surname} (${authStore.user?.email})`
                                : undefined
                        "
                        class="flex items-center gap-3 text-sm text-gray-600 rounded-lg hover:bg-gray-100 p-2 transition-all duration-200"
                        to="/profile"
                    >
                        <div
                            class="w-8 h-8 bg-gray-900 text-white rounded-full flex items-center justify-center font-semibold shrink-0 shadow-xs"
                        >
                            {{
                                authStore.user?.name?.charAt(0).toUpperCase() ||
                                'U'
                            }}
                        </div>
                        <div
                            v-show="!sidebarCollapsed"
                            class="flex flex-col min-w-0 flex-1"
                        >
                            <span class="font-medium text-gray-900 truncate">
                                {{ authStore.user?.name }}
                                {{ authStore.user?.surname }}
                            </span>
                            <span class="text-xs text-gray-500 truncate">{{
                                authStore.user?.email
                            }}</span>
                        </div>
                    </RouterLink>
                </div>
            </aside>

            <div
                v-if="mobileMenuOpen"
                class="lg:hidden absolute inset-0 z-50 bg-white"
            >
                <div class="p-4">
                    <button
                        class="p-2 hover:bg-gray-100 rounded mb-4"
                        @click="mobileMenuOpen = false"
                    >
                        <X :size="20" />
                    </button>

                    <nav class="space-y-1 mb-4">
                        <RouterLink
                            v-for="item in navigation"
                            :key="item.path"
                            :class="[
                                'flex items-center gap-3 px-3 py-2 rounded text-sm',
                                isActive(item.path)
                                    ? 'bg-gray-200 text-gray-900 font-medium'
                                    : 'text-gray-600 hover:bg-gray-100'
                            ]"
                            :to="item.path"
                            @click="mobileMenuOpen = false"
                        >
                            <component :is="item.icon" :size="18" />
                            {{ item.name }}
                        </RouterLink>
                    </nav>

                    <div class="border-t border-gray-300 pt-4 mb-4">
                        <div class="flex items-center gap-2">
                            <div class="relative flex-1">
                                <select
                                    :value="orgStore.currentOrgId"
                                    class="w-full pr-8 pl-3 py-2 border border-gray-300 rounded text-sm bg-white appearance-none cursor-pointer"
                                    @change="
                                        orgStore.setCurrentOrg(
                                            Number(
                                                (
                                                    $event.target as HTMLInputElement
                                                ).value
                                            )
                                        )
                                    "
                                >
                                    <option
                                        v-for="org in orgStore.organisations"
                                        :key="org.id"
                                        :value="org.id"
                                    >
                                        {{ org.name }}
                                    </option>
                                </select>
                                <ChevronDown
                                    :size="16"
                                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none"
                                />
                            </div>
                            <RouterLink
                                v-if="orgStore.currentOrgId"
                                :to="`/organisations/${orgStore.currentOrgId}/settings`"
                                class="p-2 border border-gray-300 rounded hover:bg-gray-100 text-gray-500 hover:text-gray-900 transition-colors flex items-center justify-center shrink-0"
                                title="Organisation Settings"
                                @click="mobileMenuOpen = false"
                            >
                                <Settings :size="18" />
                            </RouterLink>
                        </div>
                    </div>

                    <nav class="space-y-1">
                        <RouterLink
                            v-for="item in orgNavigation"
                            :key="item.path"
                            :class="[
                                'flex items-center gap-3 px-3 py-2 rounded text-sm',
                                isActive(item.path)
                                    ? 'bg-gray-200 text-gray-900 font-medium'
                                    : 'text-gray-600 hover:bg-gray-100'
                            ]"
                            :to="item.path"
                            @click="mobileMenuOpen = false"
                        >
                            <component :is="item.icon" :size="18" />
                            {{ item.name }}
                        </RouterLink>
                    </nav>
                </div>
            </div>

            <main class="flex-1 overflow-auto">
                <RouterView />
            </main>
        </div>
    </div>
</template>
