package org.example.viruswar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameServer extends Remote {
    void connectPlayer(Player player, ClientCallback callback) throws RemoteException;
    void markCell(int x, int y, ClientCallback callback) throws RemoteException;
    void disconnectPlayer(ClientCallback callback) throws RemoteException;
}
