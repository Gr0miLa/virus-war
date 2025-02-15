package org.example.viruswarsoap;

import javax.xml.ws.Endpoint;

public class MainServer {
    public static void main(String[] args) {
        try {
            Endpoint.publish("http://localhost:8080/gameService", new GameServerImpl());
            System.out.println("Сервер запущен");
        } catch (Exception e) {
            System.out.println("Ошибка = " + e.getMessage());
            e.printStackTrace();
        }
    }
}
