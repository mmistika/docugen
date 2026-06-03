import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '../auth';
import { api } from '@/api/client';
import { auth0 } from '@/auth';

// Mock API
vi.mock('@/api/client', () => ({
    api: {
        users: {
            me: vi.fn(),
            completeRegistration: vi.fn(),
            updateProfile: vi.fn()
        },
        organisations: {
            create: vi.fn()
        }
    }
}));

// Mock Auth0
vi.mock('@/auth', () => ({
    auth0: {
        logout: vi.fn(),
        getAccessTokenSilently: vi.fn()
    }
}));

describe('Auth Store', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('should initialize with default state', () => {
        const store = useAuthStore();
        expect(store.user).toBeNull();
    });

    it('should fetch user profile details on fetchMe', async () => {
        const store = useAuthStore();
        const mockMe = {
            userId: 1,
            email: 'john.doe@example.com',
            name: 'John',
            surname: 'Doe',
            image: null,
            registered: true
        };
        vi.mocked(api.users.me).mockResolvedValue(mockMe);

        await store.fetchMe();

        expect(api.users.me).toHaveBeenCalled();
        expect(store.user).toEqual(mockMe);
    });

    it('should complete profile registration and update user state if user is logged in', async () => {
        const store = useAuthStore();
        store.user = {
            userId: 1,
            email: 'john.doe@example.com',
            name: '',
            surname: '',
            image: null,
            registered: false
        };
        const mockMe = {
            userId: 1,
            email: 'john.doe@example.com',
            name: 'John',
            surname: 'Doe',
            image: null,
            registered: true
        };

        vi.mocked(api.users.completeRegistration).mockResolvedValue(mockMe);

        await store.completeProfile('John', 'Doe');

        expect(api.users.completeRegistration).toHaveBeenCalledWith({
            name: 'John',
            surname: 'Doe',
            image: null
        });
        expect(store.user).toEqual(mockMe);
    });

    it('should update state on completeProfile even if user was not initially loaded', async () => {
        const store = useAuthStore();
        store.user = null;
        const mockMe = {
            userId: 1,
            email: 'john.doe@example.com',
            name: 'John',
            surname: 'Doe',
            image: null,
            registered: true
        };

        vi.mocked(api.users.completeRegistration).mockResolvedValue(mockMe);

        await store.completeProfile('John', 'Doe');
        expect(api.users.completeRegistration).toHaveBeenCalledWith({
            name: 'John',
            surname: 'Doe',
            image: null
        });
        expect(store.user).toEqual(mockMe);
    });

    it('should update profile and update user state', async () => {
        const store = useAuthStore();
        const updatedUser = {
            userId: 1,
            email: 'john.doe@example.com',
            name: 'Johnny',
            surname: 'Doe',
            image: 'new-image-url',
            registered: true
        };
        vi.mocked(api.users.updateProfile).mockResolvedValue(updatedUser);

        await store.updateProfile('Johnny', 'Doe', 'new-image-url');

        expect(api.users.updateProfile).toHaveBeenCalledWith({
            name: 'Johnny',
            surname: 'Doe',
            image: 'new-image-url'
        });
        expect(store.user).toEqual(updatedUser);
    });

    it('should complete organization setup and set registered flag to true', async () => {
        const store = useAuthStore();
        store.user = {
            userId: 1,
            email: 'john.doe@example.com',
            name: 'John',
            surname: 'Doe',
            image: null,
            registered: false
        };

        vi.mocked(api.organisations.create).mockResolvedValue();

        await store.completeOrg('My Org');

        expect(api.organisations.create).toHaveBeenCalledWith({
            name: 'My Org'
        });
        expect(store.user.registered).toBe(true);
    });

    it('should call auth0 logout on logout', async () => {
        const store = useAuthStore();
        const originalLocation = window.location;
        // @ts-expect-error TS2790: The operand of a delete operator must be optional.
        delete window.location;
        window.location = {
            ...originalLocation,
            origin: 'http://localhost:3000'
        } as any;

        vi.mocked(auth0.logout).mockResolvedValue(undefined as any);

        await store.logout();

        expect(auth0.logout).toHaveBeenCalledWith({
            logoutParams: { returnTo: 'http://localhost:3000' }
        });

        // @ts-expect-error TS2322: Type Location is not assignable to type string & Location
        window.location = originalLocation;
    });
});
