package db;

import java.sql.*;

public class DBManager {

    private final String _url = "jdbc:mysql://localhost:3306/spy?autoReconnect=true";
    private final String _user = "root";
    private final String _password = "C54tG409";
    private Connection _connection;
    private Statement _statement;
    private ResultSet _resultSet;

    public DBUsers DBUsers;

    // TODO: 26.07.2017 исправить автодисконект от БД при долгом ожидании (maybe fixed)
    public DBManager() throws SQLException {
        initConnection();

        DBUsers = new DBUsers(this);
    }

    private void initConnection() throws SQLException {
        if (_connection == null || _connection.isClosed())
            _connection = DriverManager.getConnection(_url, _user, _password);
        if (_statement == null || _statement.isClosed())
            _statement = _connection.createStatement();
    }

    public void execute(String query) throws SQLException {
        initConnection();
        _statement.execute(query);
    }

    public ResultSet executeQuery(String query) throws SQLException {
        initConnection();
        return _statement.executeQuery(query);
    }

}