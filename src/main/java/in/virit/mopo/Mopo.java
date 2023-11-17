package in.virit.mopo;

import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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
}
