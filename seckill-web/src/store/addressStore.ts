import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Address, AddressFormValues } from '@/types/address'

interface AddressState {
    addresses: Address[]
    addAddress: (values: AddressFormValues) => void
    updateAddress: (id: string, values: Partial<AddressFormValues>) => void
    deleteAddress: (id: string) => void
    setDefault: (id: string) => void
    getDefaultAddress: () => Address | undefined
}

export const useAddressStore = create<AddressState>()(
    persist(
        (set, get) => ({
            addresses: [],

            addAddress: (values) => {
                const newAddress: Address = {
                    ...values,
                    id: Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
                    isDefault: get().addresses.length === 0, // 第一个自动设为默认
                }
                set((state) => ({
                    addresses: [...state.addresses, newAddress],
                }))
            },

            updateAddress: (id, values) => {
                set((state) => ({
                    addresses: state.addresses.map((addr) =>
                        addr.id === id ? { ...addr, ...values } : addr
                    ),
                }))
            },

            deleteAddress: (id) => {
                set((state) => {
                    const remaining = state.addresses.filter((a) => a.id !== id)
                    // 如果删除的是默认地址，将第一个设为默认
                    const deletedWasDefault = state.addresses.find((a) => a.id === id)?.isDefault
                    if (deletedWasDefault && remaining.length > 0) {
                        remaining[0].isDefault = true
                    }
                    return { addresses: remaining }
                })
            },

            setDefault: (id) => {
                set((state) => ({
                    addresses: state.addresses.map((addr) => ({
                        ...addr,
                        isDefault: addr.id === id,
                    })),
                }))
            },

            getDefaultAddress: () => {
                return get().addresses.find((a) => a.isDefault)
            },
        }),
        {
            name: 'seckill-address-storage',
        }
    )
)
