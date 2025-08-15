package com.ahccode.cards.network;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.*;
import com.ahccode.cards.card.game.daketi.Daketi;
import com.ahccode.cards.card.game.thulla.Thulla;
import com.ahccode.cards.network.message.CardMessage;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import com.ahccode.cards.ui.controller.GameScreenStarter;
import com.ahccode.cards.ui.network.GameServerManagerUI;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import static com.ahccode.cards.card.game.context.GameContextCore.deck;

@Slf4j
public class GameServer {

    private static GameServer instance;

    @FunctionalInterface
    public interface LogAppender {
        void appendLog(String logLevel, String message);
    }

    // In GameServer class, replace the TextArea with this interface
    private LogAppender logAppender;

    public GameServer(int port, LogAppender logAppender) {
        this(port);
        this.logAppender = logAppender;
        players = new ObservableList<>(() -> {
            broadcast(new Message(MessageType.PLAYER_INFO_LIST, getConnectedPlayers()));
        });
    }

    public void logMessageOnLogArea(String logLevel, String message) {
        if (logAppender != null) {
            logAppender.appendLog(logLevel, message);
        }
    }

    private GameServer() {
    }


    public static GameServer getInstance() {
        if (instance == null) {
            instance = new GameServer();
        }
        return instance;
    }

    private int port;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;

    // Thread-safe collections and proper client management
    private final Map<Integer, ClientInfo> clients = new ConcurrentHashMap<>();
    private final Object gameLock = new Object(); // For game state synchronization

    public void sendCloseMessage() {
        log.info("Sending CLose Message");
        broadcast(new Message(MessageType.SERVER_SHUTDOWN, null));
    }

    // Client info to track player numbers and streams
    private static class ClientInfo {
        final ObjectOutputStream out;
        final Socket socket;
        final int playerNumber;

        ClientInfo(ObjectOutputStream out, Socket socket, int playerNumber) {
            this.out = out;
            this.socket = socket;
            this.playerNumber = playerNumber;
        }
    }

    private ObservableList<Player> players;

    private GameServer(int port) {
        this.port = port;
        players = new ObservableList<>(() -> {
            broadcast(new Message(MessageType.PLAYER_INFO_LIST, getConnectedPlayers()));
        });
    }

    public void start() throws IOException {
        if (isRunning) {
            log.error("Server is already running on port {}", port);
            logMessageOnLogArea("ERROR", String.format("Server is already running on port %d", port));
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            log.info("Server started on port {}", port);
            logMessageOnLogArea("INFO", String.format("Server started on port %d", port));

            while (isRunning && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();

                    if (!isRunning) {
                        socket.close();
                        break;
                    }

                    synchronized (gameLock) {
                        if (players.size() >= 4) {
                            log.info("Maximum Players already Reached");
                            logMessageOnLogArea("MAX-4", "Maximum Players Reached");
                            socket.close();
                            continue;
                        }

                        log.info("New client connected: {}", socket.getInetAddress());
                        logMessageOnLogArea("INFO", String.format("New Client Connected: %s", socket.getInetAddress()));
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.flush(); // Important: flush immediately after creation

                        int playerNumber = players.size();
                        clients.put(playerNumber, new ClientInfo(out, socket, playerNumber));

                        // Create a thread for reading messages from this client
                        new Thread(new ServerHandler(socket, out, this, playerNumber)).start();
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        log.error("Error accepting client connection: {}", e.getMessage());
                        logMessageOnLogArea("ERROR", String.format("Error accepting client connection: %s", e.getMessage()));
                    }
                    // Continue the loop unless server is shutting down
                }
            }
        } finally {
            isRunning = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    log.error("Error closing server socket: {}", e.getMessage());
                }
            }
        }
    }

    public void stop() {
        if (!isRunning) {
            log.warn("Server is not running");
            return;
        }

        log.info("Stopping server...");
        logMessageOnLogArea("INFO", "Stopping server...");
        isRunning = false;

        // Close all client connections
        synchronized (gameLock) {
            for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
                try {
                    ClientInfo clientInfo = entry.getValue();
                    ObjectOutputStream out = clientInfo.out;
                    Socket socket = clientInfo.socket;

                    synchronized (out) {
                        out.writeObject(new Message(MessageType.SERVER_SHUTDOWN, "Server is shutting down"));
                        out.flush();
                        out.close();
                    }

                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    log.error("Error closing client connection {}: {}", entry.getKey(), e.getMessage());
                }
            }
            clients.clear();
            players.clear();
        }

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                log.info("Server socket closed");
                logMessageOnLogArea("INFO", "Server socket closed");
            } catch (IOException e) {
                log.error("Error closing server socket: {}", e.getMessage());
            }
        }

        log.info("Server stopped");
        logMessageOnLogArea("INFO", "Server stopped");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    public int getConnectedClientsCount() {
        return clients.size();
    }

    public synchronized void broadcast(Message message) {
        log.info("Broadcasting message: {} to {} clients", message.getType(), clients.size());
        logMessageOnLogArea("BROADCAST", String.format("Broadcasting message: %s to %d clients", message.getType(), clients.size()));

        List<Integer> failedClients = new ArrayList<>();

        for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
            try {
                ObjectOutputStream out = entry.getValue().out;
                synchronized (out) { // Synchronize each stream individually
                    out.reset(); // Reset the stream to prevent stale references
                    out.writeObject(message);
                    out.flush();
                }
                log.debug("Successfully sent to client {}", entry.getKey());
                logMessageOnLogArea("BROADCAST", String.format("Successfully sent to client %d", entry.getKey()));
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Failed to send message to client {}: {}", entry.getKey(), e.getMessage());
                failedClients.add(entry.getKey());
            }
        }

        // Remove failed clients
        for (Integer clientId : failedClients) {
            removeClient(clientId);
        }
    }

    public synchronized void broadcastToOthers(Message message, int excludePlayerNumber) {
        log.info("Broadcasting message: {} to {} clients (excluding player {})",
                message.getType(), clients.size() - 1, excludePlayerNumber);
        logMessageOnLogArea("E-BCT", String.format("Broadcasting message: %s to %d clients (excluding player %s)", message.getType(), clients.size(), excludePlayerNumber));

        List<Integer> failedClients = new ArrayList<>();

        for (Map.Entry<Integer, ClientInfo> entry : clients.entrySet()) {
            // Skip the sender
            if (entry.getKey() == excludePlayerNumber) {
                log.debug("Skipping sender (player {})", excludePlayerNumber);
                logMessageOnLogArea("E-BCT", String.format("Skipping sender (player: %s)", entry.getKey()));
                continue;
            }

            try {
                ObjectOutputStream out = entry.getValue().out;
                synchronized (out) { // Synchronize each stream individually
                    out.reset(); // Reset the stream to prevent stale references
                    out.writeObject(message);
                    out.flush();
                }
                log.debug("Successfully sent to client {}", entry.getKey());
                logMessageOnLogArea("E-BCT", String.format("Successfully sent to client %s", entry.getKey()));
            } catch (IOException e) {
                log.error("Failed to send message to client {}: {}", entry.getKey(), e.getMessage());
                failedClients.add(entry.getKey());
            }
        }

        // Remove failed clients
        for (Integer clientId : failedClients) {
            removeClient(clientId);
        }
    }

    public List<PlayerInfo> getConnectedPlayers() {
        return players.stream()
                .map(Player::toPlayerInfo)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public static void main(String[] args) {
        // Launch the UI instead of directly starting server
        try {
            // Check if JavaFX is available
            Class.forName("javafx.application.Application");
            javafx.application.Application.launch(GameServerManagerUI.class, args);
        } catch (ClassNotFoundException e) {
            // Fallback to command line if JavaFX is not available
            log.info("JavaFX not available, starting server in command line mode");
            try {
                new GameServer(5000, (LogAppender) null).start();
            } catch (IOException ioException) {
                log.error("Failed to start server: {}", ioException.getMessage());
            }
        }
    }

    public void sendHelloMessage(Message message, int playerNumber) throws IOException {
        synchronized (gameLock) {
            PlayerInfo info = (PlayerInfo) message.getData();
            players.add(new Player(info.getPlayerNumber(), info.getName(), null));
            logMessageOnLogArea("HELLO", String.format("Player added [%s, %d]", info.getName(), info.getPlayerNumber()));

            if (players.size() >= 4) {
                initializeGame();
                broadcast(new Message(MessageType.START_GAME, null));
                gameDataCleared = false;
            }
        }
    }

    public void getPlayerNumber(ObjectOutputStream out, int playerNumber) throws IOException {
        synchronized (out) {
            out.reset();
            out.writeObject(new Message(MessageType.GET_PLAYER_NO, playerNumber));
            out.flush();
        }
    }

    public void sendPlayerCards(ObjectOutputStream out, int playerNumber) throws IOException {
        synchronized (gameLock) {
            try {
                List<CardMessage> cards = getCardsForPlayer(playerNumber);
                Message message = new Message(MessageType.GET_START_CARDS, cards);

                synchronized (out) {
                    out.reset();
                    out.writeObject(message);
                    out.flush();
                }

                log.info("Sent {} cards to player {}", cards.size(), playerNumber);
            } catch (Exception e) {
                log.error("Error sending player cards to player {}: {}", playerNumber, e.getMessage());
                logMessageOnLogArea("ERROR", String.format("Error sending player cards to player %d: %s", playerNumber, e.getMessage()));
                throw new IOException("Failed to send player cards", e);
            }
        }
    }

    public void removeClient(int playerNumber) {
        synchronized (gameLock) {
            ClientInfo clientInfo = clients.remove(playerNumber);
            if (clientInfo != null) {
                try {
                    if (clientInfo.socket != null && !clientInfo.socket.isClosed()) {
                        clientInfo.socket.close();
                    }
                } catch (IOException e) {
                    log.error("Error closing socket for client {}: {}", playerNumber, e.getMessage());
                }
            }

            // Remove player from players list
            players.removeIf(player -> player.getPlayerNumber() == playerNumber);

            log.info("Removed client {}. Remaining clients: {}", playerNumber, clients.size());
            logMessageOnLogArea("REMOVED", String.format("Removed client %s, Remaining Clients: %d", playerNumber, clients.size()));
        }
    }

    public void removeClient(ObjectOutputStream out) {
        synchronized (gameLock) {
            clients.entrySet().removeIf(entry -> {
                if (entry.getValue().out == out) {
                    try {
                        if (entry.getValue().socket != null && !entry.getValue().socket.isClosed()) {
                            entry.getValue().socket.close();
                        }
                    } catch (IOException e) {
                        log.error("Error closing socket: {}", e.getMessage());
                    }
                    // Remove corresponding player
                    players.removeIf(player -> player.getPlayerNumber() == entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }

    private GameScreenStarter gameScreenStarter;

    public void setGameScreenStarter(GameScreenStarter gameScreenStarter) {
        this.gameScreenStarter = gameScreenStarter;
    }

    Game currentGame;
    Card[] deck;

    public void initializeGame() {
        synchronized (gameLock) {
            deck = new Card[52];

            int n = 0;
            for (CardFamily family : CardFamily.values()) {
                for (CardNumber number : CardNumber.values()) {
                    deck[n] = new Card(family, number, false, false);
                    n++;
                }
            }
            currentGame = initializeDaketi();
            log.info("Game initialized for {} players", players.size());
            logMessageOnLogArea("START", "Game started");
        }
    }

    public Daketi initializeDaketi() {
        Daketi daketi = new Daketi(getConnectedPlayers());
        CardMessage[] cardMessages = Arrays.stream(deck)
                .map(Card::toCardMessage)
                .toArray(CardMessage[]::new);
        daketi.assignCardsToPlayersAndCenter(cardMessages);
        return daketi;
    }

    public Thulla initializeThulla() {
        return null;
    }

    int turn = 0;

    // Modified to send cards specific to the requesting player
    public List<CardMessage> getCardsForPlayer(int requestingPlayer) {
        synchronized (gameLock) {
            try {
                if (currentGame == null) {
                    log.error("Current game is null!");
                    return new ArrayList<>();
                }

                List<Card> allCards = new ArrayList<>();

                // Get cards from each player
                for (int i = 0; i < 4; i++) {
                    List<Card> playerCards = ((Daketi) currentGame).getCardsOfPlayer(i);
                    if (playerCards != null) {
                        allCards.addAll(playerCards);
                    }
                }

                // Get center cards
                List<Card> centerCards = ((Daketi) currentGame).getCardsInCenter();
                if (centerCards != null) {
                    allCards.addAll(centerCards);
                }

                // Get remaining cards
                List<Card> remainingCards = ((Daketi) currentGame).getRemainingCards();
                if (remainingCards != null) {
                    allCards.addAll(remainingCards);
                }

                // Convert to CardMessage safely
                List<CardMessage> cardMessages = new ArrayList<>();
                for (Card card : allCards) {
                    if (card != null) {
                        try {
                            CardMessage cardMessage = card.toCardMessage();
                            if (cardMessage != null) {
                                cardMessages.add(cardMessage);
                            }
                        } catch (Exception e) {
                            log.error("Error converting card to CardMessage: {}", e.getMessage());
                        }
                    }
                }

                log.debug("Generated {} card messages for player {}", cardMessages.size(), requestingPlayer);
                return cardMessages;

            } catch (Exception e) {
                log.error("Error in getCardsForPlayer: {}", e.getMessage());
                return new ArrayList<>();
            }
        }
    }

    public void moveCard(Message message, int senderPlayerNumber) {
        log.info("Move card called from player {}", senderPlayerNumber);
        synchronized (gameLock) {
            try {
                CardMessage cardMessage = (CardMessage) message.getData();
                logMessageOnLogArea("MOVE-CARD", String.format("%s of %s clicked by player %d", cardMessage.number, cardMessage.family, senderPlayerNumber));

                // Broadcast to all connected clients EXCEPT the sender
                log.debug("Broadcasting to others....");
                broadcastToOthers(message, senderPlayerNumber);
                log.debug("Broadcasted move card message to other clients (excluding sender)");

            } catch (Exception e) {
                log.error("Error processing move card: {}", e.getMessage());
            }
        }
    }

    boolean gameDataCleared = false;

    public void playerLeft() {
        Message gameFinishedMessage = new Message(MessageType.SERVER_SHUTDOWN, "A Player has left");
        broadcast(gameFinishedMessage);
    }

    public void gameClosed() {
        if (gameDataCleared) {
            log.info("Game already closed");
            return;
        }

        log.info("Game finished - starting cleanup process");
        logMessageOnLogArea("GAME-END", "Game finished - starting cleanup");

        synchronized (gameLock) {
            try {
                // First, notify all clients that the game is finished

                // Give clients time to process the message
                Thread.sleep(500);

                // Now disconnect all clients gracefully
                List<Integer> clientsToRemove = new ArrayList<>(clients.keySet());
                for (Integer playerNumber : clientsToRemove) {
                    ClientInfo clientInfo = clients.get(playerNumber);
                    if (clientInfo != null) {
                        try {
                            // Send disconnect message
                            synchronized (clientInfo.out) {
                                clientInfo.out.writeObject(new Message(MessageType.SERVER_SHUTDOWN, "Game finished - returning to lobby"));
                                clientInfo.out.flush();
                                clientInfo.out.close();
                            }

                            // Close socket
                            if (clientInfo.socket != null && !clientInfo.socket.isClosed()) {
                                clientInfo.socket.close();
                            }

                            log.info("Disconnected client {}", playerNumber);
                            logMessageOnLogArea("DISCONNECT", String.format("Disconnected client %d", playerNumber));

                        } catch (IOException e) {
                            log.error("Error disconnecting client {}: {}", playerNumber, e.getMessage());
                        }
                    }
                }

                // Clear all game data
                clients.clear();
                players.clear();
                currentGame = null;
                deck = null;
                turn = 0;
                gameDataCleared = true;

                log.info("Game cleanup completed - server ready for new game");
                logMessageOnLogArea("RESET", "Server reset - ready for new players");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Game cleanup interrupted: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Error during game cleanup: {}", e.getMessage());
                logMessageOnLogArea("ERROR", String.format("Error during cleanup: %s", e.getMessage()));
            }
        }
    }
}