package com.geekbrains.server;

import java.sql.*;

public class DBHelper implements AutoCloseable {
    private static DBHelper instance;
    private static Connection connection;

    private static PreparedStatement findNickname;
    private static PreparedStatement changeNickname;

    public static DBHelper getInstance() {
        if (instance == null) {
            loadDriverAndOpenConnection();
            createPreparedStatements();

            instance = new DBHelper();
        }

        return instance;
    }

    private static void loadDriverAndOpenConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:myDatabase.db");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка установки соединения с базой данных");
            e.printStackTrace();
        }
    }

    private static void createPreparedStatements() {
        try {
            findNickname = connection.prepareStatement
                    ("SELECT * FROM users WHERE login = ? AND PASSWORD = ?");
            changeNickname = connection.prepareStatement
                    ("UPDATE users SET nickname = ? WHERE nickname = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String findNickname(String login, String password) {
        ResultSet resultSet = null;

        try {
            findNickname.setString(1, login);
            findNickname.setString(2, password);

            resultSet = findNickname.executeQuery();
            if (resultSet.next())
                return resultSet.getString("nickname");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(resultSet);
        }

        return null;
    }

    public int updateNickname(String oldNickname, String newNickname) {
        try {
            changeNickname.setString(1, newNickname);
            changeNickname.setString(2, oldNickname);

            return changeNickname.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        try {
            findNickname.close();
            changeNickname.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
