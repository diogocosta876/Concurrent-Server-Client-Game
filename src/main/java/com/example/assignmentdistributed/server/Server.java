package com.example.assignmentdistributed.server;

import com.example.assignmentdistributed.Database;
import com.example.assignmentdistributed.Game;
import com.example.assignmentdistributed.Rank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private ServerSocket serverSocket;
    private ReentrantLock lock;
    private ExecutorService userThreadPool;
    private ExecutorService gameThreadPool;
    private ExecutorService simpleMatchmakingThreadPool;
    private ExecutorService rankedMatchmakingThreadPool;
    private volatile Queue<UserHandler> simpleModeQueue;
    private Map<Rank, Queue<UserHandler>> rankedModeQueues;
    private static List<GameServer> activeGameServers = new ArrayList<>();

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            lock = new ReentrantLock();
            simpleModeQueue = new LinkedList<>();
            rankedModeQueues = new HashMap<>();
            for (int i = 0; i < Rank.values().length; i++) {
                rankedModeQueues.put(Rank.values()[i], new LinkedList<>());
            }

            userThreadPool = Executors.newCachedThreadPool();
            gameThreadPool = Executors.newCachedThreadPool();
            simpleMatchmakingThreadPool = Executors.newFixedThreadPool(1);
            rankedMatchmakingThreadPool = Executors.newFixedThreadPool(rankedModeQueues.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException, ClassNotFoundException {
        // Start the simple mode matchmaking thread
        simpleMatchmakingThreadPool.execute(this::simpleModeMatchmaking);
        // Start the ranked mode matchmaking thread for each queue
        for (Queue<UserHandler> queue : rankedModeQueues.values()) {
            rankedMatchmakingThreadPool.execute(() -> rankedModeMatchmaking(queue));
        }

        while (true) {
            try {
                System.out.println("Server running...");
                Socket userSocket = serverSocket.accept();
                UserHandler userHandler = new UserHandler(userSocket, this);
                userThreadPool.execute(userHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addUserToSimpleQueue(UserHandler userHandler) {
        lock.lock();
        try {
            simpleModeQueue.add(userHandler);
            System.out.println("SERVER - User added to queue - user: " + userHandler.user.getUsername() + "  level: " + userHandler.user.getLevel());
        } finally {
            lock.unlock();
        }
    }

    public void removeUserFromSimpleQueue(UserHandler userHandler) {
        lock.lock();
        try {
            boolean removed = simpleModeQueue.remove(userHandler);
            if (removed) {
                System.out.println("REMOVED USER " + userHandler.user.getUsername() + " FROM QUEUE");
            }
        } finally {
            lock.unlock();
        }
    }

    public int getSimpleQueueLength() {
        return simpleModeQueue.size();
    }

    // Method to manage the matchmaking queue and start games when two players are ready
    public void simpleModeMatchmaking() {
        while (true) {
            // Wait for the queue to have at least 2 players
            while (simpleModeQueue.size() < 2) {
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    // Handle the exception
                    Thread.currentThread().interrupt();
                }
            }

            // Remove the first two players from the queue
            UserHandler player1 = simpleModeQueue.remove();
            UserHandler player2 = simpleModeQueue.remove();

            // Start a new game with these two players
            GameServer gameServer = new GameServer(player1, player2);
            activeGameServers.add(gameServer);

            String gameToken = gameServer.getToken();

            player1.setGame(gameServer.getGame());
            player2.setGame(gameServer.getGame());
            player1.user.setGameToken(gameToken);
            player2.user.setGameToken(gameToken);
            Database.updateGameTokens(player1.user, player2.user);

            player1.shouldStartGame();
            player2.shouldStartGame();

            // Submit game task to the gameThreadPool
            gameThreadPool.execute(() -> {
                gameServer.start();

                // Check if the game has ended
                if (gameServer.gameEnded) {
                    activeGameServers.remove(gameServer);
                }
            });
        }
    }

    //Ranking
    public void addUserToRankedQueue(UserHandler userHandler){
        String level = userHandler.user.getLevel();
        lock.lock();
        try {
            Queue<UserHandler> queue = rankedModeQueues.get(Rank.getRankByLevel(Integer.parseInt(level)));
            queue.add(userHandler);
            System.out.println("SERVER - User added to queue - user: " + userHandler.user.getUsername() + "  level: " + level);
        } finally {
            lock.unlock();
        }
    }

    public void removeUserFromRankedQueue(UserHandler userHandler){
        String level = userHandler.user.getLevel();
        lock.lock();
        try {
            Queue<UserHandler> queue = rankedModeQueues.get(Rank.getRankByLevel(Integer.parseInt(level)));
            boolean removed = queue.remove(userHandler);
            if (removed) {
                System.out.println("REMOVED USER " + userHandler.user.getUsername() + " FROM RANKED QUEUE " + level);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getRankedQueueLength(int level){
        return rankedModeQueues.get(Rank.getRankByLevel(level)).size();
    }

    public void rankedModeMatchmaking(Queue<UserHandler> queue) { //ranking
        while (true) {
            while (queue.size() < 2) {
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    // Handle the exception
                    Thread.currentThread().interrupt();
                }
            }

            UserHandler player1 = queue.remove();
            UserHandler player2 = queue.remove();

            // Start a new game with these two players
            GameServer gameServer = new GameServer(player1, player2);
            activeGameServers.add(gameServer);

            String gameToken = gameServer.getToken();

            player1.setGame(gameServer.getGame());
            player2.setGame(gameServer.getGame());
            player1.user.setGameToken(gameToken);
            player2.user.setGameToken(gameToken);
            Database.updateGameTokens(player1.user, player2.user);

            player1.shouldStartGame();
            player2.shouldStartGame();

            gameThreadPool.execute(() -> {
                gameServer.start();

                // Check if the game has ended
                if (gameServer.gameEnded) {
                    activeGameServers.remove(gameServer);
                }
            });
        }
    }

    public static Game getCorrespondentGame(String gameToken, UserHandler userHandler) {
        for (GameServer gameServer : activeGameServers) {
            if (gameServer.getToken().equals(gameToken)) {
                gameServer.updateUserHandler(userHandler);
                return gameServer.getGame();
            }
        }
        return null;
    }
}
