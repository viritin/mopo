package firitin.pw;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import in.virit.mopo.Mopo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("playwright")
public class AddonHelpersIT {
    private final int port = 9998;

    static Playwright playwright = Playwright.create();

    private Browser browser;
    private Page page;
    private Mopo mopo;

    @BeforeEach
    public void setup() {
        browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions()
//                        .setHeadless(false)
//                        .setDevtools(true)
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

    //@Test // Disabled because with latest Vaadin version copilot/devmode don't seem to load anymore and probably non-functional
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

    @Test
    public void checkJsErrorsViaConsole() throws InterruptedException {
        // Note, start tracking console errors only here, as Vaadin gives one favicon related error by default
        mopo.trackClientSideErrors();
        page.navigate("http://localhost:" + port + "/addonhelpers");

        mopo.waitForConnectionToSettle();

        mopo.getClientSideErrors().clear(); // Vaadin gives one favicon related error by default, so clear it

        Assertions.assertEquals(0, mopo.getClientSideErrors().size(),
                "There should be no console errors after the page has loaded");

        // a helper message to fail the test if there is a JS error
        page.getByText("Throw JS exception").click();

        assertThat(page.getByText("Error should have been thrown!")).isVisible();

        // You could call this in the beginning of the test, but testing it is nasty
        // mopo.failOnClientSideErrorsOnClose();

        // This is pretty much the same as above but inverted for testing
        boolean thrown = false;
        try {
            mopo.failOnClientSideErrors();
        } catch (RuntimeException e) {
            thrown = true;
        }
        Assertions.assertTrue(thrown, "There should be an exception thrown");
    }

    @Test
    public void listView() {
       // One could now open each of these and e.g. check for not JS errors
       List<String> developmentTimeViewNames = mopo.getViewsReportedByDevMode(browser, "http://localhost:" + port + "/");
       developmentTimeViewNames.forEach(System.out::println);
   }
}
