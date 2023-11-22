package in.virit.mopo;

import com.microsoft.playwright.Locator;

import java.time.LocalDate;

/**
 * A helper class to work with vaadin-date-picker component.
 */
public class DatePickerPw {

    private final Locator root;

    /**
     * Creates a DatePicker page object for the given locator.
     *
     * @param gridLocator the Playwright locator for the vaadin-date-picker to
     * be interacted with
     */
    public DatePickerPw(Locator gridLocator) {
        this.root = gridLocator;
    }

    /**
     * Returns the value from the client side and parses it as
     * {@link LocalDate}.
     *
     * @return the current value of the field
     */
    public LocalDate getValue() {
        String str = (String) root.evaluate("db => db.value");
        try {
            return LocalDate.parse(str);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value to be set
     */
    public void setValue(LocalDate value) {
        root.evaluate("db => db.value = '%s'".formatted(value));
    }

    /**
     * Returns the raw string value in the field.
     *
     * @return the string value as it is formatted in the field. Note, this may
     * be locale dependent.
     */
    public String getInputString() {
        return root.locator("input").inputValue();
    }
}
