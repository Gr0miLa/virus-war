package org.example.viruswar;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {
    private Map<ClientCallback, Player> players;
    boolean gameStart = false;

    protected GameServerImpl() throws RemoteException {
        super();
        players = new HashMap<>();
    }

    @Override
    public void connectPlayer(Player player, ClientCallback callback) throws RemoteException {
        if (players.isEmpty()) {
            player.setTurn(true);
        }
        players.put(callback, player);
        System.out.println("Игрок подключился: " + player);
        System.out.println("Количество игроков: " + players.size());
        if (players.size() == 2) {
            gameStart = true;
        }
        for (Map.Entry<ClientCallback, Player> entry : players.entrySet()) {
            entry.getKey().notifyConnect(entry.getValue().isTurn(), gameStart);
        }
    }

    @Override
    public void markCell(int x, int y, ClientCallback callback) throws RemoteException {
        Player player = players.get(callback);

        ClientCallback opponentCallback = null;
        Player opponent = null;
        for (Map.Entry<ClientCallback, Player> entry : players.entrySet()) {
            if (!entry.getValue().equals(player)) {
                opponent = entry.getValue();
                opponentCallback = entry.getKey();
                break;
            }
        }

        if (opponent != null && opponentCallback != null) {
            System.out.println("Игрок " + player + " сделал ход (" + x + ", " + y + ")");
            player.markCell(x, y);
            if (player.getMoveCount() < 2) {
                player.incrementMoveCount();
            } else {
                player.resetMoveCount();
                player.setTurn(false);
                opponent.setTurn(true);
                callback.notifyMove(true);
                opponentCallback.notifyMove(false);
            }
            int mirroredX = Player.SIZE_FIELD - 1 - x;
            int mirroredY = Player.SIZE_FIELD - 1 - y;
            opponent.markOpponentCell(mirroredX, mirroredY);
            opponentCallback.notifyOpponentMove(mirroredX, mirroredY);

            if (!hasAvailableMoves(opponent)) {
                opponentCallback.notifyGameOver(false);
                callback.notifyGameOver(true);
                gameStart = false;
            }
        }
    }

    @Override
    public void disconnectPlayer(ClientCallback callback) throws RemoteException {
        Player player = players.remove(callback);
        System.out.println("Игрок отключен: " + player);
    }

    private boolean hasAvailableMoves(Player player) {
        for (int x = 0; x < Player.SIZE_FIELD; x++) {
            for (int y = 0; y < Player.SIZE_FIELD; y++) {
                if (player.isValidMove(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            GameServerImpl server = new GameServerImpl();
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry();
            registry.bind("GameServer", server);
            System.out.println("Сервер запущен");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
