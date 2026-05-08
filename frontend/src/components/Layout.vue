<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { RouterLink, RouterView, useRoute } from 'vue-router';
import {
    Building2,
    ChevronDown,
    Files,
    FileText,
    Menu,
    ScrollText,
    Users,
    X
} from '@lucide/vue';
import { useAuthStore } from '@/stores/auth';
import { useOrgStore } from '@/stores/org.ts';

const route = useRoute();
const authStore = useAuthStore();
const orgStore = useOrgStore();

const mobileMenuOpen = ref(false);

const navigation = [
    //{ name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Organisations', path: '/organisations', icon: Building2 }
];

const orgNavigation = [
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
            class="bg-white border-b border-gray-300 px-4 py-3 flex items-center justify-between"
        >
            <div class="flex items-center gap-3">
                <button
                    class="lg:hidden p-2 hover:bg-gray-100 rounded"
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
                class="hidden lg:flex w-64 bg-white border-r border-gray-300 flex-col"
            >
                <nav class="p-4">
                    <div class="space-y-1 mb-4">
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
                        >
                            <component :is="item.icon" :size="18" />
                            {{ item.name }}
                        </RouterLink>
                    </div>
                    <div class="border-t border-gray-300 pt-4 mb-4">
                        <div class="relative mb-3">
                            <select
                                :value="orgStore.currentOrgId"
                                class="w-full px-3 py-2 border border-gray-300 rounded text-sm bg-white appearance-none cursor-pointer"
                                @change="
                                    orgStore.setCurrentOrg(
                                        Number(
                                            ($event.target as HTMLInputElement)
                                                .value
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
                    </div>

                    <div class="space-y-1">
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
                        >
                            <component :is="item.icon" :size="18" />
                            {{ item.name }}
                        </RouterLink>
                    </div>
                </nav>
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
                        <div class="relative mb-3">
                            <select
                                :value="orgStore.currentOrgId"
                                class="w-full px-3 py-2 border border-gray-300 rounded text-sm bg-white"
                                @change="
                                    orgStore.setCurrentOrg(
                                        Number($event.target)
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
