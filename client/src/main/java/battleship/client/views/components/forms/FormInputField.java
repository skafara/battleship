package battleship.client.views.components.forms;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class FormInputField extends VBox {

    private static final int SPACING = 4;

    TextField textField = new TextField();

    public FormInputField(String label) {
        construct(label);
    }

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
