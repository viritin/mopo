package in.virit.mopo;

import com.microsoft.playwright.Locator;

import java.time.LocalDateTime;

/**
 * A helper class to work with vaadin-date-time-picker
 */
public class DateTimePickerPw {

    private final Locator root;

    /**
     * Creates a DateTimePicker page object for the given locator.
     *
     * @param gridLocator the Playwright locator for the vaadin-date-time-picker
     * to be interacted with
     */
    public DateTimePickerPw(Locator gridLocator) {
        this.root = gridLocator;
    }

    /**
     * Sets the current value of this field
     *
     * @param value the value to be set
     */
    public void setValue(LocalDateTime value) {
        root.evaluate("db => db.value = '%s'".formatted(value));
    }

    /**
     * Gets the currently set value of the field and parses it as
     * {@link LocalDateTime}.
     *
     * @return the current value of the field
     */
    public LocalDateTime getValue() {
        String str = (String) root.evaluate("db => db.value");
        try {
            return LocalDateTime.parse(str);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Gets the string currently in the input field defining the date part.
     *
     * @return the string value as it is formatted in the field. Note, this may
     * be locale dependent.
     */
    public String getDateInputString() {
        return root.locator("vaadin-date-picker input").inputValue();
    }

    /**
     * Returns the string currently in the input defining the time part
     *
     * @return the string value as it is formatted in the field. Note, this may
     * be locale dependent.
     */
    public String getTimeInputString() {
        return root.locator("vaadin-time-picker input").inputValue();
    }
}
