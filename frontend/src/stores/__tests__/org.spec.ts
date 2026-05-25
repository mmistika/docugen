import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useOrgStore } from '../org';
import { api } from '@/api/client';

// Mock API
vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            my: vi.fn()
        }
    }
}));

describe('Org Store', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('should initialize with default state', () => {
        const store = useOrgStore();
        expect(store.organisations).toEqual([]);
        expect(store.currentOrgId).toBeNull();
        expect(store.currentOrg).toBeUndefined();
    });

    it('should fetch organisations and set currentOrgId to the first org if not already set', async () => {
        const store = useOrgStore();
        const mockOrgs = [
            { id: 10, name: 'Google', memberCount: 1 },
            { id: 20, name: 'DeepMind', memberCount: 1 }
        ];
        vi.mocked(api.organisations.my).mockResolvedValue(mockOrgs);

        await store.fetch();

        expect(api.organisations.my).toHaveBeenCalled();
        expect(store.organisations).toEqual(mockOrgs);
        expect(store.currentOrgId).toBe(10);
        expect(store.currentOrg).toEqual(mockOrgs[0]);
    });

    it('should fetch organisations but NOT overwrite currentOrgId if it is already set', async () => {
        const store = useOrgStore();
        store.currentOrgId = 20;

        const mockOrgs = [
            { id: 10, name: 'Google', memberCount: 1 },
            { id: 20, name: 'DeepMind', memberCount: 1 }
        ];
        vi.mocked(api.organisations.my).mockResolvedValue(mockOrgs);

        await store.fetch();

        expect(store.organisations).toEqual(mockOrgs);
        expect(store.currentOrgId).toBe(20);
        expect(store.currentOrg).toEqual(mockOrgs[1]);
    });

    it('should set currentOrgId when setCurrentOrg is called', () => {
        const store = useOrgStore();
        store.organisations = [
            { id: 10, name: 'Google', memberCount: 1 },
            { id: 20, name: 'DeepMind', memberCount: 1 }
        ];

        store.setCurrentOrg(20);
        expect(store.currentOrgId).toBe(20);
        expect(store.currentOrg?.name).toBe('DeepMind');
    });
});
