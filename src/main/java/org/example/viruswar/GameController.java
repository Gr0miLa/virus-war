package org.example.viruswar;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.ResourceBundle;

public class GameController extends UnicastRemoteObject implements ClientCallback, Initializable {
    @FXML
    private Pane gameField;
    @FXML
    private Button startButton;

    public static final int SIZE_FIELD = 10;
    public static final int SIZE_CELL = 45;
    private GridPane gridPane;
    private Player player;
    private GameServer gameServer;

    public GameController() throws RemoteException {
        super();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        player = new Player(false);
        gameField.setDisable(true);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            gameServer = (GameServer) registry.lookup("GameServer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onStartButtonClicked() {
        try {
            gameServer.connectPlayer(player, this);
            createField(gameField);
            gridPane = createField(gameField);
            startButton.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GridPane createField(Pane pane) {
        GridPane grid = new GridPane();
        grid.setPrefSize(450, 450);

        int gridSize = SIZE_FIELD;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cellPane = new Pane();
                cellPane.setPrefSize(SIZE_CELL, SIZE_CELL);
                cellPane.setStyle("-fx-border-color: #ffe4c4; -fx-border-width: 1;");

                if (row == 0 && col == 0) {
                    drawCross(cellPane, Color.valueOf("#669bbc"));
                } else if (row == SIZE_FIELD - 1 && col == SIZE_FIELD - 1) {
                    drawRing(cellPane, Color.valueOf("#c1121f"));
                }
                cellPane.setStyle("-fx-background-color: #ffffff;" +
                                  " -fx-border-color: #ffe4c4;" +
                                  " -fx-border-width: 1;" +
                                  " -fx-border-style: solid;");

                final int y = col;
                final int x = row;
                cellPane.setOnMouseClicked(event -> onCellClicked(y, x));
                grid.add(cellPane, col, row);
            }
        }
        pane.getChildren().clear();
        pane.getChildren().add(grid);
        return grid;
    }


    private void onCellClicked(int y, int x) {
        if (player.isValidMove(x, y)) {
            try {
                Pane cellPane = (Pane) gridPane.getChildren().get(x * SIZE_FIELD + y);
                player.markCell(x, y);

                if (player.getCell(x, y) == 1) {
                    drawCross(cellPane, Color.valueOf("#669bbc"));
                } else if (player.getCell(x, y) == 3) {
                    cellPane.setStyle(cellPane.getStyle() + " -fx-background-color: #669bbc;");
                }
                gameServer.markCell(x, y, this);
                // System.out.println(Arrays.deepToString(player.getPlayerGrid()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.incrementMoveCount();
            if (player.getMoveCount() == 3) {
                startButton.setText("Ход противника");
                 player.resetMoveCount();
            }
        }
    }

    private void drawCross(Pane cellPane, Color color) {
        double offset = SIZE_CELL * 0.2;
        double size = SIZE_CELL - offset * 2;

        javafx.scene.shape.Line line1 = new javafx.scene.shape.Line(offset, offset, offset + size, offset + size);
        javafx.scene.shape.Line line2 = new javafx.scene.shape.Line(offset, offset + size, offset + size, offset);
        line1.setStroke(color);
        line2.setStroke(color);
        line1.setStrokeWidth(3);
        line2.setStrokeWidth(3);

        cellPane.getChildren().clear();
        cellPane.getChildren().addAll(line1, line2);
    }

    private void drawRing(Pane cellPane, Color color) {
        double offset = SIZE_CELL * 0.2;
        double radius = (SIZE_CELL - offset * 2) / 2;

        javafx.scene.shape.Circle ring = new javafx.scene.shape.Circle(SIZE_CELL / 2, SIZE_CELL / 2, radius);
        ring.setStroke(color);
        ring.setFill(Color.TRANSPARENT);
        ring.setStrokeWidth(3);

        cellPane.getChildren().clear();
        cellPane.getChildren().add(ring);
    }

    @Override
    public void notifyOpponentMove(int x, int y) throws RemoteException {
        Platform.runLater(() -> {
            Pane cellPane = (Pane) gridPane.getChildren().get(x * SIZE_FIELD + y);
            player.markOpponentCell(x, y);

            if (player.getCell(x, y) == 2) {
                drawRing(cellPane, Color.valueOf("#c1121f"));
            } else if (player.getCell(x, y) == 4) {
                cellPane.setStyle(cellPane.getStyle() + " -fx-background-color: #c1121f;");
            }

            if (player.isTurn()) {
                startButton.setText("Ваш ход");
            } else {
                startButton.setText("Ход противника");
            }
        });
    }

    @Override
    public void notifyConnect(boolean isTurn, boolean gameStart) throws RemoteException {
        Platform.runLater(() -> {
            if (gameStart) {
                System.out.println("Игра началась");
                if (isTurn) {
                    player.setTurn(true);
                    gameField.setDisable(false);
                    startButton.setText("Ваш ход");
                    System.out.println("Ваш ход");
                } else {
                    gameField.setDisable(true);
                    startButton.setText("Ход противника");
                    System.out.println("Ход противника");
                }
            }
        });
    }

    @Override
    public void notifyMove(boolean isTurn) throws RemoteException {
        Platform.runLater(() -> {
            System.out.println("Ваш ход - " + !isTurn);
            gameField.setDisable(isTurn);
            player.setTurn(!isTurn);
        });
    }

    @Override
    public void notifyGameOver(boolean isWin) throws RemoteException {
        Platform.runLater(() -> {
            startButton.setDisable(false);
            startButton.setText("Подключиться");
            gameField.setDisable(true);
            System.out.println("Game Over");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Игра окончена");
            alert.setHeaderText(null);
            String message = "Поражение";
            if (isWin) {
                message = "Победа";
            }
            alert.setContentText(message);
            alert.showAndWait();
            player.clearData();

            try {
                gameServer.disconnectPlayer(this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public void disconnectFromServer() throws RemoteException {
        if (gameServer != null) {
            gameServer.disconnectPlayer(this);
        }
    }
}
