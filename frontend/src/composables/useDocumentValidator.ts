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

import { computed, type Ref } from 'vue';
import type { Field } from '@/types/field';

function validateField(field: Field, value: string): string | null {
    if (field.required && !value) {
        return 'This field is required.';
    }
    if (!value) return null;

    if (field.type === 'text') {
        if (field.minLength != null && value.length < field.minLength) {
            return `Minimum ${field.minLength} characters.`;
        }
        if (field.maxLength != null && value.length > field.maxLength) {
            return `Maximum ${field.maxLength} characters.`;
        }
    }

    if (field.type === 'number') {
        const num = Number(value);
        if (isNaN(num)) {
            return 'Must be a number.';
        }
        if (field.minValue != null && num < field.minValue) {
            return `Minimum value is ${field.minValue}.`;
        }
        if (field.maxValue != null && num > field.maxValue) {
            return `Maximum value is ${field.maxValue}.`;
        }
        if (field.decimalPlaces != null) {
            const parts = value.split('.');
            const decimalPart = parts[1];
            const actualDecimals = decimalPart
                ? decimalPart.replace(/0+$/, '').length
                : 0;
            if (actualDecimals > field.decimalPlaces) {
                return `Maximum of ${field.decimalPlaces} decimal place(s) allowed.`;
            }
        }
    }

    return null;
}

export function useDocumentValidator(
    documentName: Ref<string>,
    fieldValues: Ref<Record<string, string>>,
    manifestStr: Ref<string | undefined>
) {
    const globalFields = computed<Field[]>(() => {
        if (!manifestStr.value) return [];
        try {
            return JSON.parse(manifestStr.value).fields ?? [];
        } catch {
            return [];
        }
    });

    const validationErrors = computed<Record<string, string>>(() => {
        const errors: Record<string, string> = {};

        if (!documentName.value.trim()) {
            errors['documentName'] = 'Document name is required.';
        }

        for (const field of globalFields.value) {
            const raw = fieldValues.value[field.name];
            const value = raw != null ? String(raw).trim() : '';
            const err = validateField(field, value);
            if (err) {
                errors[field.name] = err;
            }
        }

        try {
            const inlineFields: Field[] =
                JSON.parse(manifestStr.value ?? '{}').inline_fields ?? [];
            for (const field of inlineFields) {
                const raw = fieldValues.value[field.name];
                const value = raw != null ? String(raw).trim() : '';
                const err = validateField(field, value);
                if (err) {
                    errors[`inline_${field.name}`] = err;
                }
            }
        } catch {}

        return errors;
    });

    const hasErrors = computed(
        () => Object.keys(validationErrors.value).length > 0
    );
    const errorCount = computed(
        () => Object.keys(validationErrors.value).length
    );

    return {
        globalFields,
        validationErrors,
        hasErrors,
        errorCount
    };
}
