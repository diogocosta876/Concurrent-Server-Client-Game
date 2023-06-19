package com.example.assignmentdistributed.server;

import java.io.Serializable;

public class Champion implements Serializable {
    private final int level;
    private final int baseHealth = 50;
    private final int HealthLevelDivisor = 10;
    private final int baseDamage = 10;
    private final int DamageLevelDivisor = 50;

    public Champion(int level) {
        this.level = level;
    }

    public int getHealth() {
        return baseHealth + level/HealthLevelDivisor;
    }

    public int getDamage() {
        return baseDamage + level/DamageLevelDivisor;
    }
}