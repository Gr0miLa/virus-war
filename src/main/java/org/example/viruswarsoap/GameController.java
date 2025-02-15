package org.example.viruswarsoap;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

public class GameController {
    @FXML
    private Pane gameField;
    @FXML
    private Button startButton;

    public static final int SIZE_FIELD = 10;
    public static final int SIZE_CELL = 45;
    private boolean isPolling = false;
    private GameServer gameServer;
    private GridPane gridPane;
    private Player player;
    private String clientId;
    private String gameState;

    public void connectToGameServer() {
        try {
            URL wsdlURL = new URL("http://localhost:8080/gameService?wsdl");
            QName qname = new QName("http://viruswarsoap.example.org/", "GameServerImplService");
            Service service = Service.create(wsdlURL, qname);

            gameServer = service.getPort(GameServer.class);

            player = new Player();
            String connectMessage = gameServer.connectPlayer(player);
            String[] arrMessage = connectMessage.split(" ");
            clientId = arrMessage[1];
            player.setTurn(Boolean.parseBoolean(arrMessage[2]));
            System.out.println("connectMessage = " + connectMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onStartButtonClicked() {
        connectToGameServer();
        gridPane = createField(gameField);
        startButton.setDisable(true);
        gameField.setDisable(false);
        isPolling = true;
        System.out.println("Поле сгенерировано");
        startPolling();
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
        if (player.isTurn()) {
            if (player.isValidMove(x, y)) {
                Pane cellPane = (Pane) gridPane.getChildren().get(x * SIZE_FIELD + y);
                player.markCell(x, y);

                if (player.getCell(x, y) == 1) {
                    drawCross(cellPane, Color.valueOf("#669bbc"));
                } else if (player.getCell(x, y) == 3) {
                    cellPane.setStyle(cellPane.getStyle() + " -fx-background-color: #669bbc;");
                }

                String moveMessage = gameServer.markCell(x, y, clientId);
                System.out.println("moveMessage = " + moveMessage);
                String[] arrMessage = moveMessage.split(" ");
                player.setTurn(Boolean.parseBoolean(arrMessage[1]));

                player.incrementMoveCount();
                if (player.getMoveCount() == 3) {
                    startButton.setText("Ход противника");
                    player.resetMoveCount();
                }
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

    private void startPolling() {
        new Thread(() -> {
            while (isPolling) {
                try {
                    String gameState = gameServer.updateGameState(clientId);
                    updateGameState(gameState);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void updateGameState(String stateMessage) {
        if (!stateMessage.equals(gameState)) {
            String[] arrMessage = stateMessage.split(" ");
            System.out.println("stateMessage = " + stateMessage);
            player.setTurn(Boolean.parseBoolean(arrMessage[1]));

            int lastX = Integer.parseInt(arrMessage[2]);
            int lastY = Integer.parseInt(arrMessage[3]);

            if (lastX != -1 && lastY != -1) {
                notifyOpponentMove(lastX, lastY);
            }

            if (!Boolean.parseBoolean(arrMessage[4])) {
                boolean isWin = Boolean.parseBoolean(arrMessage[5]);
                System.out.println("isWin = " + isWin);
                isPolling = false;
                notifyGameOver(isWin);
            }

            gameState = stateMessage;
        }
    }

    public void notifyOpponentMove(int x, int y) {
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

    public void notifyGameOver(boolean isWin) {
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

            gameServer.disconnectPlayer(clientId);
        });
    }

    public void disconnectFromServer() {
        if (gameServer != null) {
            gameServer.disconnectPlayer(clientId);
        }
    }
}
