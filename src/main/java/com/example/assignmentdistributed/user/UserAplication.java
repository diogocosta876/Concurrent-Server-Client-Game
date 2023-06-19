package com.example.assignmentdistributed.user;

import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.Rank;
import com.example.assignmentdistributed.SocketMessage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class UserAplication extends Application {
    private Socket userSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String username;

    private int mode;

    public UserAplication() {}

    public void connectToServer() {
        String hostname = "localhost";

        try {
            InetAddress ip = InetAddress.getByName(hostname);
            userSocket = new Socket(ip, 12345);
            output = new ObjectOutputStream(userSocket.getOutputStream());
            input = new ObjectInputStream(userSocket.getInputStream());
            userSocket.setSoTimeout(200);
            System.out.println("USER - CONNECTION WAS SUCCESSFULLY SET UP");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authentication() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        connectToServer();

        drawLoginMenu(primaryStage);
    }

    public void drawLoginMenu(Stage stage) {
        // Create UI controls
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // Create layout panes and add controls
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(10);
        root.add(usernameLabel, 0, 0);
        root.add(usernameField, 1, 0);
        root.add(passwordLabel, 0, 1);
        root.add(passwordField, 1, 1);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(loginButton, registerButton);
        root.add(buttonBox, 0, 2, 2, 1);

        // Create scene and add layout pane
        Scene scene = new Scene(root, 300, 200);

        // Set stage properties and show
        stage.setScene(scene);
        stage.setTitle("Authentication Menu");
        stage.show();

        loginButton.setOnAction(event -> {
            sendLoginRequest(usernameField.getText(), passwordField.getText());

            SocketMessage response = getServerMessageResponse();
            if (response.getCode() == SocketMessage.LOGIN_SUCCESSFUL) {
                stage.close();
                username = usernameField.getText();
                Object[] arguments = response.getArguments();
                String game = (String) arguments[0];

                if (game.equals("game"))
                    draw_success_message("Login succeded! Game is starting.", stage, "startGame");
                else
                    draw_success_message("Login succeded! Choose Game Mode.", stage, "login");
            } else {
                // show error
                stage.close();
                draw_error("Invalid username or password.", stage);
            }
        });

        registerButton.setOnAction(event -> {
            if(usernameField.getText().isEmpty()) {
                stage.close();
                draw_error("Username cannot be empty!", stage);
            }
            else if(passwordField.getText().isEmpty()){
                stage.close();
                draw_error("Password cannot be empty!", stage);
            }
            else {
                sendRegistrationRequest(usernameField.getText(), passwordField.getText());

                SocketMessage response = getServerMessageResponse();
                if (response.getCode() == SocketMessage.REGISTER_SUCCESSFUL) {
                    stage.close();
                    draw_success_message("Register Succeeded! Please Login.", stage, "register");
                } else {
                    // show error
                    stage.close();
                    draw_error("Username already exists.", stage);
                }
            }
        });
    }

    public void draw_error(String error, Stage stage) {
        // Create UI controls
        Label errorLabel = new Label(error);
        Button okayButton = new Button("I understand");

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(10);
        root.add(errorLabel, 0, 0);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(okayButton);
        root.add(buttonBox, 0, 2, 2, 1);

        // Create scene and add layout pane
        Scene scene = new Scene(root, 300, 200);

        // Set stage properties and show
        Stage errorStage = new Stage();
        errorStage.setScene(scene);
        errorStage.setTitle("Error");
        errorStage.show();

        okayButton.setOnAction(event -> {
            errorStage.close();
            drawLoginMenu(stage);
        });
    }

    public void draw_success_message(String msg, Stage stage, String etapa) {
        // Create UI controls
        Label messageLabel = new Label(msg);
        Button nextButton = new Button("Next");

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.setVgap(10);
        root.add(messageLabel, 0, 0);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(nextButton);
        root.add(buttonBox, 0, 2, 2, 1);

        // Create scene and add layout pane
        Scene scene = new Scene(root, 400, 200);
        Stage successStage = new Stage();
        successStage.setScene(scene);
        successStage.setTitle("Success");
        successStage.show();

        nextButton.setOnAction(event -> {
            successStage.close();

            switch (etapa) {
                case "login" -> askGameMode(stage);
                case "register" -> drawLoginMenu(stage);
                case "gameMode" -> {
                    sendGameMode(mode);
                    inQueue(stage);
                }
                case "startGame" -> {
                    SocketMessage response = getServerMessageResponse();
                    Object[] arguments = response.getArguments();
                    Game game = (Game) arguments[0];
                    mode = game.mode;
                    Integer rank = (Integer) arguments[1];

                    startGame(stage, game, rank);
                }
                default -> {
                }
            }
        });
    }

    public SocketMessage getServerMessageResponse(){
        SocketMessage response = null;
        try {
            response = (SocketMessage) input.readObject();
        } catch (SocketException se) {
            // handle socket closed exception
            System.out.println("Socket closed: " + se.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return response;
    }

    public void sendLoginRequest(String name, String pass) {
        try {
            SocketMessage loginInfo = new SocketMessage(SocketMessage.LOGIN_REQUEST, name, pass);

            output.writeObject(loginInfo);
            output.flush();
            System.out.println("USER - WRITING login info");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRegistrationRequest(String name, String pass) {
        try {
            SocketMessage loginInfo = new SocketMessage(SocketMessage.REGISTER_REQUEST, name, pass);

            output.writeObject(loginInfo);
            output.flush();
            System.out.println("USER - WRITING registration info");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askGameMode(Stage stage) {
        System.out.println("ASKING GAME MODE");
        GameModeView.run(stage, mode -> {
            setMode(mode);
            if(mode == 1)
                draw_success_message("Simple Mode chosen Successfully! Click next to enter the queue", stage, "gameMode");
            else
                draw_success_message("Ranked Mode chosen Successfully! Click next to enter the queue", stage, "gameMode");
        });
    }

    public void sendGameMode(int mode) {
        try {
            SocketMessage gameMode = new SocketMessage(SocketMessage.GAME_MODE, mode);

            output.writeObject(gameMode);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inQueue(Stage stage){
        stage.setOnCloseRequest(event -> {
            // code to run when the window is closed
            System.out.println("Window closed - exiting queue");
            sendExitQueueRequest();
        });

        System.out.println("IN QUEUE");
        SocketMessage posGameMode = getServerMessageResponse();
        Object[] arguments = posGameMode.getArguments();

        Integer queueLength = (Integer) arguments[0];
        Integer rank;
        if (mode == 1) {
            rank = null;
            QueueView.run(stage, username, queueLength, "");
        }
        else { // ranking
            rank = (Integer) arguments[1];
            QueueView.run(stage, username, queueLength, Rank.getRankByLevel(rank).toString());
        }

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
            SocketMessage response = getServerMessageResponse();
            if (response != null && response.getCode() == SocketMessage.UPDATE_QUEUE_LENGTH) {
                System.out.println("SUCCESS");
                Object[] arguments2 = response.getArguments();
                Integer queueLength2 = (Integer) arguments2[0];
                QueueView.updateView(stage, queueLength2);
            }
            if (response != null && response.getCode() == SocketMessage.START_GAME) {
                System.out.println("Received START_GAME");
                Object[] arguments2 = response.getArguments();
                Game game = (Game) arguments2[0];

                stage.close();
                timeline.stop();
                startGame(stage, game, rank);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    public void sendExitQueueRequest() {
        try {
            SocketMessage exitRequest = new SocketMessage(SocketMessage.EXIT_QUEUE, username);

            output.writeObject(exitRequest);
            output.flush();
            System.out.println("USER - Sending Queue Exit request");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame(Stage stage, Game game, Integer rank) {
        stage.setOnCloseRequest(event -> {
            System.out.println("Window closed - Game went down");
            sendReconnectionMessage();
        });

        System.out.println("Game started! Mode: " + mode);
        GameView.run(stage, game, username , mode, rank, output);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
            SocketMessage response = getServerMessageResponse();
            if (response != null && response.getCode() == SocketMessage.UPDATE_GAME) {
                Object[] arguments = response.getArguments();
                String turn = (String) arguments[0];
                String tooltip = (String) arguments[1];
                String attack_effectiveness = (String) arguments[2];
                int P1_health = (int) arguments[3];
                int P2_health = (int) arguments[4];
                boolean player_turn = turn.equals(username);
                GameView.updateView(player_turn, tooltip, attack_effectiveness, P1_health, P2_health);
            }
            if(response != null && response.getCode() == SocketMessage.GAME_OVER) {
                System.out.println("Game is Over");
                Object[] arguments = response.getArguments();
                String winner = (String) arguments[0];
                String tooltip = (String) arguments[1];

                stage.close();
                timeline.stop();
                draw_winner(stage, winner, tooltip);
            }
            sendConfirmationMessage();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void sendReconnectionMessage() {
        try {
            SocketMessage exitRequest = new SocketMessage(SocketMessage.GAME_RECONNECTION);

            output.writeObject(exitRequest);
            output.flush();
            System.out.println("USER - Sending Queue Exit request");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendConfirmationMessage() {
        try {
            SocketMessage gameMode = new SocketMessage(SocketMessage.STILL_HERE);

            output.writeObject(gameMode);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void draw_winner(Stage stage, String winner, String tooltip) {
        GameOverView.run(stage, winner, tooltip, mode -> {
            switch (mode) {
                case 1 -> {
                    stage.close();
                    askGameMode(stage);
                }
                case 2 -> {
                    try {
                        SocketMessage logoutRequest = new SocketMessage(SocketMessage.LOGOUT_REQUEST);

                        output.writeObject(logoutRequest);
                        output.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stage.close();
                    try {
                        userSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    start(stage);
                }
                case 3 -> {
                    try {
                        SocketMessage logoutRequest = new SocketMessage(SocketMessage.LOGOUT_REQUEST);

                        output.writeObject(logoutRequest);
                        output.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stage.close();
                }
            }
        });
    }

    private void setMode(int m) {
        mode = m;
    }
}
