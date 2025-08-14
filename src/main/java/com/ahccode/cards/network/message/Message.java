package com.ahccode.cards.network.message;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class Message implements Serializable {
    private MessageType type;  // e.g., "MOVE", "STATE", "CHAT"
    private Object data;  // Can be card moves, player info, etc.

    public Message(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() { return type; }
    public Object getData() { return data; }
}
