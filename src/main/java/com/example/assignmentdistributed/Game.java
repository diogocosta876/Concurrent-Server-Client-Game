package com.example.assignmentdistributed;

import com.example.assignmentdistributed.server.Champion;
import com.example.assignmentdistributed.user.User;

import java.io.Serializable;
import java.util.Random;

public class Game implements Serializable {
    public int mode;
    public String P1;
    public int P1_level;
    public Champion P1_champion;
    public int P1_currentChampHealth;

    public String P2;
    public int P2_level;
    public Champion P2_champion;
    public int P2_currentChampHealth;

    public String turn;
    public String tooltip;
    public String attackEffectiveness;

    public boolean gameOver = false;

    private String winner;

    public Game(User user1, User user2, int mode){
        P1 = user1.getUsername();
        P2 = user2.getUsername();
        P1_level = Integer.parseInt(user1.getLevel());
        P2_level = Integer.parseInt(user2.getLevel());
        P1_champion = new Champion(P1_level);
        P2_champion = new Champion(P2_level);
        P1_currentChampHealth = P1_champion.getHealth();
        P2_currentChampHealth = P2_champion.getHealth();
        turn = P1;
        tooltip = turn + " was given the first attack!";
        attackEffectiveness = "";

        this.mode = mode;
    }

    public void nextTurn() {
        if (turn.equals(P1)){
            attack(P1);
            turn = P2;
        }
        else if (turn.equals(P2)) {
            attack(P2);
            turn = P1;
        }
        else System.out.println("invalid turn: " + turn);
    }

    public void attack(String attacker){
        Champion attackingChampion;
        int defendingChampionCurrentHealth;

        if (attacker.equals(P1)) {
            attackingChampion = P1_champion;
            defendingChampionCurrentHealth = P2_currentChampHealth;
        } else {
            attackingChampion = P2_champion;
            defendingChampionCurrentHealth = P1_currentChampHealth;
        }

        Random rand = new Random();
        double randomMultiplier = 0.5 + rand.nextDouble();   //rand between 0.5 and 1.5
        int damage = (int) (randomMultiplier * attackingChampion.getDamage());
        defendingChampionCurrentHealth -= damage;

        tooltip = attacker + " attacks and deals " + damage + " damage!";

        if (randomMultiplier < 0.8) attackEffectiveness = "Weak attack!";
        if (randomMultiplier > 0.8 && randomMultiplier < 1.2) attackEffectiveness = "Normal attack!";
        if (randomMultiplier > 1.2) attackEffectiveness = "Critical hit!";

        if (defendingChampionCurrentHealth <= 0) {
            System.out.println(attacker + " won!");
            winner = attacker;
            gameOver = true;
            if (attacker.equals(P1)) {
                P2_currentChampHealth = 0;
            } else {
                P1_currentChampHealth = 0;
            }
        } else {
            if (attacker.equals(P1)) {
                P2_currentChampHealth = defendingChampionCurrentHealth;
            } else {
                P1_currentChampHealth = defendingChampionCurrentHealth;
            }
        }
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }
}
