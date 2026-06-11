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

import { describe, expect, it } from 'vitest';
import { ref } from 'vue';
import { useDocumentValidator } from '../useDocumentValidator';

describe('useDocumentValidator Composable', () => {
    it('should validate document name and identify empty values', () => {
        const documentName = ref('');
        const fieldValues = ref<Record<string, string>>({});
        const manifestStr = ref<string | undefined>(undefined);

        const { validationErrors, hasErrors, errorCount } =
            useDocumentValidator(documentName, fieldValues, manifestStr);

        expect(hasErrors.value).toBe(true);
        expect(errorCount.value).toBe(1);
        expect(validationErrors.value).toEqual({
            documentName: 'Document name is required.'
        });

        documentName.value = '   ';
        expect(validationErrors.value.documentName).toBe(
            'Document name is required.'
        );

        documentName.value = 'My Document';
        expect(hasErrors.value).toBe(false);
        expect(errorCount.value).toBe(0);
        expect(validationErrors.value).toEqual({});
    });

    it('should parse global fields from manifest string', () => {
        const documentName = ref('Doc');
        const fieldValues = ref<Record<string, string>>({});
        const manifestStr = ref(
            JSON.stringify({
                fields: [
                    { id: '1', name: 'title', type: 'text', required: true }
                ]
            })
        );

        const { globalFields } = useDocumentValidator(
            documentName,
            fieldValues,
            manifestStr
        );

        expect(globalFields.value).toHaveLength(1);
        expect(globalFields.value[0]).toEqual({
            id: '1',
            name: 'title',
            type: 'text',
            required: true
        });
    });

    it('should handle malformed manifest string gracefully', () => {
        const documentName = ref('Doc');
        const fieldValues = ref<Record<string, string>>({});
        const manifestStr = ref('invalid-json');

        const { globalFields, validationErrors } = useDocumentValidator(
            documentName,
            fieldValues,
            manifestStr
        );

        expect(globalFields.value).toEqual([]);
        expect(validationErrors.value).toEqual({});
    });

    describe('Text Field Validation', () => {
        it('should validate required text fields', () => {
            const documentName = ref('Doc');
            const fieldValues = ref<Record<string, string>>({ username: '' });
            const manifestStr = ref(
                JSON.stringify({
                    fields: [{ name: 'username', type: 'text', required: true }]
                })
            );

            const { validationErrors } = useDocumentValidator(
                documentName,
                fieldValues,
                manifestStr
            );
            expect(validationErrors.value.username).toBe(
                'This field is required.'
            );

            fieldValues.value.username = 'john_doe';
            expect(validationErrors.value.username).toBeUndefined();
        });

        it('should validate minLength and maxLength', () => {
            const documentName = ref('Doc');
            const fieldValues = ref<Record<string, string>>({ note: 'hi' });
            const manifestStr = ref(
                JSON.stringify({
                    fields: [
                        {
                            name: 'note',
                            type: 'text',
                            required: false,
                            minLength: 5,
                            maxLength: 10
                        }
                    ]
                })
            );

            const { validationErrors } = useDocumentValidator(
                documentName,
                fieldValues,
                manifestStr
            );
            expect(validationErrors.value.note).toBe('Minimum 5 characters.');

            fieldValues.value.note = 'validnote';
            expect(validationErrors.value.note).toBeUndefined();

            fieldValues.value.note = 'too_long_note_value';
            expect(validationErrors.value.note).toBe('Maximum 10 characters.');
        });
    });

    describe('Number Field Validation', () => {
        it('should validate value is a number', () => {
            const documentName = ref('Doc');
            const fieldValues = ref<Record<string, string>>({ age: 'abc' });
            const manifestStr = ref(
                JSON.stringify({
                    fields: [{ name: 'age', type: 'number', required: false }]
                })
            );

            const { validationErrors } = useDocumentValidator(
                documentName,
                fieldValues,
                manifestStr
            );
            expect(validationErrors.value.age).toBe('Must be a number.');

            fieldValues.value.age = '25';
            expect(validationErrors.value.age).toBeUndefined();
        });

        it('should validate minValue and maxValue', () => {
            const documentName = ref('Doc');
            const fieldValues = ref<Record<string, string>>({ rating: '12' });
            const manifestStr = ref(
                JSON.stringify({
                    fields: [
                        {
                            name: 'rating',
                            type: 'number',
                            required: false,
                            minValue: 1,
                            maxValue: 10
                        }
                    ]
                })
            );

            const { validationErrors } = useDocumentValidator(
                documentName,
                fieldValues,
                manifestStr
            );
            expect(validationErrors.value.rating).toBe('Maximum value is 10.');

            fieldValues.value.rating = '0';
            expect(validationErrors.value.rating).toBe('Minimum value is 1.');

            fieldValues.value.rating = '5';
            expect(validationErrors.value.rating).toBeUndefined();
        });

        it('should validate decimalPlaces', () => {
            const documentName = ref('Doc');
            const fieldValues = ref<Record<string, string>>({
                price: '10.555'
            });
            const manifestStr = ref(
                JSON.stringify({
                    fields: [
                        {
                            name: 'price',
                            type: 'number',
                            required: false,
                            decimalPlaces: 2
                        }
                    ]
                })
            );

            const { validationErrors } = useDocumentValidator(
                documentName,
                fieldValues,
                manifestStr
            );
            expect(validationErrors.value.price).toBe(
                'Maximum of 2 decimal place(s) allowed.'
            );

            fieldValues.value.price = '10.500';
            expect(validationErrors.value.price).toBeUndefined();

            fieldValues.value.price = '10.5';
            expect(validationErrors.value.price).toBeUndefined();

            fieldValues.value.price = '10';
            expect(validationErrors.value.price).toBeUndefined();
        });
    });

    describe('Inline Field Validation', () => {
        it('should validate inline fields and prefix error keys with inline_', () => {
            const documentName = ref('Doc');
            const fieldValues = ref<Record<string, string>>({ code: '' });
            const manifestStr = ref(
                JSON.stringify({
                    inline_fields: [
                        { name: 'code', type: 'text', required: true }
                    ]
                })
            );

            const { validationErrors } = useDocumentValidator(
                documentName,
                fieldValues,
                manifestStr
            );
            expect(validationErrors.value.inline_code).toBe(
                'This field is required.'
            );

            fieldValues.value.code = '123';
            expect(validationErrors.value.inline_code).toBeUndefined();
        });
    });
});
