export type DocumentStatus = 'DRAFT' | 'FINAL';

export interface Document {
    id: number
    name: string
    status: DocumentStatus
    templateName: string
    createdAt: Date
}