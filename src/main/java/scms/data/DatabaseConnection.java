package scms.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection
{
    private static final String DB_URL = "jdbc:mysql://localhost:3306/scms";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection()
    {
    }

    public static synchronized DatabaseConnection getInstance()
    {
        if (instance == null)
        {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException
    {
        if (connection == null || connection.isClosed())
        {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public synchronized void closeConnection() throws SQLException
    {
        if (connection != null && !connection.isClosed())
        {
            connection.close();
        }
    }
}
