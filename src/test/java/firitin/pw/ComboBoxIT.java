package firitin.pw;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import in.virit.mopo.ComboBoxPw;
import in.virit.mopo.Mopo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("playwright")
public class ComboBoxIT {
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

    @Test
    public void rawUsage() throws InterruptedException {
        page.navigate("http://localhost:" + port + "/combobox");

        assertThat(page.locator("vaadin-combo-box")).isVisible();

        Locator value = page.locator("#value");

        assertThat(value).containsText("bar");

        Locator cb = page.locator("input[role='combobox']");

        cb.fill("foo");
        // seems to be needed with latest Vaadin versions
        page.waitForTimeout(1000);
        cb.press("Enter");

        assertThat(value).containsText("foo");

        cb.fill("ba");
        Locator overlay = page.locator("vaadin-combo-box-overlay");
        // this should be third option & visible
        overlay.getByText("baz").click();

        assertThat(value).containsText("baz");


        // Show options with the arrow down click
        page.locator("vaadin-combo-box #toggleButton").click();

        //pick first option
        page.locator("vaadin-combo-box-item").first().click();
        assertThat(value).containsText("foo");

        System.out.println("Success!!");
    }

    @Test
    public void usageWithComboBoxPw() throws InterruptedException {
        page.navigate("http://localhost:" + port + "/combobox");

        // in the example only one, typically with id or label
        ComboBoxPw cbPw = new ComboBoxPw(page.locator("vaadin-combo-box"));
        assertThat(page.locator("vaadin-combo-box")).isVisible();

        Locator value = page.locator("#value");

        assertThat(value).containsText("bar");

        cbPw.filterAndSelectFirst("foo");

        assertThat(value).containsText("foo");

        cbPw.filter("ba").selectOption("baz");

        assertThat(value).containsText("baz");

        cbPw.openDropDown().getByText("foo").click();

        assertThat(value).containsText("foo");

        System.out.println("Success!!");
    }

}
