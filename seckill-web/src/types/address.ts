/**
 * 收货地址
 */
export interface Address {
    id: string
    /** 收件人 */
    name: string
    /** 手机号 */
    phone: string
    /** 省份 */
    province: string
    /** 城市 */
    city: string
    /** 区县 */
    district: string
    /** 详细地址 */
    detail: string
    /** 是否默认地址 */
    isDefault: boolean
}

/**
 * 地址表单值
 */
export type AddressFormValues = Omit<Address, 'id' | 'isDefault'>
