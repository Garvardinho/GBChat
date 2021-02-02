package com.geekbrains.server;

public class SimpleAuthService implements AuthService {
    private final DBHelper dbHelper = DBHelper.getInstance();

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        return DBHelper.getInstance().findNickname(login, password);
    }

    @Override
    public boolean updateNickname(String oldNickname, String newNickname) {
        return DBHelper.getInstance().updateNickname(oldNickname, newNickname) != 0;
    }
}
