package firitin.pw;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import in.virit.mopo.DatePickerPw;
import in.virit.mopo.DateTimePickerPw;
import in.virit.mopo.Mopo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("playwright")
public class DatePickerIT {
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
    public void doStuffWithDatePickerPw() {

        page.navigate("http://localhost:" + port + "/date");

        LocalDate localDate = LocalDate.of(2001,12,24);

        DatePickerPw datePickerPw = new DatePickerPw(page.locator("#dp"));

        LocalDate value = datePickerPw.getValue();
        assertNull(value);

        datePickerPw.setValue(localDate);

        value = datePickerPw.getValue();

        assertEquals(localDate, value);

        assertThat(page.locator("#dpValue")).containsText(localDate.toString());

        LocalDate now = LocalDate.now();
        mopo.click(page.getByText("set now"));
        value = datePickerPw.getValue();
        String valueInField = datePickerPw.getInputString();


        String formattedNow = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US).format(now);

        assertEquals(formattedNow, valueInField);
        assertThat(page.locator("#dpValue")).containsText(now.toString());

        // and the same with time...

        DateTimePickerPw dateTimePickerPw = new DateTimePickerPw(page.locator("#dtp"));

        LocalDateTime localDateTime = LocalDateTime.of(2001,12,24,22,36,0,0);
        dateTimePickerPw.setValue(localDateTime);
        assertThat(page.locator("#dtpValue")).containsText(localDateTime.toString());

        String dateInputValue = dateTimePickerPw.getDateInputString();
        String timeInputValue = dateTimePickerPw.getTimeInputString();
        assertEquals("12/24/2001", dateInputValue);
        assertEquals("10:36:00 PM", timeInputValue);

        System.out.println("Success!!");

    }

    @Test
    public void doStuffWithRawApi() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yyyy HH:mm");

        page.navigate("http://localhost:" + port + "/date");

        LocalDate localDate = LocalDate.of(2001,12,24);

        String formatted = localDate.format(dateFormatter);

        Locator dateInput = page.locator("#dp input");

        dateInput.fill(formatted);
        dateInput.press("Enter");

        assertThat(page.locator("#dpValue")).containsText(localDate.toString());

        LocalDate now = LocalDate.now();
        mopo.click(page.getByText("set now"));
        String valueInField = dateInput.inputValue();
        String formattedNow = dateFormatter.format(now);

        assertEquals(formattedNow, valueInField);
        assertThat(page.locator("#dpValue")).containsText(now.toString());


        // and the same with time...

        LocalDateTime localDateTime = LocalDateTime.of(2001,12,24,22,36,0,0);
        String formattedDateTime = localDateTime.format(dateTimeFormatter);

        Locator dtpDateInput = page.locator("#dtp vaadin-date-picker input");
        Locator dtpTimeInput = page.locator("#dtp vaadin-time-picker input");
        dtpTimeInput.clear();
        dtpTimeInput.fill(timeFormatter.format(localDateTime));
        dtpDateInput.clear();
        dtpDateInput.fill(dateFormatter.format(localDateTime));
        dtpDateInput.press("Enter");

        assertThat(page.locator("#dtpValue")).containsText(localDateTime.toString());

        System.out.println("Success!!");
    }

}
