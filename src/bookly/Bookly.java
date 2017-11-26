/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bookly;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import bookly.*;

/**
 *
 * @author Machi
 */
public class Bookly extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Bookly - your personal library");
        Parent myPane = FXMLLoader.load(getClass().getResource("Bookly.fxml"));
        Scene scene = new Scene(myPane);
//        table.setItems(data);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
