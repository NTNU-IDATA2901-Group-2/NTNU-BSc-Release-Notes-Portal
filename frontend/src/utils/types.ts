export interface ChangeNote {
    id: number,
    title: string,
    reference: string,
    description: string,
    developerNotes: string,
    upgradeNotes: string,
    product: Product,
    scope: Scope,
    feature: Feature,
    customer: Customer,
    published: boolean,
    archived: boolean,
    viewableByEveryone: boolean,
    gitRepositoryId?: number,
    gitCommitHash?: string,
    relatedReleaseNoteIds?: number[]
}

export interface PersistChangeNoteDTO {
    title?: string,
    reference?: string,
    description?: string,
    developerNotes?: string,
    upgradeNotes?: string,
    productId?: number,
    scopeId?: number,
    featureId?: number,
    published?: boolean,
    customerId?: number
}

export interface GitRepository {
    id: number,
    name: string,
    url: string
}

export interface PersistGitRepositoryDTO {
    name: string,
    url: string
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

export interface ReleaseTimeline {
    previewAvailableFrom?: string,
    recommendedTestPhaseFrom?: string,
    recommendedTestPhaseTo?: string,
    plannedProductionDeployment?: string
}

export type TestingNeed = 'LOW' | 'LOW_MEDIUM' | 'MEDIUM' | 'MEDIUM_HIGH' | 'HIGH'
export const testingNeedValues = ['LOW', 'LOW_MEDIUM', 'MEDIUM', 'MEDIUM_HIGH', 'HIGH'] as const
export interface ChangeImpact {
    id: number,
    feature?: Feature,
    whatIsChanged: string,
    whatShouldBeTested: string,
    testingNeed?: TestingNeed
}

export interface PersistChangeImpactDTO {
    featureId: number,
    whatIsChanged: string,
    whatShouldBeTested: string,
    testingNeed: TestingNeed
}

export interface ReleaseNote {
    id: number,
    tag: string,
    summary: string,
    published: boolean,
    changeNotes: ChangeNote[],
    product?: Product,
    syncedToGit: boolean,
    releaseTimeline: ReleaseTimeline,
    knownLimitations: string[],
    changeImpacts: ChangeImpact[]
}



export interface PersistReleaseNoteDTO {
    version?: string,
    description?: string,
    published?: boolean,
    changeNoteIds?: number[],
    productId?: number,
    releaseTimeline?: ReleaseTimeline,
    knownLimitations?: string[]
}

export interface PersistChangeNoteDTO {
    title?: string,
    reference?: string,
    description?: string,
    developerNotes?: string,
    upgradeNotes?: string,
    productId?: number,
    scopeId?: number,
    featureId?: number,
    customerId?: number,
    viewableByEveryone?: boolean,
}

export interface PaginatedResponse<T> {
    content: T,
    totalItems: number
}

export interface Tag {
    id: number,
    name: string
}

export interface OnMutationApiCallFinished {
    onSettled?: () => void,
    onSuccess: (data?: string) => void,
    onError: () => void
}

export interface Prompt {
    id: number,
    name: string,
    prompt: string
}
