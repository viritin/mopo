package firitin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import firitin.testdomain.Person;

public class ContactForm extends FormLayout { 
  TextField firstName = new TextField("First name"); 
  TextField lastName = new TextField("Last name");
  EmailField email = new EmailField("Email");

  Button save = new Button("Save");
  Button delete = new Button("Delete");
  Button close = new Button("Cancel");
  // Other fields omitted
  Binder<Person> binder = new BeanValidationBinder<>(Person.class);
  private Person contact;

  public ContactForm() {
    addClassName("contact-form");
    binder.bindInstanceFields(this);


    add(firstName, 
        lastName,
        email,
        createButtonsLayout());
  }

  private Component createButtonsLayout() {
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    save.addClickShortcut(Key.ENTER);
    close.addClickShortcut(Key.ESCAPE);

    save.addClickListener(event -> validateAndSave()); // <1>
    delete.addClickListener(event -> fireEvent(new DeleteEvent(this, contact))); // <2>
    close.addClickListener(event -> fireEvent(new CloseEvent(this))); // <3>

    binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid())); // <4>
    return new HorizontalLayout(save, delete, close);
  }

  private void validateAndSave() {
    if(binder.isValid()) {
      fireEvent(new SaveEvent(this, contact)); // <6>
    }
  }


  public void setContact(Person contact) {
    this.contact = contact;
    if(contact != null) {
      binder.setBean(contact);
    }
  }

  // Events
  public static abstract class ContactFormEvent extends ComponentEvent<ContactForm> {
    private Person contact;

    protected ContactFormEvent(ContactForm source, Person contact) {
      super(source, false);
      this.contact = contact;
    }

    public Person getContact() {
      return contact;
    }
  }

  public static class SaveEvent extends ContactFormEvent {
    SaveEvent(ContactForm source, Person contact) {
      super(source, contact);
    }
  }

  public static class DeleteEvent extends ContactFormEvent {
    DeleteEvent(ContactForm source, Person contact) {
      super(source, contact);
    }

  }

  public static class CloseEvent extends ContactFormEvent {
    CloseEvent(ContactForm source) {
      super(source, null);
    }
  }

  public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
    return addListener(DeleteEvent.class, listener);
  }

  public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
    return addListener(SaveEvent.class, listener);
  }
  public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
    return addListener(CloseEvent.class, listener);
  }


}

