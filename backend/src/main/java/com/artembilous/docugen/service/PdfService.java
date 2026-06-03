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
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
        String executablePath = System.getenv("PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH");
        if (executablePath != null && !executablePath.isBlank()) {
            log.info("Using system-provided Chromium at: {}", executablePath);
            options.setExecutablePath(java.nio.file.Paths.get(executablePath));
        }
        this.browser = this.playwright.chromium().launch(options);
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

        String styledHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "body {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    font-family: 'Inter', system-ui, -apple-system, sans-serif;\n" +
                "    font-size: 14px;\n" +
                "    line-height: 1.6;\n" +
                "    color: #1f2937;\n" +
                "}\n" +
                "p {\n" +
                "    margin-top: 0;\n" +
                "    margin-bottom: 0.75rem;\n" +
                "}\n" +
                "h1 {\n" +
                "    font-size: 1.8rem;\n" +
                "    font-weight: 700;\n" +
                "    margin-top: 1.5rem;\n" +
                "    margin-bottom: 0.75rem;\n" +
                "    color: #111827;\n" +
                "    line-height: 1.25;\n" +
                "}\n" +
                "h2 {\n" +
                "    font-size: 1.4rem;\n" +
                "    font-weight: 600;\n" +
                "    margin-top: 1.25rem;\n" +
                "    margin-bottom: 0.5rem;\n" +
                "    color: #111827;\n" +
                "    line-height: 1.25;\n" +
                "}\n" +
                "h3 {\n" +
                "    font-size: 1.2rem;\n" +
                "    font-weight: 600;\n" +
                "    margin-top: 1rem;\n" +
                "    margin-bottom: 0.5rem;\n" +
                "    color: #111827;\n" +
                "    line-height: 1.25;\n" +
                "}\n" +
                "ul {\n" +
                "    list-style-type: disc;\n" +
                "    padding-left: 1.5rem;\n" +
                "    margin-top: 0;\n" +
                "    margin-bottom: 0.75rem;\n" +
                "}\n" +
                "ol {\n" +
                "    list-style-type: decimal;\n" +
                "    padding-left: 1.5rem;\n" +
                "    margin-top: 0;\n" +
                "    margin-bottom: 0.75rem;\n" +
                "}\n" +
                "li {\n" +
                "    margin-bottom: 0.25rem;\n" +
                "}\n" +
                "strong {\n" +
                "    font-weight: 600;\n" +
                "    color: #111827;\n" +
                "}\n" +
                "em {\n" +
                "    font-style: italic;\n" +
                "}\n" +
                "blockquote {\n" +
                "    border-left: 3px solid #000000;\n" +
                "    padding-left: 1rem;\n" +
                "    margin-left: 0;\n" +
                "    margin-right: 0;\n" +
                "    margin-top: 1rem;\n" +
                "    margin-bottom: 1rem;\n" +
                "    color: #4b5563;\n" +
                "    font-style: italic;\n" +
                "}\n" +
                "hr {\n" +
                "    border: 0;\n" +
                "    border-top: 1px solid #e5e7eb;\n" +
                "    margin-top: 1.5rem;\n" +
                "    margin-bottom: 1.5rem;\n" +
                "}\n" +
                "mark {\n" +
                "    background-color: #fef08a;\n" +
                "    border-radius: 0.125rem;\n" +
                "    padding: 0 0.125rem;\n" +
                "}\n" +
                "p:empty::before {\n" +
                "    content: \"\\00a0\";\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                html + "\n" +
                "</body>\n" +
                "</html>";

        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            page.setContent(styledHtml, new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

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
