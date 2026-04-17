<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import {Edit, FileText, MoreVertical, Plus, Search} from '@lucide/vue';
import {RouterLink} from 'vue-router';
import {api} from '@/api/client';
import type {Template} from "@/types/template.ts";
import {useOrgStore} from "@/stores/org.ts";

const orgStore = useOrgStore();

const templates = ref<Template[]>([]);
const searchQuery = ref('');
const isLoading = ref(true);

watch(() => orgStore.currentOrgId, async (newId) => {
  if (newId) {
    try {
      templates.value = await api.organisations.templates.all(orgStore.currentOrgId!);
    } catch (error) {
    } finally {
      isLoading.value = false;
    }
  }
}, { immediate: true });

const filteredTemplates = computed(() => {
  if (!searchQuery.value) return templates.value;
  const query = searchQuery.value.toLowerCase();
  return templates.value.filter(t => t.name.toLowerCase().includes(query));
});
</script>

<template>
  <div class="p-4 lg:p-8">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 mb-1">Templates</h1>
        <p class="text-sm text-gray-600">Manage document templates</p>
      </div>
      <RouterLink
          to="/templates/new"
          class="px-4 py-2 bg-gray-900 text-white rounded text-sm hover:bg-gray-800 flex items-center gap-2 justify-center"
      >
        <Plus :size="16" />
        Create Template
      </RouterLink>
    </div>

    <div class="bg-white border border-gray-300 rounded-lg p-4 mb-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="flex-1 relative">
          <Search :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Search templates..."
              class="w-full pl-9 pr-4 py-2 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>
    </div>
    <div v-if="isLoading" class="text-center py-12 text-gray-500">Loading templates...</div>
    <div v-else-if="filteredTemplates.length === 0" class="text-center py-12 bg-white border border-dashed border-gray-300 rounded-lg">
      <p class="text-gray-500">No templates found.</p>
    </div>
    <div v-else class="hidden lg:grid grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="template in filteredTemplates" :key="template.id" class="bg-white border border-gray-300 rounded-lg overflow-hidden hover:shadow-md transition-shadow">
        <div class="h-40 bg-gray-100 border-b border-gray-300 p-4">
          <div class="w-full h-full border-2 border-dashed border-gray-400 rounded flex items-center justify-center">
            <FileText :size="32" class="text-gray-400" />
          </div>
        </div>
        <div class="p-4">
          <div class="flex items-start justify-between mb-4">
            <h3 class="font-medium text-gray-900 text-sm">{{ template.name }}</h3>
            <button class="p-1 hover:bg-gray-100 rounded">
              <MoreVertical :size="16" class="text-gray-600" />
            </button>
          </div>
          <div class="flex gap-2">
            <RouterLink
                :to="`/templates/${template.id}`"
                class="flex-1 px-3 py-1.5 border border-gray-300 rounded text-xs text-center hover:bg-gray-50 flex items-center justify-center gap-1 font-medium"
            >
              <Edit :size="12" />
              Edit
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!isLoading && filteredTemplates.length > 0" class="lg:hidden space-y-3">
      <div v-for="template in filteredTemplates" :key="template.id" class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <div class="flex">
          <div class="w-24 h-24 bg-gray-100 border-r border-gray-300 flex items-center justify-center shrink-0">
            <FileText :size="24" class="text-gray-400" />
          </div>
          <div class="flex-1 p-3">
            <div class="flex items-start justify-between h-full">
              <div class="flex-1 min-w-0">
                <h3 class="font-medium text-sm text-gray-900 truncate">{{ template.name }}</h3>
              </div>
              <button class="p-1 ml-2">
                <MoreVertical :size="16" class="text-gray-600" />
              </button>
            </div>
          </div>
        </div>
        <div class="border-t border-gray-300 p-3 flex gap-2">
          <RouterLink
              :to="`/templates/${template.id}`"
              class="flex-1 px-3 py-1.5 border border-gray-300 rounded text-xs text-center hover:bg-gray-50 flex items-center justify-center gap-1 font-medium"
          >
            <Edit :size="12" />
            Edit
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>