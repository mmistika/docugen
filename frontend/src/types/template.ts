export type TemplateVersionStatus = 'DRAFT' | 'ACTIVE' | 'RETIRED';

export interface Template {
    id: number;
    name: string;
}

export interface TemplateDetail {
    id: number;
    name: string;
    version: number;
    manifest: string;
    content: string;
    status: TemplateVersionStatus;
}
