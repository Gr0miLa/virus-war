package org.example.viruswar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void notifyOpponentMove(int x, int y) throws RemoteException;
    void notifyConnect(boolean isTurn, boolean gameStart) throws RemoteException;
    void notifyMove(boolean isTurn) throws RemoteException;
    void notifyGameOver(boolean isWin) throws RemoteException;
}
