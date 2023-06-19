package com.example.assignmentdistributed.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class GameOverView {
    public static void run(Stage stage, String winner, String tooltip, Consumer<Integer> modeCallback) {
        // Create labels for winner and ELO points
        Label winnerLabel = new Label("Winner: " + winner);
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setMinHeight(30); // Set the desired height of the spacer
        spacer.setMaxHeight(30);


        Button button1 = new Button("Play another game");
        Button button2 = new Button("Logout");
        Button button3 = new Button("Exit");

        // Set button event handlers
        button1.setOnAction(event -> {
            // Invoke modeCallback with mode 1
            modeCallback.accept(1);
        });

        button2.setOnAction(event -> {
            // Invoke modeCallback with mode 2
            modeCallback.accept(2);
        });

        button3.setOnAction(event -> {
            // Invoke modeCallback with mode 3
            modeCallback.accept(3);
        });

        // Create a vertical layout pane
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER); // Center align the content
        vbox.setSpacing(10); // Set spacing between elements
        vbox.setPadding(new Insets(10)); // Set padding around the layout

        if(!tooltip.equals("")) {
            Label reconnectionFail = new Label(tooltip);
            reconnectionFail.setFont(Font.font("Arial", 14));
            vbox.getChildren().addAll(reconnectionFail);
        }
        vbox.getChildren().addAll(winnerLabel, spacer, button1, button2, button3);

        // Create a scene with the layout and set the window title
        Scene scene = new Scene(vbox, 400, 400);
        stage.setTitle("Game Over Window");
        stage.setScene(scene);

        // Show the window
        stage.show();
    }
}
