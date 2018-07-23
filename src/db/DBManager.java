package db;

import java.sql.*;

public class DBManager {

    private final String _url = "jdbc:mysql://localhost:3306/spy";
    private final String _user = "root";
    private final String _password = "C54tG409";
    private Connection _connection;
    private Statement _statement;
    private ResultSet _resultSet;

    public DBUsers DBUsers;

    // TODO: 26.07.2017 исправить автодисконект от БД при долгом ожидании
    public DBManager() throws SQLException {
        _connection = DriverManager.getConnection(_url, _user, _password);
        _statement = _connection.createStatement();

        DBUsers = new DBUsers(this);
    }

    public void execute(String query) throws SQLException {
        _statement.execute(query);
    }

    public ResultSet executeQuery(String query) throws SQLException {
        return _statement.executeQuery(query);
    }

}