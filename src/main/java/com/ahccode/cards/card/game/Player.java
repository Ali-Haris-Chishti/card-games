package com.ahccode.cards.card.game;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.network.GameClient;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import com.ahccode.cards.network.message.MoveMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player {
    public final List<Card> cardsInHand = new ArrayList<>();
    protected int playerNumber;
    protected final String name;
    protected final GameClient associatedClient;

    public Player(int playerNumber, String name, GameClient associatedClient) {
        this.playerNumber = playerNumber;
        this.name = name;
        this.associatedClient = associatedClient;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public String getName() {
        return name;
    }

    public GameClient getAssociatedClient() {
        return associatedClient;
    }

    public void sendHelloToServer() {
        try {
            // Start the client connection (this will automatically get player number)
            associatedClient.start();

            // Wait for player number to be received
            waitForPlayerNumber();

            // Update our player number from the client
            this.playerNumber = associatedClient.getMyPlayerNumber();

            // Now send hello with correct player number
            associatedClient.send(new Message(MessageType.HELLO, toPlayerInfo()));

            System.out.println("Sent hello to server: " + this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForPlayerNumber() {
        // Wait for the client to receive the player number from server
        int attempts = 0;
        final int maxAttempts = 50; // 5 seconds maximum wait

        while (associatedClient.getMyPlayerNumber() == -1 && attempts < maxAttempts) {
            try {
                Thread.sleep(100); // Wait 100ms
                attempts++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for player number", e);
            }
        }

        if (associatedClient.getMyPlayerNumber() == -1) {
            throw new RuntimeException("Failed to receive player number from server after " + (maxAttempts * 100) + "ms");
        }

        System.out.println("Received player number: " + associatedClient.getMyPlayerNumber());
    }

    public PlayerInfo toPlayerInfo() {
        return new PlayerInfo(playerNumber, name);
    }

    @Override
    public String toString() {
        return String.format("Player[number=%d, name='%s']", playerNumber, name);
    }

    public void moveCard(MoveMessage moveMessage) {
        try {
            associatedClient.send(new Message(MessageType.MOVE_CARD, moveMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}