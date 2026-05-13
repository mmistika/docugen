package com.artembilous.docugen.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PdfService {

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        log.info("Initializing Playwright and launching headless Chromium browser...");
        this.playwright = Playwright.create();
        this.browser = this.playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
        log.info("Playwright and Chromium browser successfully initialized.");
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down Playwright browser process...");
        if (this.browser != null) {
            try {
                this.browser.close();
            } catch (Exception ignored) {
            }
        }
        if (this.playwright != null) {
            try {
                this.playwright.close();
            } catch (Exception ignored) {
            }
        }
        log.info("Playwright browser process cleanly terminated.");
    }

    public byte[] generateFromHtml(String html) {
        long startTime = System.currentTimeMillis();
        log.info("Initiating PDF generation request (HTML length: {})...", html.length());

        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

            byte[] pdf = page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setMargin(new Margin()
                            .setTop("28mm")
                            .setBottom("28mm")
                            .setLeft("20mm")
                            .setRight("20mm")
                    )
                    .setPrintBackground(true)
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("PDF generation completed successfully in {}ms (PDF size: {} bytes).", duration, pdf.length);
            return pdf;
        } catch (Exception e) {
            log.error("Failed to generate PDF from HTML: ", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
