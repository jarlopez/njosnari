package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AgentClientGui extends Application {
    private static final String MAIN_GUI_LOC = "/ui/main.fxml";

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(MAIN_GUI_LOC));
        Scene scene = new Scene(root, 540, 400);
        stage.setTitle("njosnari -- Client");
        stage.setScene(scene);
        stage.show();
    }
}
