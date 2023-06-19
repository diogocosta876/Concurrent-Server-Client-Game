package com.example.assignmentdistributed.user;

import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.Rank;
import com.example.assignmentdistributed.SocketMessage;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class GameView {
    private static Label leftPlayerDamageLabel;
    private static Label rightPlayerDamageLabel;
    private static Label actionLabel;
    private static Label effectivenessLabel;
    private static Button attackButton;


    public static void run(Stage stage, Game game, String username, int mode, Integer rank, ObjectOutputStream output) {
        String leftPlayerName = game.P1;
        String rightPlayerName = game.P2;

        Label leftPlayerNameLabel = new Label(leftPlayerName);
        leftPlayerNameLabel.setAlignment(Pos.TOP_CENTER);
        leftPlayerDamageLabel = new Label(Integer.toString(game.P1_currentChampHealth));
        leftPlayerDamageLabel.setAlignment(Pos.CENTER);
        Label rightPlayerNameLabel = new Label(rightPlayerName);
        rightPlayerNameLabel.setAlignment(Pos.TOP_CENTER);
        rightPlayerDamageLabel = new Label(Integer.toString(game.P2_currentChampHealth));
        rightPlayerDamageLabel.setAlignment(Pos.CENTER);
        actionLabel = new Label(game.tooltip);
        actionLabel.setAlignment(Pos.CENTER);
        effectivenessLabel = new Label(game.attackEffectiveness);
        effectivenessLabel.setAlignment(Pos.CENTER);

        Image championP1 = new Image("/com/example/assignmentdistributed/ChampionP1.png");
        Image championP2 = new Image("/com/example/assignmentdistributed/ChampionP2.png");

        // Create a layout for the labels
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(20));
        layout.setHgap(100);
        layout.setVgap(30);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #F5F5F5;");

        layout.add(leftPlayerNameLabel, 0, 0);
        layout.add(leftPlayerDamageLabel, 0, 2);
        ImageView imageView1 = new ImageView(championP1);
        layout.add(imageView1, 0, 1);
        imageView1.setFitWidth(150);
        imageView1.setFitHeight(150);
        layout.add(rightPlayerNameLabel, 2, 0);
        layout.add(rightPlayerDamageLabel, 2, 2);
        ImageView imageView2 = new ImageView(championP2);
        imageView2.setFitWidth(150);
        imageView2.setFitHeight(150);
        layout.add(imageView2, 2, 1);
        layout.add(actionLabel, 1, 1);
        layout.add(effectivenessLabel, 1, 2);

        GridPane.setHalignment(leftPlayerNameLabel, HPos.CENTER);
        GridPane.setHalignment(imageView1, HPos.CENTER);
        GridPane.setHalignment(leftPlayerDamageLabel, HPos.CENTER);
        GridPane.setHalignment(rightPlayerNameLabel, HPos.CENTER);
        GridPane.setHalignment(imageView2, HPos.CENTER);
        GridPane.setHalignment(rightPlayerDamageLabel, HPos.CENTER);
        GridPane.setHalignment(effectivenessLabel, HPos.CENTER);

        leftPlayerNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        leftPlayerDamageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        rightPlayerNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        rightPlayerDamageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        actionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        effectivenessLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        leftPlayerDamageLabel.setStyle("-fx-text-fill: red;");
        rightPlayerDamageLabel.setStyle("-fx-text-fill: red;");

        if(mode == 1) {
            stage.setTitle("Simple Game Window");

            Label simpleLabel = new Label("Simple Mode");
            simpleLabel.setAlignment(Pos.CENTER);
            layout.add(simpleLabel, 1, 0);
            GridPane.setHalignment(simpleLabel, HPos.CENTER);
            simpleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        }
        else {
            stage.setTitle("Ranked Game Window");

            if (rank != null) {
                Label rankLabel = new Label("Ranked Mode: " + Rank.getRankByLevel(rank));
                rankLabel.setAlignment(Pos.CENTER);
                layout.add(rankLabel, 1, 0);
                GridPane.setHalignment(rankLabel, HPos.CENTER);
                rankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            }
        }

        attackButton = new Button("Attack");
        layout.add(attackButton, 1, 3);
        GridPane.setHalignment(attackButton, HPos.CENTER);

        if (!game.turn.equals(username)) attackButton.setDisable(true);

        attackButton.setOnAction(event -> {
            System.out.println("button pressed");
            try {
                SocketMessage attackMessage = new SocketMessage(SocketMessage.GAME_ATTACK);

                output.writeObject(attackMessage);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Scene scene = new Scene(layout, 850, 400);

        stage.setScene(scene);

        stage.show();
    }

    public static void updateView(Boolean turn, String tooltip, String attack_effectiveness, int P1_health, int P2_health) {
        System.out.println(tooltip);
        System.out.println(attack_effectiveness);
        // Update the action label with the attacker name and damage value
        actionLabel.setText(tooltip);
        effectivenessLabel.setText(attack_effectiveness);
        if (attack_effectiveness.equals("Weak attack!")) effectivenessLabel.setStyle("-fx-text-fill: blue;");
        if (attack_effectiveness.equals("Normal attack!")) effectivenessLabel.setStyle("-fx-text-fill: orange;");
        if (attack_effectiveness.equals("Critical hit!")) effectivenessLabel.setStyle("-fx-text-fill: red;");
        leftPlayerDamageLabel.setText(Integer.toString(P1_health));
        rightPlayerDamageLabel.setText(Integer.toString(P2_health));

        attackButton.setDisable(!turn);
    }
}