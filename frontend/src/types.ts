export interface ChangeNote {
    id: Number,
    reference: String,
    description: String,
    developerNotes: String,
    upgradeNotes: String,
    changeSource: String,
    product: Product,
    scope: Scope,
    feature: Feature,
    customer: Customer,
    published: boolean,
    archived: boolean
}

export interface PersistChangeNoteDTO {
    reference?: String,
    description?: String,
    developerNotes?: String,
    upgradeNotes?: String,
    changeSource?: String,
    productId?: Number,
    scopeId?: Number,
    featureId?: Number,
    published?: boolean,
    customerId?: Number
}

export interface Customer {
    id: Number,
    name: String
}

export interface Feature {
    id: Number,
    name: String
}

export interface Product {
    id: Number,
    name: String
}

export interface Scope {
    id: Number,
    name: String
}

export interface ReleaseNote {
    id: Number,
    version: String,
    description: String,
    published: boolean,
    changeNotes: ChangeNote[]
}

export interface PersistReleaseNoteDTO {
    version?: String,
    description?: String,
    published?: boolean,
    changeNoteIds?: Number[]
}