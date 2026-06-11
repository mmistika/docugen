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

export function buildInteractiveContent(html: string): string {
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');

    doc.querySelectorAll<HTMLElement>('span[data-type="inline-field"]').forEach(
        (span) => {
            const name = span.getAttribute('name') ?? '';
            const fieldType = span.getAttribute('type') ?? 'text';
            const required = span.getAttribute('required') === 'true';

            const input = doc.createElement('input');
            input.setAttribute('data-field-name', name);
            input.setAttribute(
                'type',
                fieldType === 'number' ? 'number' : 'text'
            );
            input.setAttribute('placeholder', `{${name}}`);
            input.setAttribute('aria-label', name);
            input.setAttribute('size', String(Math.max(name.length + 2, 4)));
            if (required) input.setAttribute('required', 'true');
            input.className = 'inline-doc-input';

            span.replaceWith(input);
        }
    );

    return doc.body.innerHTML;
}
