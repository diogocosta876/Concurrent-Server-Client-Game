package com.example.assignmentdistributed.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class GameModeView {
    public static void run(Stage stage, Consumer<Integer> modeCallback) {
        Button button1 = new Button("Simple Matchmaking");
        Button button2 = new Button("Ranked Matchmaking");

        // Set button event handlers
        button1.setOnAction(event -> {
            // Invoke modeCallback with mode 1
            modeCallback.accept(1);
            stage.close();
        });

        button2.setOnAction(event -> {
            // Invoke modeCallback with mode 2
            modeCallback.accept(2);
            stage.close();
        });

        // Create a layout for the labels
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #F5F5F5;");

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);

        layout.getChildren().addAll(button1, button2, buttonBox);

        // Create a scene with the layout and set the window title
        Scene scene = new Scene(layout, 400, 200);
        stage.setTitle("GameMode Window");
        stage.setScene(scene);

        // Show the window
        stage.show();
    }
}
