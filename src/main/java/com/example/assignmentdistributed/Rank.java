package com.example.assignmentdistributed;

import com.example.assignmentdistributed.server.UserHandler;

public enum Rank {
    IRON(1, 300),
    BRONZE(301, 600),
    SILVER(601, 900),
    GOLD(901, 1200),
    PLATINUM(1201, 1500),
    DIAMOND(1501, 1800),
    MASTER(1801, 2100),
    CHALLENGER(2101, Integer.MAX_VALUE);

    private final int minRange;
    private final int maxRange;

    Rank(int minRange, int maxRange) {
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    public static Rank getRankByLevel(int level) {
        for (Rank rank : Rank.values()) {
            if (level >= rank.minRange && level <= rank.maxRange) {
                return rank;
            }
        }
        return null;
    }

    public static void calculatePoints(UserHandler player1, UserHandler player2) {
        int player1Points = Integer.parseInt(player1.user.getLevel());
        int player2Points = Integer.parseInt(player2.user.getLevel());

        int kFactorPlayer1 = getKFactor(player1Points);
        int kFactorPlayer2 = getKFactor(player2Points);

        double expectedScorePlayer1 = 1.0 / (1 + Math.pow(10, (player2Points - player1Points) / 400.0));
        double expectedScorePlayer2 = 1.0 - expectedScorePlayer1;

        // Calculate points change for player1
        int pointsChangePlayer1 = (int) Math.round(kFactorPlayer1 * (1 - expectedScorePlayer1));
        player1.user.setLevel(player1Points + pointsChangePlayer1);

        // Calculate points change for player2
        int pointsChangePlayer2 = (int) Math.round(kFactorPlayer2 * (0 - expectedScorePlayer2));
        int currentPoints = player2Points + pointsChangePlayer2;
        if(currentPoints >= 1)
            player2.user.setLevel(player2Points + pointsChangePlayer2);
        else
            player2.user.setLevel(1);

        // Update the points in the database
        Database.updateUsersLevels(player1.user.getUsername(), player1.user.getLevel(),
                player2.user.getUsername(), player2.user.getLevel());
    }


    private static int getKFactor(int points) {
        if (points >= Rank.IRON.minRange && points <= Rank.IRON.maxRange) {
            return 32;
        } else if (points >= Rank.BRONZE.minRange && points <= Rank.BRONZE.maxRange) {
            return 24;
        } else if (points >= Rank.SILVER.minRange && points <= Rank.SILVER.maxRange) {
            return 20;
        } else if (points >= Rank.GOLD.minRange && points <= Rank.GOLD.maxRange) {
            return 16;
        } else if (points >= Rank.PLATINUM.minRange && points <= Rank.PLATINUM.maxRange) {
            return 12;
        } else if (points >= Rank.DIAMOND.minRange && points <= Rank.DIAMOND.maxRange) {
            return 8;
        } else if (points >= Rank.MASTER.minRange && points <= Rank.MASTER.maxRange) {
            return 4;
        } else {
            return 2;
        }
    }

}