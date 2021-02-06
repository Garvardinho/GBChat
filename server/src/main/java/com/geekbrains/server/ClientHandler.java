package com.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private AuthService authService;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            server.getExecutorService().submit(() -> {
                try {
                    authorize(server);
                    msgParsing(server);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    ClientHandler.this.disconnect();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void msgParsing(Server server) throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith("/")) {
                if (msg.equals("/end")) {
                    sendMsg("/end");
                    break;
                }

                if (msg.startsWith("/w ")) {
                    String[] tokens = msg.split("\\s", 3);
                    server.privateMsg(this, tokens[1], tokens[2]);
                }

                if (msg.startsWith("/upNick")) {
                    String[] tokens = msg.split("\\s", 2);

                    if (tokens.length == 2 && !tokens[1].trim().equals("")) {
                        String newNickname = tokens[1].trim();

                        if (!server.isNickBusy(newNickname)
                                && authService.updateNickname(getNickname(), newNickname)) {
                            String message = String.format
                                    ("Никнейм пользователя '%s' был изменен на '%s'", getNickname(), newNickname);
                            server.broadcastMsg(message);

                            nickname = newNickname;
                            server.broadcastClientsList();
                        }
                    }
                }
            } else {
                server.broadcastMsg(nickname + ": " + msg);
            }
        }
    }

    private void authorize(Server server) throws IOException {
        while (true) {
            String msg = in.readUTF();
            // /auth login1 pass1
            if (msg.startsWith("/auth ")) {
                this.authService = server.getAuthService();
                String[] tokens = msg.split("\\s");
                String nick = authService.getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                if (nick != null && !server.isNickBusy(nick)) {
                    sendMsg("/authok " + nick);
                    nickname = nick;
                    server.subscribe(this);
                    break;
                }
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
