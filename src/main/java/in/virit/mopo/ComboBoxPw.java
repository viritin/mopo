package in.virit.mopo;

import com.microsoft.playwright.Locator;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Helper class to work with vaadin-combo-box component
 */
public class ComboBoxPw {

    private final Locator root;

    /**
     * Creates a ComboBox page object for the given locator.
     *
     * @param gridLocator the Playwright locator for the combobox to be
     * interacted with
     */
    public ComboBoxPw(Locator gridLocator) {
        this.root = gridLocator;
    }

    /**
     * Searches for given term in the ComboBox and picks the first suggestion.
     *
     * @param filter the string to be searched for
     */
    public void filterAndSelectFirst(String filter) {
        filter(filter);
        root.locator("input").press("Enter");
    }

    /**
     * Fills given filter to the combobox.
     *
     * @param filter the filter string to be set
     * @return the {@link ComboBoxPw} (for fluent API)
     */
    public ComboBoxPw filter(String filter) {
        root.locator("input").clear();
        root.locator("input").fill(filter);
        return this;
    }

    /**
     * Selects a given option from currently open suggestions.
     *
     * @param option the text of the option to select
     * @return the ComboBoxPw for further configuration
     */
    public ComboBoxPw selectOption(String option) {
        selectionDropdown()
                // For reason unknown to me, there are sometimes duplicates
                // of some items that are "hidden" (and hidden with css), skip those
                .locator("vaadin-combo-box-item:not([hidden])")
                .getByText(option, new Locator.GetByTextOptions().setExact(true))
                .click();
        assertThat(selectionDropdown()).isHidden();
        return this;
    }

    /**
     * Returns a locator for the combobox overlay that contains current
     * suggestions
     *
     * @return the locator for selection overlay
     */
    public Locator selectionDropdown() {
        // there can be only one
        return root.page().locator("vaadin-combo-box-overlay");
    }

    /**
     * Clicks on the dropdown toggle button in the ComboBox to show the options.
     *
     * @return a Locator to the selection dropdown
     */
    public Locator openDropDown() {
        root.locator("#toggleButton").click();
        return selectionDropdown();
    }

}
