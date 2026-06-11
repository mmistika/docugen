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
import { buildInteractiveContent } from '../templateParser';

describe('templateParser.ts - buildInteractiveContent', () => {
    it('should convert span to input element with correct attributes', () => {
        const html =
            '<p>Hello <span data-type="inline-field" name="age" type="number" required="true"></span></p>';
        const result = buildInteractiveContent(html);

        expect(result).toContain('<input');
        expect(result).toContain('data-field-name="age"');
        expect(result).toContain('type="number"');
        expect(result).toContain('placeholder="{age}"');
        expect(result).toContain('required="true"');
        expect(result).toContain('class="inline-doc-input"');
    });

    it('should use default text type when type is omitted', () => {
        const html =
            '<p>Hello <span data-type="inline-field" name="firstname"></span></p>';
        const result = buildInteractiveContent(html);

        expect(result).toContain('type="text"');
        expect(result).not.toContain('required=');
    });
});
