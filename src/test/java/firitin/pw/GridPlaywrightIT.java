package firitin.pw;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import in.virit.mopo.GridPw;
import in.virit.mopo.Mopo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("playwright")
public class GridPlaywrightIT {
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
    public void doRandomStuffAndChangeFirstRow() throws InterruptedException {
        page.navigate("http://localhost:" + port + "/grid");

        GridPw grid = new GridPw(page);
        String originalFirstName = grid.getTableRow(0).getCell(0).textContent();

        // Some warmup with simple GridPo API
        assertEquals(0, grid.getFirstVisibleRowIndex());
        grid.scrollToIndex(10);
        assertEquals(10, grid.getFirstVisibleRowIndex());
        grid.scrollToIndex(0);

        // Select the first row for editing
        grid.getTableRow(0).select();

        // Check the form is visible and contains the first row data
        assertThat(page.getByLabel("First name", new Page.GetByLabelOptions().setExact(true)))
                .hasValue(originalFirstName);

        int rowCount = grid.getRenderedRowCount();
        assertTrue(rowCount > 0);


        page.getByPlaceholder("Filter by name...").locator("input")
                .fill(originalFirstName.substring(0, 6));

        // Filter input has lazy value changelistener, so we need to wait a bit
        mopo.waitForConnectionToSettle();

        int renderedRowCount = grid.getRenderedRowCount();
        assertEquals(1, renderedRowCount);

        grid.getTableRow(0).select();

        String newFirstName = originalFirstName+"_changed0";
        if(originalFirstName.contains("_changed")) {
            int i = Integer.parseInt(originalFirstName.substring(originalFirstName.lastIndexOf("_changed") + 8));
            newFirstName = originalFirstName.substring(0, originalFirstName.lastIndexOf("_changed")) + "_changed" + (i + 1);
            if(newFirstName.length() > 15) {
                newFirstName = newFirstName.substring(0, 15);
            }
        }

        page.getByLabel("First name", new Page.GetByLabelOptions().setExact(true))
                .fill(newFirstName);

        page.getByText("Save").click();
        assertThat(page.getByText("Save")).not().isVisible();

        // Get the first cell of the first row in Grid and check text
        // TODO add API to get cell by column header text
        assertThat(grid.getTableRow(0).getCell(0)).hasText(newFirstName);
        assertThat(grid.getTableRow(0).getCell("First Name")).hasText(newFirstName);

        // An alternative way to verify without GridPo
        assertThat(page.locator("vaadin-grid-cell-content").and(page.getByText(newFirstName))).isVisible();
    }


    @Test
    public void scrollingAndAssertingContent() {
        page.navigate("http://localhost:" + port + "/grid");


        // Examples of hacks needed without GridPo, even for trivial asserts

        // This is what one might try
        String string = page.locator("#contact-grid #items td").first().textContent();
        System.out.println("string = " + string); // empty string, slot, content elsewhere :-(

        // This is how it can be done
        String name = page.locator("#contact-grid #items td slot").first().getAttribute("name");
        string = page.locator("#contact-grid vaadin-grid-cell-content[slot='%s']".formatted(name)).first().textContent();
        System.out.println("string = " + string); // Now we are talking

        // Now let's drop in GridPw helper

        GridPw grid = new GridPw(page.locator("#contact-grid"));

        System.out.println("Showing rows: %s-%s".formatted(grid.getFirstVisibleRowIndex(), grid.getLastVisibleRowIndex()));

        String cellContent;
        cellContent = grid.getTableRow(0).getCell(0).textContent();
        assertEquals("First0", cellContent);
        cellContent = grid.getTableRow(0).getCell("First Name").textContent();
        assertEquals("First0", cellContent);

        cellContent = grid.getTableRow(0).getCell(1).textContent();
        assertEquals("Lastname0", cellContent);
        cellContent = grid.getTableRow(0).getCell("Last Name").textContent();
        assertEquals("Lastname0", cellContent);

        grid.scrollToIndex(3);

        System.out.println("Showing rows: %s-%s".formatted(grid.getFirstVisibleRowIndex(), grid.getLastVisibleRowIndex()));

        cellContent = grid.getTableRow(0).getCell(0).textContent();
        assertEquals("First0", cellContent);
        cellContent = grid.getTableRow(0).getCell("First Name").textContent();
        assertEquals("First0", cellContent);

        grid.scrollToIndex(100);

        System.out.println("Showing rows: %s-%s".formatted(grid.getFirstVisibleRowIndex(), grid.getLastVisibleRowIndex()));


        // this should now automatically scroll row index 0 to be visible
        cellContent = grid.getTableRow(0).getCell(0).textContent();
        assertEquals("First0", cellContent);
        cellContent = grid.getTableRow(0).getCell("First Name").textContent();
        assertEquals("First0", cellContent);
        System.out.println("Showing rows: %s-%s".formatted(grid.getFirstVisibleRowIndex(), grid.getLastVisibleRowIndex()));
        cellContent = grid.getTableRow(101).getCell(1).textContent();
        assertEquals("Lastname101", cellContent);
        System.out.println("Showing rows: %s-%s".formatted(grid.getFirstVisibleRowIndex(), grid.getLastVisibleRowIndex()));
    }
}
