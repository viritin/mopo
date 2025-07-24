package in.virit.mopo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * General utilities for Playwright and Vaadin.
 */
public class Mopo {

    private final Page page;
    private List<String> clientSideErrors = new ArrayList<>();

    /**
     * Constructs a new Mopo for given page
     *
     * @param page the page use by this Mopo instance
     */
    public Mopo(Page page) {
        this.page = page;
    }

    /**
     * Waits until the client-server communication by Vaadin has settled.
     *
     * @param page the page on which Vaadin app is expected to be run
     */
    public static void waitForConnectionToSettle(Page page) {
        // Default to be bit larger than the defaults for lazy value change events
        int minWait = 500;
        waitForConnectionToSettle(page, minWait);
    }

    /**
     * Waits until the client-server communication by Vaadin has settled.
     *
     * @param page    the page on which Vaadin app is expected to be run
     * @param minWait the minimum wait time spent to watch if client-server communication starts
     */
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
     *
     * @param page the page to be checked
     * @deprecated this method depends on dev mode, consider using {@link #failOnClientSideErrors()} ()} and {@link #trackClientSideErrors()} that use browser console.
     */
    @Deprecated
    public static void assertNoJsErrors(Page page) {

        try {
            assertThat(page.locator("vaadin-dev-tools")).isVisible();
            waitForConnectionToSettle(page);
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

    /**
     * Starts monitoring browser console and errors and collects
     * those for further inspection (e.g. with {@link #getClientSideErrors()} or {@link #failOnClientSideErrors()}).
     */
    public void trackClientSideErrors() {
        page.waitForTimeout(1000);
        page.onConsoleMessage(msg -> {
            System.out.println("Console message: " + msg.type() + " " + msg.text());
            if (msg.type().equals("error")) {
                clientSideErrors.add(msg.text());
            }
        });

        page.onPageError(error -> {
            System.out.println("Page error: " + error);
            clientSideErrors.add(error);
        });
    }

    /**
     * Returns the list of console errors that have been logged since the
     * {@link #trackClientSideErrors()} was called.
     *
     * @return a list of console messages that are errors
     */
    public List<String> getClientSideErrors() {
        return clientSideErrors;
    }

    /**
     * Waits until the client-server communication by Vaadin
     * has settled.
     */
    public void waitForConnectionToSettle() {
        waitForConnectionToSettle(page);
    }

    /**
     * Asserts that there are no JS errors in the dev console.
     *
     * @deprecated this method depends on dev mode, consider using {@link #failOnClientSideErrors()} ()} and {@link #trackClientSideErrors()} that use browser console.
     */
    @Deprecated
    public void assertNoJsErrors() {
        assertNoJsErrors(page);
    }

    /**
     * Returns a list of routes/views(URLs) that Vaadin app in development mode contains.
     *
     * @param browser the browser instance
     * @param rootUrl relative URLs of the test server views that in development time lists
     * @return a list of URLs pointing to known routes
     */
    public List<String> getViewsReportedByDevMode(Browser browser, String rootUrl) {
        List<String> urls = new ArrayList<>();

        Page page1 = browser.newPage();
        page1.navigate(rootUrl);

        page1.waitForSelector("#outlet a");

        List<ElementHandle> anchors = page1.locator("#outlet a").elementHandles();
        for (ElementHandle anchor : anchors) {
            String href = anchor.getAttribute("href");
            if (href != null) {
                urls.add(href);
            }
        }
        page1.close();
        return urls;
    }

    /**
     * Executes given task in a temporarily visible UI part, like a dialog or
     * form. The UI part is expected to be detached after the task (implicitly
     * asserted).
     *
     * @param selector  a selector to the UI part to be accessed or a part within it (like a
     *                  "Save" button)
     * @param taskToRun the task that should be performed in the temporarily
     *                  visible component (composition)
     */
    public void driveIn(String selector, Runnable taskToRun) {
        driveIn(page.locator(selector), taskToRun);
    }

    /**
     * Executes given task in a temporarily visible UI part, like a dialog or
     * form. The UI part is expected to be hidden after the task.
     *
     * @param locator   a locator to the UI part or a part within it (like a
     *                  "Save" button). This is used to verify that the component is shown before
     *                  the given task is executed and hidden after the execution.
     * @param taskToRun the task that should be performed in the temporarily
     *                  visible component (composition)
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

    /**
     * Asserts that there are no client-side errors in the console.
     * Throws a RuntimeException if there are any errors.
     */
    public void failOnClientSideErrors() {
        List<String> consoleErrors = getClientSideErrors();
        if (!consoleErrors.isEmpty()) {
            StringBuilder sb = new StringBuilder("There are JS errors in the console:\n");
            for (var msg : consoleErrors) {
                sb.append(msg).append("\n");
            }
            throw new RuntimeException("JS errors discovered: "+ sb.toString());
        }
    }
}
