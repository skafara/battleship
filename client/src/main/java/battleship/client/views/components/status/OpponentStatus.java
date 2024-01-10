package battleship.client.views.components.status;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class OpponentStatus extends FlowPane {

    public OpponentStatus(BooleanBinding isInRoom, BooleanProperty isResponding) {
        construct(isInRoom, isResponding);
    }

    private void construct(BooleanBinding isInRoom, BooleanProperty isResponding) {
        Text text = new Text();
        text.textProperty().bind(
                Bindings.createStringBinding(
                        () -> getDescription(isInRoom, isResponding),
                        isInRoom, isResponding
                )
        );
        getChildren().add(text);
    }

    private String getDescription(BooleanBinding isInRoom, BooleanProperty isResponding) {
        String status;
        if (!isInRoom.get()) {
            status = "...";
        }
        else {
            status = isResponding.get() ? "OK" : "Not Responding";
        }
        return String.format("Opponent: %s", status);
    }

}
