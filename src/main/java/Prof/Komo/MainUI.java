package Prof.Komo;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;

/**
 * App front end
 * Sponsored by Seimens Foundation.
 */
public class MainUI extends Application
{
    public static void main( String[] args )
    {
        launch(args);
    }

    @Override
    public void start(Stage ps) throws Exception {
        ps.setTitle("Prof. Andrew R. M. Komo, Inc.");

        GridPane gd = new GridPane();
        gd.setAlignment(Pos.CENTER);
        gd.setHgap(10);
        gd.setVgap(10);
        gd.setPadding(new Insets(25, 25, 25, 25));

        Text scTitle = new Text("Welcome to Prof. Andrew R. M. Komo, Inc (R) Redistricting Maths App!");
        gd.add(scTitle, 0,0,3,1);

        Button btn = new Button("Exit");
        btn.setOnAction(event -> System.exit(0));
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        gd.add(hbBtn, 0, 4);

        Button b2 = new Button("Start");
        HBox hb2 = new HBox(10);
        hb2.setAlignment(Pos.BOTTOM_RIGHT);
        hb2.getChildren().add(b2);
        gd.add(hb2, 2, 4);

        ps.setScene(new Scene(gd, 500, 500));
        ps.show();
    }
}
