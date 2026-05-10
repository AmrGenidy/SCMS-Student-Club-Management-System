package scms.data.dao;

import scms.application.model.Transaction;
import scms.data.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO
{
    public boolean logTransaction(Transaction transaction) throws SQLException
    {
        final String sql = "INSERT INTO transactions (transaction_id, type, amount, description, date) VALUES (?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, transaction.getTransactionId());
            preparedStatement.setString(2, transaction.getType());
            preparedStatement.setDouble(3, transaction.getAmount());
            preparedStatement.setString(4, transaction.getDescription());
            preparedStatement.setDate(5, new java.sql.Date(transaction.getDate().getTime()));
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Transaction> fetchAllTransactions() throws SQLException
    {
        final String sql = "SELECT transaction_id, type, amount, description, date FROM transactions ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery())
        {
            while (resultSet.next())
            {
                Transaction transaction = new Transaction(
                    resultSet.getInt("transaction_id"),
                    resultSet.getString("type"),
                    resultSet.getDouble("amount"),
                    resultSet.getString("description"),
                    resultSet.getDate("date")
                );
                transactions.add(transaction);
            }
        }
        return transactions;
    }
}
