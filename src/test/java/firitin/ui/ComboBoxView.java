package firitin.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route
public class ComboBoxView extends VerticalLayout {

    public ComboBoxView() {
        add("Test view");

        ComboBox<String> cb = new ComboBox<>();
        cb.setItems("foo", "bar", "bar2","baz");
        Paragraph value = new Paragraph();
        value.setId("value");
        cb.addValueChangeListener(e -> {
            value.setText("value:" + e.getValue());
        });
        add(cb,value);

        cb.setValue("bar");
    }
}
