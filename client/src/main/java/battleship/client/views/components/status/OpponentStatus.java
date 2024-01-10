package battleship.client.views.components.status;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class OpponentStatus extends FlowPane {

    public OpponentStatus(BooleanProperty isResponding) {
        construct(isResponding);
    }

    private void construct(BooleanProperty isResponding) {
        Text text = new Text();
        text.textProperty().bind(
                Bindings.createStringBinding(
                        () -> getDescription(isResponding),
                        isResponding
                )
        );
        getChildren().add(text);
    }

    private String getDescription(BooleanProperty isResponding) {
        String status = isResponding.get() ? "OK" : "Not Responding";
        return String.format("Opponent: %s", status);
    }

}
