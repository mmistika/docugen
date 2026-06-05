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
