package battleship.client.views.components;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;

public class MenuBar extends javafx.scene.control.MenuBar {

    public MenuBar() {
        construct();
    }

    private void construct() {
        getMenus().addAll(constructMenuFile(), constructMenuHelp());
    }

    private Menu constructMenuFile() {
        Menu menu = new Menu("File");

        MenuItem itemQuit = new MenuItem("Quit");
        itemQuit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        itemQuit.setOnAction((e) -> Platform.exit());
        menu.getItems().add(itemQuit);

        return menu;
    }

    private Menu constructMenuHelp() {
        Menu menu = new Menu("Help");

        MenuItem itemAbout = new MenuItem("About");
        itemAbout.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
        itemAbout.setOnAction(this::handleAbout);
        menu.getItems().add(itemAbout);

        return menu;
    }

    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("bclient - Battleship Client");
        alert.setContentText("Rules: https://en.wikipedia.org/wiki/Battleship_(game)\n\nSeminar Work of KIV/UPS, 2024\nStanislav Kafara, skafara@students.zcu.cz\nUniversity of West Bohemia, Pilsen");
        alert.showAndWait();
    }

}
