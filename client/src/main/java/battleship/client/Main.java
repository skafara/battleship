package battleship.client;

import battleship.client.views.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {

    private StageManager stageManager;

    @Override
    public void init() throws Exception {
        super.init();

        stageManager = new StageManager();
    }

    @Override
    public void start(Stage stage) {
        stage.setOnCloseRequest((e) -> {
            Platform.exit();
            System.exit(0);
        });

        stageManager.setStage(stage);
        stageManager.setScene(StageManager.Scene.Index);
        stageManager.showStage();
    }

    public static void main(String[] args) {
        launch();
    }
}
