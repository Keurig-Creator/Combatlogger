package com.keurig.combatlogger.permission;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Permission {

    private long time;

    @Getter
    private String permission;

    private final Map<String, Message> messages = new HashMap<>();

    @Getter
    private int weight;

    public Permission(long time, String permission, int weight) {
        this.time = time;
        this.permission = permission;
        this.weight = weight;
    }

    public void setActionMessage(String on, String off) {
        messages.put("Actionbar", new Message(on, off));
    }

    public void setChatMessage(String on, String off) {
        messages.put("Message", new Message(on, off));
    }

    public Message getChatMessage() {
        return messages.get("Message");
    }

    public Message getActionbar() {
        return messages.get("Actionbar");
    }

    @Override
    public String toString() {
        return "Permission{" +
                "time=" + time +
                ", permission='" + permission + '\'' +
                ", messages=" + messages +
                '}';
    }
}
