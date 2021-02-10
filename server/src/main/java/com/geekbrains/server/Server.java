package com.geekbrains.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;
    private ExecutorService executorService;
    private Logger logger = Logger.getLogger(Server.class.getName());

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        executorService = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            logger.log(Level.INFO, "Сервер запущен на порту 8189");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
                logger.log(Level.INFO, "Подключился новый клиент");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "Сервер завершил свою работу");
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public void privateMsg(ClientHandler sender, String receiverNick, String msg) {
        if (sender.getNickname().equals(receiverNick)) {
            sender.sendMsg("заметка для себя: " + msg);
            return;
        }
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(receiverNick)) {
                o.sendMsg("от " + sender.getNickname() + ": " + msg);
                sender.sendMsg("для " + receiverNick + ": " + msg);
                return;
            }
        }
        sender.sendMsg("Клиент " + receiverNick + " не найден");
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    public boolean isNickBusy(String nickname) {
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * clients.size());
        sb.append("/clients ");
        // '/clients '
        for (ClientHandler o : clients) {
            sb.append(o.getNickname()).append(" ");
        }
        // '/clients nick1 nick2 nick3 '
        sb.setLength(sb.length() - 1);
        // '/clients nick1 nick2 nick3'
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
