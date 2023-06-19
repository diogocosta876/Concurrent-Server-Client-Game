package com.example.assignmentdistributed.server;

import com.example.assignmentdistributed.Database;
import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.SocketMessage;
import com.example.assignmentdistributed.TokenGenerator;
import com.example.assignmentdistributed.user.User;

import java.io.*;
import java.net.Socket;

public class UserHandler extends Thread implements Serializable {
    private transient final Socket userSocket;
    private transient final Server server;
    public transient ObjectInputStream input;
    public transient ObjectOutputStream output;

    public User user;
    public int mode;
    private volatile boolean shouldStartGame = false;
    private Game game;
    private volatile boolean userIsOn = false;
    public volatile boolean gameOver = false;
    public volatile boolean userAttack = false;

    public UserHandler(Socket socket, Server mainServer) {
        userSocket = socket;
        server = mainServer;

        try {
            System.out.println("SERVER trying to setup connection");
            output = new ObjectOutputStream(userSocket.getOutputStream());
            input = new ObjectInputStream(userSocket.getInputStream());
            this.userSocket.setSoTimeout(200);
        } catch (IOException e) {
            System.out.println("Failed");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            boolean isAuthenticated = false;
            while(!isAuthenticated) {
                SocketMessage authenticationInfo = getUserMessageResponse();
                if (authenticationInfo != null && authenticationInfo.getCode() == SocketMessage.LOGIN_REQUEST) {
                    System.out.println("SERVER - login request received");
                    if (dealWithLogin(authenticationInfo)) {
                        isAuthenticated = true;
                    } else
                        output.writeObject(new SocketMessage(SocketMessage.LOGIN_REJECTED));
                }
                 else if (authenticationInfo != null && authenticationInfo.getCode() == SocketMessage.REGISTER_REQUEST) {
                    System.out.println("SERVER - Register request received");
                    if (dealWithRegister(authenticationInfo)) {
                        output.writeObject(new SocketMessage(SocketMessage.REGISTER_SUCCESSFUL));
                    } else
                        output.writeObject(new SocketMessage(SocketMessage.REGISTER_REJECTED));
                }
            }

            System.out.println("user gameToken: " + user.getGameToken());
            // check if user has a game going
            if (!user.getGameToken().equals("")) {
                System.out.println("Game already in progress for this user");

                game = Server.getCorrespondentGame(user.getGameToken(), this);
                if (game != null)
                    mode = game.mode;

                System.out.println("Mode: " + mode);
                output.writeObject(new SocketMessage(SocketMessage.LOGIN_SUCCESSFUL, "game"));

                startGame();
            } else { //askGameMode and then queue
                output.writeObject(new SocketMessage(SocketMessage.LOGIN_SUCCESSFUL, ""));

                askGameMode();
                inQueue();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                userSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SocketMessage getUserMessageResponse(){
        SocketMessage response = null;
        try {
            response = (SocketMessage) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            //throw new RuntimeException(e);
        }
        return response;
    }

    public boolean dealWithLogin(SocketMessage loginInfo) {
        Object[] arguments = loginInfo.getArguments();
        String username = (String) arguments[0];
        String password = (String) arguments[1];
        System.out.println(username);
        System.out.println(password);
        user = Database.authenticate(username, password);
        return (user != null);
    }

    public boolean dealWithRegister(SocketMessage registerInfo) {
        Object[] arguments = registerInfo.getArguments();
        String username = (String) arguments[0];
        String password = (String) arguments[1];
        User user = new User(username, password, TokenGenerator.generateToken(username, password));
        System.out.println(username);
        System.out.println(password);
        return Database.register(user);
    }

    public void askGameMode() {
        System.out.println("ASKING GAME MODE!");
        SocketMessage user_message = null;
        while (user_message == null) {
            user_message = getUserMessageResponse();
            if (user_message != null && user_message.getCode() == SocketMessage.GAME_MODE) {
                Object[] arguments = user_message.getArguments();
                mode = (Integer) arguments[0];
            } else
                user_message = null;
        }
        System.out.println("MODE: " + mode);
    }

    public void inQueue() throws IOException, ClassNotFoundException {
        if (mode == 1) {
            server.addUserToSimpleQueue(this);
            int queue_length = server.getSimpleQueueLength();
            output.writeObject(new SocketMessage(SocketMessage.GAME_MODE, queue_length));

            shouldStartGame = false;
            while (!shouldStartGame) {
                if (server.getSimpleQueueLength() != queue_length){
                    System.out.println("A ENVIAR SOCKET UPDATE QUEUE");
                    try {
                        output.writeObject(new SocketMessage(SocketMessage.UPDATE_QUEUE_LENGTH, server.getSimpleQueueLength()));
                        output.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    queue_length = server.getSimpleQueueLength();
                }

                SocketMessage user_message = getUserMessageResponse();
                if (user_message != null && user_message.getCode() == SocketMessage.EXIT_QUEUE) {
                    server.removeUserFromSimpleQueue(this);
                }
            }
        }
        else { //ranking
            server.addUserToRankedQueue(this);
            int rank = Integer.parseInt(user.getLevel());
            int queue_length = server.getRankedQueueLength(rank);
            output.writeObject(new SocketMessage(SocketMessage.GAME_MODE, queue_length, rank));

            shouldStartGame = false;
            while (!shouldStartGame) {
                if (server.getRankedQueueLength(rank) != queue_length){
                    System.out.println("A ENVIAR SOCKET UPDATE QUEUE");
                    try {
                        output.writeObject(new SocketMessage(SocketMessage.UPDATE_QUEUE_LENGTH, server.getRankedQueueLength(rank)));
                        output.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    queue_length = server.getRankedQueueLength(rank);
                }

                SocketMessage user_message = getUserMessageResponse();
                if (user_message != null && user_message.getCode() == SocketMessage.EXIT_QUEUE) {
                    server.removeUserFromRankedQueue(this);
                }
            }
        }

        startGame();
    }

    public void shouldStartGame() {
        shouldStartGame = true;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isUserOn() {
        return userIsOn;
    }

    public void startGame() {
        try {
            if(mode == 0)
                output.writeObject(new SocketMessage(SocketMessage.START_GAME, game));
            else {
                output.writeObject(new SocketMessage(SocketMessage.START_GAME, game, Integer.parseInt(user.getLevel())));
            }
            output.flush();

            gameOver = false;
            while (!gameOver) {
                SocketMessage user_message = getUserMessageResponse();

                if (user_message != null && user_message.getCode() == SocketMessage.STILL_HERE) {
                    userIsOn = true;
                }
                if(user_message != null && user_message.getCode() == SocketMessage.GAME_RECONNECTION) {
                    userIsOn = false;
                }
                if(user_message != null && user_message.getCode() == SocketMessage.GAME_ATTACK) {
                    userAttack = true;
                }
            }

            onGameOver();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onGameUpdate() {
        System.out.println(game.tooltip);

        try {
            output.writeObject(new SocketMessage(SocketMessage.UPDATE_GAME, game.turn, game.tooltip,
                    game.attackEffectiveness, game.P1_currentChampHealth, game.P2_currentChampHealth));
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGameOver() throws IOException, ClassNotFoundException {
        try {
            System.out.println("sending game over ");
            if(userIsOn && game.tooltip.equals("")) {
                if(mode == 1)
                    output.writeObject(new SocketMessage(SocketMessage.GAME_OVER, game.getWinner(), game.tooltip));
                else {
                    if(game.getWinner().equals(user.getUsername()))
                        output.writeObject(new SocketMessage(SocketMessage.GAME_OVER, game.getWinner(),
                                "You WON the game! Your current points are " + user.getLevel()));
                    else
                        output.writeObject(new SocketMessage(SocketMessage.GAME_OVER, game.getWinner(),
                                "You LOST the game! Your current points are " + user.getLevel()));
                }
            }
            else if(userIsOn && !game.tooltip.equals("")) {
                output.writeObject(new SocketMessage(SocketMessage.GAME_OVER, game.getWinner(), game.tooltip));
            }
            output.flush();
        } catch (Exception e){
            e.printStackTrace();
        }

        while (true){
            SocketMessage user_message = getUserMessageResponse();

            if (user_message != null && user_message.getCode() == SocketMessage.LOGOUT_REQUEST) {
                System.out.println("User has logged out");
                break;
            }
            if (user_message != null && user_message.getCode() == SocketMessage.GAME_MODE) {
                Object[] arguments = user_message.getArguments();
                mode = (Integer) arguments[0];
                inQueue();
                break;
            }
        }
    }
}
