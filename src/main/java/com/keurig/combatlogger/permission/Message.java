package com.keurig.combatlogger.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Message {

    @Getter
    private String on;

    @Getter
    private String off;

    @Override
    public String toString() {
        return "Message{" +
                "on='" + on + '\'' +
                ", off='" + off + '\'' +
                '}';
    }
}
