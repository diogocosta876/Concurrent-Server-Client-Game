package com.example.assignmentdistributed.server;

import com.example.assignmentdistributed.Database;
import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.Rank;
import com.example.assignmentdistributed.TokenGenerator;

import java.io.Serializable;

public class GameServer implements Serializable {
    private UserHandler user1;
    private UserHandler user2;
    private final String token;

    private final Game game;

    public volatile boolean gameEnded = false;

    public GameServer(UserHandler user1, UserHandler user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.game = new Game(user1.user, user2.user, user1.mode);

        // initialize token
        this.token = TokenGenerator.generateRandomToken();
    }

    public String getToken() {
        return token;
    }

    public void start() {
        System.out.println("GameServer - Starting game with players " + user1.user.getUsername() + " and " + user2.user.getUsername());

        System.out.println(game.tooltip);
        long timeout = 20000; // Timeout period in milliseconds (1 minute)
        long startTime = System.currentTimeMillis();

        while (!game.gameOver) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (elapsedTime > timeout) {
                // set the user that is still on the winner
                if(user1.isUserOn()) {
                    if(game.mode == 1)
                        game.setTooltip(user2.user.getUsername() + " disconnected and didn't connect in time!");
                    else{
                        game.setTooltip(user2.user.getUsername() + " disconnected and didn't connect in time!\n Your current points are " + user1.user.getLevel());
                    }
                    game.setWinner(user1.user.getUsername());

                } else if(user2.isUserOn()){
                    if(game.mode == 1)
                        game.setTooltip(user1.user.getUsername() + " disconnected and didn't connect in time!");
                    else{
                        game.setTooltip(user1.user.getUsername() + " disconnected and didn't connect in time!\n Your current points are " + user2.user.getLevel());
                    }
                    game.setWinner(user2.user.getUsername());
                }

                break;
            }

            //System.out.println(user1.isUserOn() + "   " + user2.isUserOn());
            if(user1.isUserOn() && user2.isUserOn()) {
                startTime = System.currentTimeMillis();
                if (game.turn.equals(user1.user.getUsername()) && user1.userAttack){
                    game.nextTurn();
                    user1.userAttack = false;
                    user1.onGameUpdate();
                    user2.onGameUpdate();
                }
                else if (game.turn.equals(user2.user.getUsername()) && user2.userAttack){
                    game.nextTurn();
                    user2.userAttack = false;
                    user1.onGameUpdate();
                    user2.onGameUpdate();
                }
            }
        }

        if(game.gameOver) {
            if (game.mode == 2){
                if(game.getWinner().equals(user1.user.getUsername()))
                    Rank.calculatePoints(user1, user2);
                else
                    Rank.calculatePoints(user2, user1);
            }
            game.setTooltip("");
        }

        System.out.println("GAME ENDING - CLEANING TOKENS");

        user1.gameOver = true;
        user2.gameOver = true;
        Database.cleanGameTokens(token);

        gameEnded = true;
    }

    public Game getGame() {
        return game;
    }

    public void updateUserHandler(UserHandler userHandler) {
        if(user1.user.getUsername().equals(userHandler.user.getUsername()))
            user1 = userHandler;
        if(user2.user.getUsername().equals(userHandler.user.getUsername()))
            user2 = userHandler;
    }


}
