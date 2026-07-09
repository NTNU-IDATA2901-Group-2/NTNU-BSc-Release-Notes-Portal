import type { OnMutationApiCallFinished, PersistTagDTO, Tag } from "@/utils/types";
import type { UseMutationReturnType, UseQueryReturnType } from "@tanstack/vue-query";
import { useDeleteCustomer, useGetCustomers, usePersistCustomer, useUpdateCustomer } from "@/api/customers-api";
import { useDeleteFeature, useGetFeatures, usePersistFeature, useUpdateFeature } from "@/api/features-api";
import { useDeleteProduct, useGetProducts, usePersistProduct, useUpdateProduct } from "@/api/products-api";
import { useDeleteScope, useGetScopes, usePersistScope, useUpdateScope } from "@/api/scopes-api";

/**
 * Configuration wiring one manageable entity (product, feature, customer or scope)
 * to its vue-query hooks and i18n keys. All four entities share the {id, name} Tag shape,
 * so a single CRUD panel component can be driven by this config.
 */
export interface EntityCrudConfig {
  /** Singular entity key, resolves labels via the existing title.* i18n namespace. */
  entityKey: 'product' | 'feature' | 'customer' | 'scope',
  /** Plural key used as the tab value and for the entityManagement.tabs.* i18n label. */
  tabKey: 'products' | 'features' | 'customers' | 'scopes',
  useList: () => UseQueryReturnType<Tag[], Error>,
  useCreate: (onFinished: OnMutationApiCallFinished) => UseMutationReturnType<unknown, unknown, PersistTagDTO, unknown>,
  useUpdate: (onFinished: OnMutationApiCallFinished) => UseMutationReturnType<unknown, unknown, { id: number, name: string }, unknown>,
  useDelete: (onFinished: OnMutationApiCallFinished) => UseMutationReturnType<unknown, unknown, number, unknown>,
}

export const entityCrudConfigs: EntityCrudConfig[] = [
  {
    entityKey: 'product',
    tabKey: 'products',
    useList: useGetProducts,
    useCreate: usePersistProduct,
    useUpdate: useUpdateProduct,
    useDelete: useDeleteProduct,
  },
  {
    entityKey: 'feature',
    tabKey: 'features',
    useList: useGetFeatures,
    useCreate: usePersistFeature,
    useUpdate: useUpdateFeature,
    useDelete: useDeleteFeature,
  },
  {
    entityKey: 'customer',
    tabKey: 'customers',
    useList: useGetCustomers,
    useCreate: usePersistCustomer,
    useUpdate: useUpdateCustomer,
    useDelete: useDeleteCustomer,
  },
  {
    entityKey: 'scope',
    tabKey: 'scopes',
    useList: useGetScopes,
    useCreate: usePersistScope,
    useUpdate: useUpdateScope,
    useDelete: useDeleteScope,
  },
]
