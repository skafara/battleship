package battleship.client.views.components.forms;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Form Input Field
 */
public class FormInputField extends VBox {

    private static final int SPACING = 4;

    private TextField textField = new TextField();

    /**
     * Constructs a Form Input Field
     * @param label Input field label
     */
    public FormInputField(String label) {
        construct(label);
    }

    /**
     * Returns the input field text field
     * @return Text field
     */
    public TextField getTextField() {
        return textField;
    }

    private void construct(String label) {
        setSpacing(SPACING);

        getChildren().addAll(
                new Label(label),
                textField
        );
    }
}
