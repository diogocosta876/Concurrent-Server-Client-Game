package com.example.assignmentdistributed;

import java.io.Serializable;

public class SocketMessage implements Serializable {
    public static final int LOGIN_REQUEST = 0;
    public static final int REGISTER_REQUEST = 1;
    public static final int LOGOUT_REQUEST = 2;
    public static final int LOGIN_SUCCESSFUL = 3;
    public static final int LOGIN_REJECTED = 4;
    public static final int REGISTER_SUCCESSFUL = 5;
    public static final int REGISTER_REJECTED = 6;
    public static final int GAME_MODE = 7;
    public static final int EXIT_QUEUE = 8;
    public static final int UPDATE_QUEUE_LENGTH = 9;
    public static final int START_GAME = 10;
    public static final int UPDATE_GAME = 11;
    public static final int STILL_HERE = 12;
    public static final int GAME_OVER = 13;
    public static final int GAME_RECONNECTION = 14;
    public static final int GAME_ATTACK = 15;

    private final int code;
    private final Object[] arguments;

    public SocketMessage(int code, Object... arguments) {
        this.code = code;
        this.arguments = arguments;
    }

    public int getCode() {
        return code;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
