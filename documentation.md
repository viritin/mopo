# Mopo Documentation

Mopo is a small helper library to make testing Vaadin applications with Microsoft Playwright easier from Java. 
It provides Page Object helpers for complex Vaadin components (Grid, Date Picker, Date Time Picker, Combo Box) and 
a set of utility methods to interact with Vaadin-specific client/server behavior.

For installation and a short intro, see README.md. This document focuses on practical examples taken directly from the 
integration tests in this repository.

## Utilities: Mopo

Mopo provides utilities around Playwright and Vaadin, such as:
- waitForConnectionToSettle(): waits for Vaadin client/server requests to settle
- click(Locator)/click(String): reliable clicking helper
- trackClientSideErrors(), getClientSideErrors(), failOnClientSideErrors(): console error tracking helpers
- assertNoJsErrors(): asserts dev tools shows no JS errors (when available in dev mode)
- getViewsReportedByDevMode(): list available routes reported by Vaadin DevTools
- driveIn(): execute code in the context of an element/shadow root

Example usage (from AddonHelpersIT):

```java
// Track JS console errors and ensure page stays clean
mopo.trackClientSideErrors();
page.navigate("http://localhost:" + port + "/addonhelpers");

mopo.waitForConnectionToSettle();

mopo.getClientSideErrors().clear(); // clear expected favicon warning

Assertions.assertEquals(0, mopo.getClientSideErrors().size(),
    "There should be no console errors after the page has loaded");

// Trigger an exception on purpose and fail the test if there are errors
page.getByText("Throw JS exception").click();
boolean thrown = false;
try {
    mopo.failOnClientSideErrors();
} catch (RuntimeException e) {
    thrown = true;
}
Assertions.assertTrue(thrown, "There should be an exception thrown");
```

List routes from Vaadin DevTools (dev mode):

```java
List<String> developmentTimeViewNames = mopo.getViewsReportedByDevMode(browser, "http://localhost:" + port + "/");
developmentTimeViewNames.forEach(System.out::println);
```

Tip: When using inputs whose value is updated with server round-trips or lazy value-changed listeners, prefer calling 
`mopo.waitForConnectionToSettle()` before asserting.

## Grid: GridPw

GridPw wraps vaadin-grid to provide convenient methods:
- getRenderedRowCount()
- getFirstVisibleRowIndex(), getLastVisibleRowIndex()
- scrollToIndex(int)
- selectRow(int)
- getRow(int).getCell(int | String headerText)
- RowPw.select()

Example (adapted from GridPlaywrightIT):

```java
page.navigate("http://localhost:" + port + "/grid");
GridPw grid = new GridPw(page);

// Basic scrolling API
assertEquals(0, grid.getFirstVisibleRowIndex());
grid.scrollToIndex(10);
assertEquals(10, grid.getFirstVisibleRowIndex());
grid.scrollToIndex(0);

// Select first row and edit via a form bound to selection
String originalFirstName = grid.getRow(0).getCell(0).textContent();
grid.getRow(0).select();

// Update the "First name" field in the edit form
String newFirstName = originalFirstName + "_changed0";
page.getByLabel("First name", new Page.GetByLabelOptions().setExact(true)).fill(newFirstName);
page.getByText("Save").click();

mopo.waitForConnectionToSettle();

// Verify via both index and header
assertThat(grid.getRow(0).getCell(0)).hasText(newFirstName);
assertThat(grid.getRow(0).getCell("First Name")).hasText(newFirstName);
```

Filtering example showing lazy server round-trips:

```java
page.getByPlaceholder("Filter by name...").locator("input").fill("Alice");
// Filter input has lazy value change listener
mopo.waitForConnectionToSettle();
assertEquals(1, grid.getRenderedRowCount());
```

## Date Picker: DatePickerPw

DatePickerPw helps reading/writing values of vaadin-date-picker using LocalDate.
- getValue(): LocalDate (returns null if field is empty or unparsable)
- setValue(LocalDate)
- getInputString(): raw input value as string (locale formatted)

Example (from DatePickerIT):

```java
page.navigate("http://localhost:" + port + "/date");
DatePickerPw datePickerPw = new DatePickerPw(page.locator("#dp"));

LocalDate localDate = LocalDate.of(2001, 12, 24);
assertNull(datePickerPw.getValue());

datePickerPw.setValue(localDate);
assertEquals(localDate, datePickerPw.getValue());
assertThat(page.locator("#dpValue")).containsText(localDate.toString());

// The view has a button that sets the date to now on the server
LocalDate now = LocalDate.now();
mopo.click(page.getByText("set now"));
String valueInField = datePickerPw.getInputString();
String formattedNow = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US).format(now);
assertEquals(formattedNow, valueInField);
assertThat(page.locator("#dpValue")).containsText(now.toString());
```

## Date Time Picker: DateTimePickerPw

DateTimePickerPw (see source for methods) provides helpers for vaadin-date-time-picker, including reading the separate 
date and time inputs as strings formatted according to locale.

Example (from DatePickerIT):

```java
DateTimePickerPw dateTimePickerPw = new DateTimePickerPw(page.locator("#dtp"));
LocalDateTime localDateTime = LocalDateTime.of(2001,12,24,22,36,0,0);

dateTimePickerPw.setValue(localDateTime);
assertThat(page.locator("#dtpValue")).containsText(localDateTime.toString());

String dateInputValue = dateTimePickerPw.getDateInputString();
String timeInputValue = dateTimePickerPw.getTimeInputString();
assertEquals("12/24/2001", dateInputValue);
assertEquals("10:36:00 PM", timeInputValue);
```

## Combo Box: ComboBoxPw

ComboBoxPw offers convenience around vaadin-combo-box:
- filter(String)
- filterAndSelectFirst(String)
- selectOption(String)
- openDropDown(): Locator
- selectionDropdown(): Locator

Example usage (from ComboBoxIT):

```java
page.navigate("http://localhost:" + port + "/combobox");

ComboBoxPw cbPw = new ComboBoxPw(page.locator("vaadin-combo-box"));
Locator value = page.locator("#value");

// Type filter and pick first suggestion
cbPw.filterAndSelectFirst("foo");
assertThat(value).containsText("foo");

// Type partial and select a specific option from the overlay
cbPw.filter("ba").selectOption("baz");
assertThat(value).containsText("baz");

// Open dropdown via toggle and click an option
cbPw.openDropDown().getByText("foo").click();
assertThat(value).containsText("foo");
```

Raw Playwright example (no helper) for comparison:

```java
Locator cb = page.locator("input[role='combobox']");
cb.fill("foo");
page.waitForTimeout(1000); // sometimes needed with newer Vaadin versions
cb.press("Enter");
```

## Tips

- Prefer locating by ids or labels where possible: `page.getByLabel("First name", new Page.GetByLabelOptions().setExact(true))`.
- Vaadin components often update state via server round-trips. After interactions that trigger server-side logic, use `mopo.waitForConnectionToSettle()` before asserting.
- For combo box suggestions, ensure the overlay is visible and skip hidden duplicates: `vaadin-combo-box-item:not([hidden])`.
- Use Mopo.click() wrapper to avoid flakiness with certain shadow DOM elements.

## References

- Source helpers: src/main/java/in/virit/mopo/
- Integration tests with examples: src/test/java/firitin/pw/
- README: README.md
