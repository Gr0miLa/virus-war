package org.example.viruswarsoap;


public class Player {
    public static final int SIZE_FIELD = 10;
    private final int[][] playerGrid = new int[SIZE_FIELD][SIZE_FIELD];
    private boolean isTurn;
    private boolean isWin;
    private int moveCount;

    private int lastMoveX = -1;
    private int lastMoveY = -1;

    public Player() {
        this.isTurn = false;
        this.isWin = false;
        this.moveCount = 1;
        playerGrid[0][0] = 1;
        playerGrid[SIZE_FIELD - 1][SIZE_FIELD - 1] = 2;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public int getLastMoveX() {
        return lastMoveX;
    }

    public int getLastMoveY() {
        return lastMoveY;
    }

    public void setLastMove(int x, int y) {
        this.lastMoveX = x;
        this.lastMoveY = y;
    }

    public void markOpponentCell(int x, int y) {
        if (playerGrid[x][y] == 0) {
            playerGrid[x][y] = 2;
        } else if (playerGrid[x][y] == 1) {
            playerGrid[x][y] = 4;
        }
    }

    public void markCell(int x, int y) {
        if (playerGrid[x][y] == 0) {
            playerGrid[x][y] = 1;
        } else if (playerGrid[x][y] == 2) {
            playerGrid[x][y] = 3;
        }
    }

    public int getCell(int x, int y) {
        return playerGrid[x][y];
    }

    public boolean checkCell(int x, int y) {
        return playerGrid[x][y] == 1 || playerGrid[x][y] == 3 || playerGrid[x][y] == 4;
    }

    public boolean isTurn() {
        return isTurn;
    }

    public void setTurn(boolean turn) {
        isTurn = turn;
    }

    public void clearData() {
        for (int i = 0; i < SIZE_FIELD; i++) {
            for (int j = 0; j < SIZE_FIELD; j++) {
                playerGrid[i][j] = 0;
            }
        }
        this.isTurn = false;
        this.moveCount = 1;
        playerGrid[0][0] = 1;
        playerGrid[SIZE_FIELD - 1][SIZE_FIELD - 1] = 2;
    }

    public int[][] getPlayerGrid() {
        return playerGrid;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        moveCount++;
    }

    public void resetMoveCount() {
        moveCount = 0;
    }

    public boolean isValidMove(int x, int y) {
        if (checkCell(x, y)) {
            return false;
        }

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newX = x + i;
                int newY = y + j;
                if (newX >= 0 && newX < SIZE_FIELD && newY >= 0 && newY < SIZE_FIELD) {
                    if (playerGrid[newX][newY] == 1 || (playerGrid[newX][newY] == 3 && isConnectedToOne(newX, newY))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isConnectedToOne(int x, int y) {
        boolean[][] visited = new boolean[SIZE_FIELD][SIZE_FIELD];
        return dfs(x, y, visited);
    }

    private boolean dfs(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= SIZE_FIELD || y < 0 || y >= SIZE_FIELD || visited[x][y]) {
            return false;
        }
        visited[x][y] = true;
        if (playerGrid[x][y] == 1) {
            return true;
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newX = x + i;
                int newY = y + j;
                if (newX >= 0 && newX < SIZE_FIELD && newY >= 0 && newY < SIZE_FIELD && playerGrid[x][y] == 3 && !visited[newX][newY]) {
                    if (dfs(newX, newY, visited)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
