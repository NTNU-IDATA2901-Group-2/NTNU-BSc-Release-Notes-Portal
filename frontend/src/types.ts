export interface ChangeNote {
    id: number,
    reference: string,
    description: string,
    developerNotes: string,
    upgradeNotes: string,
    changeSource: string,
    product: Product,
    scope: Scope,
    feature: Feature,
    customer: Customer,
    published: boolean,
    archived: boolean
}

export interface PersistChangeNoteDTO {
    reference?: string,
    description?: string,
    developerNotes?: string,
    upgradeNotes?: string,
    changeSource?: string,
    productId?: number,
    scopeId?: number,
    featureId?: number,
    published?: boolean,
    customerId?: number
}

export interface Customer {
    id: number,
    name: string
}

export interface Feature {
    id: number,
    name: string
}

export interface Product {
    id: number,
    name: string
}

export interface Scope {
    id: number,
    name: string
}

export interface ReleaseNote {
    id: number,
    tag: string,
    summary: string,
    published: boolean,
    changeNotes: ChangeNote[]
}

export interface PersistReleaseNoteDTO {
    version?: string,
    description?: string,
    published?: boolean,
    changeNoteIds?: number[]
}

export interface Tag {
    id: number,
    name: string
}