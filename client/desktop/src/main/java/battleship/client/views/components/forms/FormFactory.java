package battleship.client.views.components.forms;

import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

/**
 * Form Factory
 */
public class FormFactory {

    /** Spacing between individual form input fields */
    public static final int SPACING = 16;

    /**
     * Returns a constructed Form Input Field bidirectionally bound to provided text property
     * @param label Input field label
     * @param textProperty Input field text field text property
     * @return Form Input Field
     */
    public static FormInputField getBidirectionallyBoundFormInputField(String label, StringProperty textProperty) {
        FormInputField formInputField = new FormInputField(label);
        formInputField.getTextField().textProperty().bindBidirectional(textProperty);
        return formInputField;
    }

    /**
     * Returns a constructed Button with set provided mouse click handler
     * @param text Button text
     * @param handler Mouse click handler
     * @return Button
     */
    public static Button getButton(String text, EventHandler<? super MouseEvent> handler) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setOnMouseClicked(handler);
        return button;
    }

}
