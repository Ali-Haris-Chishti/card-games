package com.ahccode.cards.network;

import com.ahccode.cards.network.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class ServerHandler implements Runnable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final GameServer server;
    private final int playerNumber;

    public ServerHandler(Socket socket, ObjectOutputStream out, GameServer server, int playerNumber) {
        this.socket = socket;
        this.out = out;
        this.server = server;
        this.playerNumber = playerNumber;
    }

    @Override
    public void run() {
        ObjectInputStream in = null;
        try {
            // Small delay to ensure client is ready
            Thread.sleep(100);

            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("ServerHandler started for client " + playerNumber + ": " + socket.getInetAddress());

            while (true) {
                try {
                    System.out.println("Waiting for message from player " + playerNumber + ": " + socket.getInetAddress());

                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) {
                        System.err.println("Received non-Message object from player " + playerNumber + ": " + obj.getClass());
                        continue;
                    }

                    Message message = (Message) obj;
                    System.out.println("Received message: " + message.getType() + " from player " + playerNumber);

                    switch (message.getType()) {
                        case GET_PLAYER_NO:
                            System.out.println("Processing GET_PLAYER_NO for player " + playerNumber);
                            server.getPlayerNumber(out, playerNumber);
                            break;

                        case HELLO:
                            System.out.println("Processing HELLO for player " + playerNumber);
                            server.sendHelloMessage(message, playerNumber);
                            break;

                        case GET_START_CARDS:
                            System.out.println("Processing GET_START_CARDS for player " + playerNumber);
                            server.sendPlayerCards(out, playerNumber);
                            System.out.println("Completed GET_START_CARDS for player " + playerNumber);
                            break;

                        case MOVE_CARD:
                            System.out.println("Processing MOVE_CARD from player " + playerNumber);
                            server.moveCard(message, playerNumber); // Pass player number to exclude sender
                            break;

                        default:
                            System.out.println("Unknown message type: " + message.getType() + " from player " + playerNumber);
                            break;
                    }

                } catch (ClassNotFoundException e) {
                    System.err.println("ClassNotFoundException from player " + playerNumber + ": " + e.getMessage());
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    if (e.getMessage().contains("Connection reset") || e.getMessage().contains("Socket closed")) {
                        System.out.println("Client " + playerNumber + " disconnected normally");
                    } else {
                        System.err.println("IO Error with player " + playerNumber + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                } catch (Exception e) {
                    System.err.println("Unexpected error with player " + playerNumber + ": " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        } catch (InterruptedException e) {
            System.err.println("ServerHandler interrupted for player " + playerNumber);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("Error creating ObjectInputStream for player " + playerNumber + ": " + e.getMessage());
        } finally {
            // Clean up when client disconnects
            cleanup();
        }
    }

    private void cleanup() {
        System.out.println("Cleaning up resources for player " + playerNumber);

        server.removeClient(playerNumber);

        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing output stream for player " + playerNumber + ": " + e.getMessage());
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket for player " + playerNumber + ": " + e.getMessage());
        }

        System.out.println("Cleanup completed for player " + playerNumber);
    }
}