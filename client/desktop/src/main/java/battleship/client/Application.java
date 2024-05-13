package battleship.client;

import battleship.client.views.StageManager;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Application
 */
public class Application extends javafx.application.Application {

    private StageManager stageManager;

    /**
     * Initialize Application
     * @throws Exception Never
     */
    @Override
    public void init() throws Exception {
        super.init();

        stageManager = new StageManager();
    }

    /**
     * Start Application
     * @param stage Stage
     */
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

    /**
     * Launch Application
     * @param args CLI args
     */
    public static void main(String[] args) {
        launch();
    }
}
