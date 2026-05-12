package com.artembilous.docugen.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        this.playwright = Playwright.create();
        this.browser = this.playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @PreDestroy
    public void cleanup() {
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
    }

    public byte[] generateFromHtml(String html) {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

            return page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setMargin(new Margin()
                            .setTop("28mm")
                            .setBottom("28mm")
                            .setLeft("20mm")
                            .setRight("20mm")
                    )
                    .setPrintBackground(true)
            );
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
