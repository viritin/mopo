package firitin.pw;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import in.virit.mopo.Mopo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("playwright")
public class AddonHelpersIT {
    private final int port = 9998;

    static Playwright playwright = Playwright.create();

    static {
    }

    private Browser browser;
    private Page page;
    private Mopo mopo;

    @BeforeEach
    public void setup() {
        browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setDevtools(true)
                );

        page = browser.newPage();
        page.setDefaultTimeout(5000); // die faster if needed
        mopo = new Mopo(page);
    }

    @AfterEach
    public  void closePlaywright() {
        page.close();
        browser.close();
    }

    @Test
    public void doRandomStuffAndChangeFirstRow() throws InterruptedException {
        page.navigate("http://localhost:" + port + "/addonhelpers");

        // There should be a JS error in the dev console, assert that
        mopo.assertNoJsErrors();

        page.getByText("Throw JS exception").click();

        boolean exceptionThrown = false;
        try {
            mopo.assertNoJsErrors();
        } catch (java.lang.AssertionError e) {
            // Expected
            System.out.println(e.getMessage());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertThat(page.locator("vaadin-dev-tools>div.error")).isAttached();
   }
}
