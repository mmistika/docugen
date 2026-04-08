package com.artembilous.docugen.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    public byte[] generateFromHtml(String html) {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(true));

            Page page = browser.newContext().newPage();
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

            browser.close();
            return pdf;

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
