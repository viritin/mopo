package in.virit.mopo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * General utilities for Playwright & Vaadin.
 */
public class Mopo {

    private final Page page;

    public Mopo(Page page) {
        this.page = page;
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

    public void waitForConnectionToSettle() {
        waitForConnectionToSettle(page);
    }

    /**
     * Asserts that there are no JS errors in the dev console.
     */
    public void assertNoJsErrors() {
        assertNoJsErrors(page);
    }

    public List<String> getDevelopmentTimeViewNames(Browser browser, Page page) {
        List<String> urls = new ArrayList<>();

        String url = page.url();
        String substring = url.substring(0, url.lastIndexOf("/"));
        String temp = substring + "/kjhgfhjkhgb";
        Page page1 = browser.newPage();
        page1.navigate(temp);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        page1.waitForSelector("#outlet a");

        List<ElementHandle> anchors = page1.locator("#outlet a").elementHandles();
        for (ElementHandle anchor : anchors) {
            String href = anchor.getAttribute("href");
            if (href != null) {
                urls.add(substring + "/" + href);
            }
        }

        page1.close();
        return urls;
    }

    /**
     * Executes given task in a temporarily visible UI part, like a dialog or
     * form. The UI part is expected to be detached after the task.
     *
     * @param locator a locator to the UI part or a part within it (like a
     * "Save" button)
     * @param taskToRun the task that should be performed in the temporarily
     * visible component (composition)
     */
    public void driveIn(String selector, Runnable taskToRun) {
        driveIn(page.locator(selector), taskToRun);
    }

    /**
     * Executes given task in a temporarily visible UI part, like a dialog or
     * form. The UI part is expected to be hidden after the task.
     *
     * @param locator a locator to the UI part or a part within it (like a
     * "Save" button). This is used to verify that the component is shown before
     * the given task is executed and hidden after the execution.
     * @param taskToRun the task that should be performed in the temporarily
     * visible component (composition)
     */
    public void driveIn(Locator locator, Runnable taskToRun) {
        assertThat(locator).isVisible();
        taskToRun.run();
        assertThat(locator).not().isVisible();
    }

    /**
     * A shorthand to click the referenced locator and to
     * {@link #waitForConnectionToSettle()}. This is a helper you can for
     * example use for "Save" button in your form, that asynchronously changes
     * content already present in the UI. When using this instead of the raw
     * Playwright click, you can assert the changed content right away without
     * further magic.
     *
     * @param locator the locator to click
     */
    public void click(Locator locator) {
        locator.click();
        waitForConnectionToSettle();
    }

    /**
     * A shorthand to click the referenced selector and to
     * {@link #waitForConnectionToSettle()}. This is a helper you can for
     * example use for "Save" button in your form, that asynchronously changes
     * content already present in the UI. When using this instead of the raw
     * Playwright click, you can assert the changed content right away without
     * further magic.
     *
     * @param selector the selector to click
     */
    public void click(String selector) {
        page.locator(selector).click();
        waitForConnectionToSettle();
    }
}
