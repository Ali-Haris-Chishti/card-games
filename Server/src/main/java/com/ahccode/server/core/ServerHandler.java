package com.ahccode.server.core;

import com.ahccode.common.network.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

import static com.ahccode.common.network.MessageType.*;

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
            log.info("ServerHandler started for client {}: {}", playerNumber, socket.getInetAddress());

            while (true) {
                try {
                    log.info("Waiting for message from player {}: {}", playerNumber, socket.getInetAddress());

                    Object obj = in.readObject();
                    if (!(obj instanceof Message)) {
                        log.error("Received non-Message object from player {}: {}", playerNumber, obj.getClass());
                        continue;
                    }

                    Message message = (Message) obj;
                    log.info("Received message: {} from player {}", message.getType(), playerNumber);

                    switch (message.getType()) {
                        case GET_PLAYER_NO:
                            log.info("Processing GET_PLAYER_NO for player {}", playerNumber);
                            server.getPlayerNumber(out, playerNumber);
                            break;

                        case HELLO:
                            log.info("Processing HELLO for player {}", playerNumber);
                            server.sendHelloMessage(message, playerNumber);
                            break;

                        case GET_START_CARDS:
                            log.info("Processing GET_START_CARDS for player {}", playerNumber);
                            server.sendPlayerCards(out, playerNumber);
                            log.info("Completed GET_START_CARDS for player {}", playerNumber);
                            break;

                        case MOVE_CARD:
                            log.info("Processing MOVE_CARD from player {}", playerNumber);
                            server.moveCard(message, playerNumber); // Pass player number to exclude sender
                            break;

                        case GAME_FINISHED:
                            log.info("Processing GAME_FINISHED for player {}", playerNumber);
                            server.broadcast(new Message(GAME_FINISHED, "Game Finished Normally"));
                            server.gameClosed();
                            break;

                        case PLAYER_LEFT:
                            log.info("Processing PLAYER_LEFT for player {}", playerNumber);
                            server.sendCloseMessage();
                            break;


                        default:
                            log.info("Unknown message type: {} from player {}", message.getType(), playerNumber);
                            break;
                    }

                } catch (ClassNotFoundException e) {
                    log.error("ClassNotFoundException from player {}: {}", playerNumber, e.getMessage());
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    if (e.getMessage().contains("Connection reset") || e.getMessage().contains("Socket closed")) {
                        log.info("Client {} disconnected normally", playerNumber);
                    } else {
                        log.error("IO Error with player {}: {}", playerNumber, e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                } catch (Exception e) {
                    log.error("Unexpected error with player {}: {}", playerNumber, e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.error("ServerHandler interrupted for player {}", playerNumber);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("Error creating ObjectInputStream for player {}: {}", playerNumber, e.getMessage());
        } finally {
            // Clean up when client disconnects
            cleanup();
        }
    }

    private void cleanup() {
        log.info("Cleaning up resources for player {}", playerNumber);

        server.removeClient(playerNumber);

        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            log.error("Error closing output stream for player {}: {}", playerNumber, e.getMessage());
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            log.error("Error closing socket for player {}: {}", playerNumber, e.getMessage());
        }

        log.info("Cleanup completed for player {}", playerNumber);
    }
}