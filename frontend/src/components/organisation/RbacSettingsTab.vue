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
import { Lock, Pencil, Plus, Shield, Trash2 } from '@lucide/vue';
import { api } from '@/api/client';
import RoleModal from '@/components/modals/RoleModal.vue';
import type { PermissionDTO, RoleDTO } from '@/types/rbac';

const props = defineProps<{
    orgId: number;
}>();

const roles = ref<RoleDTO[]>([]);
const allPermissions = ref<PermissionDTO[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const showModal = ref(false);
const editTarget = ref<RoleDTO | null>(null);
const isSavingRole = ref(false);

const fetchRbacSettings = async () => {
    isLoading.value = true;
    error.value = null;
    try {
        roles.value = await api.organisations.rbac.roles(props.orgId);
        allPermissions.value = await api.organisations.rbac.permissions(
            props.orgId
        );
    } catch (err) {
        console.error('Failed to fetch roles & permissions in settings:', err);
        error.value = 'Failed to load roles and permissions.';
    } finally {
        isLoading.value = false;
    }
};

const isAdmin = (role: RoleDTO) => role.name === 'ADMIN';

const formatPerm = (name: string) => {
    const part = name.includes(':') ? name.split(':')[1] : name;
    return part!.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
};

const formatGroup = (name: string) =>
    name.charAt(0).toUpperCase() + name.slice(1);

const permissionGroups = computed(() => {
    const groups = new Map<string, PermissionDTO[]>();
    for (const p of allPermissions.value) {
        const group = p.name.includes(':') ? p.name.split(':')[0] : 'other';
        if (!groups.has(group!)) groups.set(group!, []);
        groups.get(group!)!.push(p);
    }
    return groups;
});

const openCreate = () => {
    editTarget.value = null;
    showModal.value = true;
};

const openEdit = (role: RoleDTO) => {
    editTarget.value = role;
    showModal.value = true;
};

const closeModal = () => {
    showModal.value = false;
    editTarget.value = null;
};

const saveRole = async (name: string, permissions: string[]) => {
    isSavingRole.value = true;
    try {
        if (editTarget.value) {
            await api.organisations.rbac.updateRole(props.orgId, {
                name,
                permissions
            });
            const found = roles.value.find(
                (r) => r.name === editTarget.value!.name
            );
            if (found) found.permissions = permissions;
        } else {
            await api.organisations.rbac.createRole(props.orgId, {
                name,
                permissions
            });
            roles.value = await api.organisations.rbac.roles(props.orgId);
        }
        closeModal();
    } catch (error) {
        console.error('Failed to save role:', error);
    } finally {
        isSavingRole.value = false;
    }
};

const deleteRole = async (role: RoleDTO) => {
    if (!confirm(`Delete role "${role.name}"? This cannot be undone.`)) return;
    try {
        await api.organisations.rbac.deleteRole(props.orgId, role.name);
        roles.value = roles.value.filter((r) => r.name !== role.name);
    } catch (err) {
        console.error('Failed to delete role:', err);
    }
};

watch(
    () => props.orgId,
    (newOrgId) => {
        if (newOrgId) {
            fetchRbacSettings();
        }
    },
    { immediate: true }
);
</script>

<template>
    <div>
        <div class="flex items-start justify-between mb-6 gap-4">
            <p class="text-sm text-gray-600 max-w-lg">
                Define roles and their permissions. Roles control what members
                can do within this organisation.
            </p>
            <button
                class="shrink-0 px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 transition-colors"
                @click="openCreate"
            >
                <Plus :size="16" />
                Create Role
            </button>
        </div>

        <div v-if="isLoading" class="text-sm text-gray-400 py-8 text-center">
            Loading roles…
        </div>
        <div v-else-if="error" class="text-sm text-red-500 py-8 text-center">
            {{ error }}
        </div>
        <div
            v-else-if="roles.length === 0"
            class="text-center py-12 border border-dashed border-gray-300 rounded-lg"
        >
            <p class="text-sm text-gray-500">No roles defined yet.</p>
        </div>
        <div v-else class="space-y-4">
            <div
                v-for="role in roles"
                :key="role.name"
                class="bg-white border border-gray-300 rounded-lg p-6"
            >
                <div class="flex items-start justify-between mb-5">
                    <div class="flex items-center gap-3">
                        <div
                            class="w-10 h-10 bg-gray-100 rounded flex items-center justify-center shrink-0"
                        >
                            <Lock
                                v-if="isAdmin(role)"
                                :size="18"
                                class="text-gray-400"
                            />
                            <Shield v-else :size="18" class="text-gray-500" />
                        </div>
                        <div>
                            <div class="flex items-center gap-2">
                                <h3 class="font-semibold text-gray-900">
                                    {{ role.name }}
                                </h3>
                                <span
                                    v-if="isAdmin(role)"
                                    class="px-1.5 py-px text-[10px] font-bold uppercase tracking-wide rounded bg-gray-100 text-gray-500 border border-gray-200"
                                    >System</span
                                >
                            </div>
                            <p class="text-xs text-gray-500">
                                {{ role.permissions.length }} permission{{
                                    role.permissions.length !== 1 ? 's' : ''
                                }}
                            </p>
                        </div>
                    </div>
                    <div v-if="!isAdmin(role)" class="flex items-center gap-1">
                        <button
                            class="p-1.5 hover:bg-gray-100 rounded transition-colors"
                            title="Edit role"
                            @click="openEdit(role)"
                        >
                            <Pencil :size="15" class="text-gray-500" />
                        </button>
                        <button
                            class="p-1.5 hover:bg-red-50 rounded transition-colors"
                            title="Delete role"
                            @click="deleteRole(role)"
                        >
                            <Trash2 :size="15" class="text-red-500" />
                        </button>
                    </div>
                </div>
                <div
                    class="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-6 gap-y-4"
                >
                    <div
                        v-for="[group, perms] in permissionGroups"
                        :key="group"
                    >
                        <p
                            class="text-[10px] font-semibold text-gray-400 uppercase tracking-wider mb-1.5"
                        >
                            {{ formatGroup(group) }}
                        </p>
                        <div class="flex flex-col gap-1">
                            <span
                                v-for="perm in perms"
                                :key="perm.name"
                                :class="
                                    role.permissions.includes(perm.name)
                                        ? 'bg-green-50 text-green-800 border border-green-200'
                                        : 'bg-gray-50 text-gray-400 border border-gray-200'
                                "
                                class="inline-flex items-center gap-1.5 px-2 py-1 rounded text-xs font-medium"
                            >
                                <span
                                    :class="
                                        role.permissions.includes(perm.name)
                                            ? 'bg-green-500'
                                            : 'bg-gray-300'
                                    "
                                    class="w-1.5 h-1.5 rounded-full shrink-0"
                                />
                                {{ formatPerm(perm.name) }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <RoleModal
            :all-permissions="allPermissions"
            :is-saving="isSavingRole"
            :role="editTarget"
            :show="showModal"
            @close="closeModal"
            @save="saveRole"
        />
    </div>
</template>
