/*
 * Docugen — Document Generation & Management Platform
 * Copyright (C) 2026 Artem Bilous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

export interface Field {
    id: string;
    name: string;
    type: FieldType;
    required: boolean;
    // text
    maxLength?: number | null;
    minLength?: number | null;
    placeholder?: string | null;
    // number
    minValue?: number | null;
    maxValue?: number | null;
    decimalPlaces?: number | null;
}

export type FieldType = 'text' | 'number';

export type FieldPropertyDef =
    | { key: string; label: string; kind: 'text'; placeholder?: string }
    | { key: string; label: string; kind: 'number'; placeholder?: string }
    | { key: string; label: string; kind: 'boolean' };

export interface FieldTypeDef {
    type: FieldType;
    label: string;
    // lucide icon name
    icon: string;
    defaults: Partial<Omit<Field, 'id' | 'name' | 'type' | 'required'>>;
    properties: FieldPropertyDef[];
}

export const FIELD_TYPE_REGISTRY: FieldTypeDef[] = [
    {
        type: 'text',
        label: 'Text',
        icon: 'Type',
        defaults: {
            maxLength: 255,
            minLength: 0,
            placeholder: 'Enter text...'
        },
        properties: [
            {
                key: 'placeholder',
                label: 'Placeholder',
                kind: 'text',
                placeholder: 'Enter text...'
            },
            {
                key: 'minLength',
                label: 'Min Length',
                kind: 'number',
                placeholder: '0'
            },
            {
                key: 'maxLength',
                label: 'Max Length',
                kind: 'number',
                placeholder: '255'
            }
        ]
    },
    {
        type: 'number',
        label: 'Number',
        icon: 'Hash',
        defaults: {
            minValue: 0,
            maxValue: 999,
            decimalPlaces: 0
        },
        properties: [
            {
                key: 'minValue',
                label: 'Min Value',
                kind: 'number',
                placeholder: '0'
            },
            {
                key: 'maxValue',
                label: 'Max Value',
                kind: 'number',
                placeholder: '999'
            },
            {
                key: 'decimalPlaces',
                label: 'Decimal Places',
                kind: 'number',
                placeholder: '0'
            }
        ]
    }
];

export const FIELD_TYPE_MAP = Object.fromEntries(
    FIELD_TYPE_REGISTRY.map((d) => [d.type, d])
) as Record<FieldType, FieldTypeDef>;

export function createField(
    type: FieldType,
    scope: 'global' | 'inline'
): Field {
    const def = FIELD_TYPE_MAP[type];
    const uniqueSuffix = Math.random().toString(36).substring(2, 9);
    return {
        id: `${scope}_${uniqueSuffix}`,
        name: `${scope}_${type}`,
        type,
        required: false,
        ...def.defaults
    };
}
