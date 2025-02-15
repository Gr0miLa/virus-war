package org.example.viruswarsoap;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface GameServer {
    @WebMethod
    String connectPlayer(Player player);

    @WebMethod
    String markCell(int x, int y, String clientId);

    @WebMethod
    String updateGameState(String clientId);

    @WebMethod
    void disconnectPlayer(String clientId);
}
