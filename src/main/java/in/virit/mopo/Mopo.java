package in.virit.mopo;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.setDefaultAssertionTimeout;

/**
 * General utilities for Playwright & Vaadin.
 */
public class Mopo {

    private final Page page;

    public Mopo(Page page) {
        this.page = page;
    }

    public void waitForConnectionToSettle() {
        waitForConnectionToSettle(page);
    }

    public static void waitForConnectionToSettle(Page page) {
        // Default to be bit larger than the defaults for lazy value change events
        int minWait = 500;
        waitForConnectionToSettle(page, minWait);
    }

    public static void waitForConnectionToSettle(Page page, int minWait) {
        long start = System.currentTimeMillis();

        // TODO, figure out if we can detect debounced events in the queue!?
        // Could return without minWait...
        page.waitForTimeout(minWait);

        // wait for the loading indicator to disappear
        assertThat(page.locator("vaadin-connection-indicator[loading]")).hasCount(0);

        // System.out.println("Waited for" + (System.currentTimeMillis() - start) + "ms");

    }

    /**
     * Asserts that there are no JS errors in the dev console.
     */
    public void assertNoJsErrors() {
        assertNoJsErrors(page);
    }

    /**
     * Asserts that there are no JS errors in the dev console.
     */
    public static void assertNoJsErrors(Page page) {

        try {
            ElementHandle elementHandle = page.waitForSelector("vaadin-dev-tools>div.error",
                    //wait for it just a tiny moment
                    new Page.WaitForSelectorOptions().setTimeout(100.0)
            );
            if (elementHandle != null) {
                String msg = page.locator("vaadin-dev-tools div.message.error").last().textContent();
                throw new AssertionError("JS error in dev console: " + msg);
            }
        } catch (com.microsoft.playwright.TimeoutError e) {
            // expected
            return;
        }
    }
}
