export type DocumentStatus = 'DRAFT' | 'ACTIVE';

export interface Document {
    id: number
    name: string
    status: DocumentStatus
    templateName: string
    createdAt: Date
}