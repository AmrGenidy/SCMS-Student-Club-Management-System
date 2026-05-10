package scms.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection
{
    private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/scms";
    private static final String DEFAULT_USER     = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";

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

    public static synchronized void resetForTests()
    {
        if (instance != null)
        {
            try
            {
                instance.closeConnection();
            }
            catch (SQLException ignored)
            {
            }
            instance = null;
        }
    }

    public synchronized Connection getConnection() throws SQLException
    {
        if (connection == null || connection.isClosed())
        {
            String url      = System.getProperty("scms.db.url",      DEFAULT_URL);
            String user     = System.getProperty("scms.db.user",     DEFAULT_USER);
            String password = System.getProperty("scms.db.password", DEFAULT_PASSWORD);
            connection = DriverManager.getConnection(url, user, password);
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
