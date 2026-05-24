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
