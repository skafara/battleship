package battleship.client;

import battleship.client.views.StageManager;
import javafx.application.Application;
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
        stageManager.setStage(stage);
        stageManager.setScene(StageManager.Scene.Index);
        stageManager.showStage();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}
