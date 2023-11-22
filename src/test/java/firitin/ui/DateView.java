package firitin.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Route
public class DateView extends VerticalLayout {

    public DateView() {

        UI.getCurrent().setLocale(Locale.forLanguageTag("fi"));

        DatePicker datePicker = new DatePicker();
        datePicker.setId("dp");

        Paragraph dpValue = new Paragraph();
        dpValue.setId("dpValue");
        datePicker.addValueChangeListener(e -> {
            dpValue.setText(e.getValue().toString());
        });
        add(datePicker, dpValue);

        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setStep(Duration.of(1, ChronoUnit.SECONDS));
        dateTimePicker.setId("dtp");
        Paragraph dtpValue = new Paragraph();
        dtpValue.setId("dtpValue");
        add(dateTimePicker, dtpValue);

        dateTimePicker.addValueChangeListener(e -> dtpValue.setText(e.getValue().toString()));

        add(new Button("set now", e-> {
            datePicker.setValue(LocalDate.now());
            dateTimePicker.setValue(LocalDateTime.now());
        }));

    }

}
