package firitin.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route
public class AddOnHelpersView extends VerticalLayout {

    public AddOnHelpersView() {
        add("Test view");

        add(new Button("Throw JS exception", e -> {
            // deliberately cause a JS exception
            getElement().executeJs("window.foo();");
        }));
    }
}
