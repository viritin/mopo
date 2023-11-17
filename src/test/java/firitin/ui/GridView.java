package firitin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import firitin.testdomain.Person;
import firitin.testdomain.Service;

@Route
public class GridView extends VerticalLayout {
    Grid<Person> grid = new Grid<>(Person.class);
    TextField filterText = new TextField();
    ContactForm form;

    public GridView() {
        Service.initTestData();
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();

        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private HorizontalLayout getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form = new ContactForm();
        form.setWidth("25em");
        form.addSaveListener(this::saveContact); // <1>
        form.addDeleteListener(this::deleteContact); // <2>
        form.addCloseListener(e -> closeEditor()); // <3>
    }

    private void saveContact(ContactForm.SaveEvent event) {
        System.out.println("saved!");
        updateList();
        closeEditor();
    }

    private void deleteContact(ContactForm.DeleteEvent event) {
        throw new RuntimeException("Not implemented");
    }

    private void configureGrid() {
        grid.setId("contact-grid");
        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("firstName", "lastName");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event ->
                editContact(event.getValue()));
    }

    private Component getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> {
            // Add slowness to simulate a real backend
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            updateList();
        });

        Button addContactButton = new Button("Add contact");
        addContactButton.addClickListener(click -> addContact());

        var toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    public void editContact(Person contact) {
        if (contact == null) {
            closeEditor();
        } else {
            form.setContact(contact);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setContact(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void addContact() {
        grid.asSingleSelect().clear();
        editContact(new Person());
    }


    private void updateList() {
        grid.setItems(q -> {
            String f = filterText.getValue();
            return Service.findByFirstName(f, q.getOffset(), q.getLimit()).stream();
        });
    }

}
