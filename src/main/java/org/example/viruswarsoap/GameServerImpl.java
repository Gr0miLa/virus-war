package org.example.viruswarsoap;

import javax.jws.WebService;
import java.util.*;

@WebService(endpointInterface = "org.example.viruswarsoap.GameServer")
public class GameServerImpl implements GameServer {
    private Map<String, Player> players;
    boolean gameStart = false;

    public GameServerImpl() {
        players = new HashMap<>();
    }

    @Override
    public String connectPlayer(Player player) {
        gameStart = true;
        System.out.println("Игрок подключился: " + player);

        if (players.isEmpty()) {
            player.setTurn(true);
        }

        String clientId = UUID.randomUUID().toString();
        players.put(clientId, player);
        System.out.println("Количество игроков: " + players.size());
        return "CONNECTED " + clientId + " " + player.isTurn();
    }

    @Override
    public String markCell(int x, int y, String clientId) {
        Player player = players.get(clientId);
        Player opponent = null;
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            if (!entry.getValue().equals(player)) {
                opponent = entry.getValue();
                break;
            }
        }

        if (opponent != null) {
            System.out.println("Игрок " + player + " сделал ход (" + x + ", " + y + ")");
            player.markCell(x, y);
            if (player.getMoveCount() < 2) {
                player.incrementMoveCount();
            } else {
                player.resetMoveCount();
                player.setTurn(false);
                opponent.setTurn(true);
            }
            int mirroredX = Player.SIZE_FIELD - 1 - x;
            int mirroredY = Player.SIZE_FIELD - 1 - y;
            opponent.markOpponentCell(mirroredX, mirroredY);
            opponent.setLastMove(mirroredX, mirroredY);

            if (!hasAvailableMoves(opponent)) {
                System.out.println("Игра окончена");
                player.setWin(true);
                gameStart = false;
            }
            if (!hasAvailableMoves(player)) {
                System.out.println("Игра окончена");
                player.setWin(false);
                gameStart = false;
            }
        }
        return "MOVE " + player.isTurn();
    }

    @Override
    public String updateGameState(String clientId) {
        Player player = players.get(clientId);
        int lastX = -1;
        int lastY = -1;
        boolean isTurn = false;
        boolean isWin = false;
        if (player != null) {
            lastX = player.getLastMoveX();
            lastY = player.getLastMoveY();
            isTurn = player.isTurn();
            isWin = player.isWin();
        }
        return "STATE " + isTurn + " " + lastX + " " + lastY +
                " " + gameStart + " " + isWin;
    }

    @Override
    public void disconnectPlayer(String clientId) {
        if (players.containsKey(clientId)) {
            players.remove(clientId);
            System.out.println("Игрок отключен: " + clientId);
            if (gameStart) {
                gameStart = false;
            }
        } else {
            System.out.println("Игрок уже отключен или не подключен: " + clientId);
        }
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
}
