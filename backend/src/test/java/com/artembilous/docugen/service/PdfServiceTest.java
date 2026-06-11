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

package com.artembilous.docugen.service;

import com.microsoft.playwright.Browser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfServiceTest {

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();
        pdfService.init();
    }

    @AfterEach
    void tearDown() {
        pdfService.cleanup();
    }

    @Test
    void testGeneratePdfSuccess() {
        String html = "<h1>Test Title</h1><p>Hello, this is a test PDF.</p>";
        byte[] pdfBytes = pdfService.generateFromHtml(html);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testBrowserRecoveryWhenClosed() {
        String html = "<h1>Test Title</h1><p>First run.</p>";
        byte[] pdfBytes1 = pdfService.generateFromHtml(html);
        assertNotNull(pdfBytes1);

        Browser initialBrowser = pdfService.getBrowser();
        assertNotNull(initialBrowser);
        assertTrue(initialBrowser.isConnected());

        initialBrowser.close();
        assertFalse(initialBrowser.isConnected());

        byte[] pdfBytes2 = pdfService.generateFromHtml(html);
        assertNotNull(pdfBytes2);
        assertTrue(pdfBytes2.length > 0);

        Browser newBrowser = pdfService.getBrowser();
        assertNotNull(newBrowser);
        assertNotSame(initialBrowser, newBrowser);
        assertTrue(newBrowser.isConnected());
    }

    @Test
    void testBrowserRecoveryWhenNull() {
        pdfService.cleanup();
        assertNull(pdfService.getBrowser());

        String html = "<h1>Test Title</h1><p>Recover from null.</p>";
        byte[] pdfBytes = pdfService.generateFromHtml(html);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        assertNotNull(pdfService.getBrowser());
        assertTrue(pdfService.getBrowser().isConnected());
    }
}
