package battleship.client.views.components.status;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

public class ServerStatus extends FlowPane {

    public ServerStatus(StringProperty address, BooleanProperty isResponding) {
        construct(address, isResponding);
    }

    private void construct(StringProperty address, BooleanProperty isResponding) {
        Text text = new Text();
        text.textProperty().bind(
                Bindings.createStringBinding(
                        () -> getDescription(address, isResponding),
                        address, isResponding
                )
        );
        getChildren().add(text);
    }

    private String getDescription(StringProperty address, BooleanProperty isResponding) {
        String status = isResponding.get() ? "OK" : "Reconnecting";
        return String.format("Server: %s (%s)", status, address.get());
    }

}
