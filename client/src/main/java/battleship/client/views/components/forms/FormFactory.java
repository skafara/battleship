package battleship.client.views.components.forms;

import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class FormFactory {

    public static final int SPACING = 16;

    public static FormInputField getBidirectionalyBoundFormInputField(String label, StringProperty textProperty) {
        FormInputField formInputField = new FormInputField(label);
        formInputField.getTextField().textProperty().bindBidirectional(textProperty);
        return formInputField;
    }

    public static Button getButton(String text, EventHandler<? super MouseEvent> handler) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setOnMouseClicked(handler);
        return button;
    }

}
