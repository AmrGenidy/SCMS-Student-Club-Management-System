package scms.application;

import scms.application.model.Transaction;
import scms.data.dao.TransactionDAO;

import java.sql.SQLException;
import java.util.List;

public class FinanceManager
{
    private final TransactionDAO transactionDAO;

    public FinanceManager()
    {
        this.transactionDAO = new TransactionDAO();
    }

    public FinanceManager(TransactionDAO transactionDAO)
    {
        this.transactionDAO = transactionDAO;
    }

    public boolean recordTransaction(Transaction transaction) throws SQLException
    {
        if (transaction == null)
        {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        return transactionDAO.logTransaction(transaction);
    }

    public double calculateRemainingBudget() throws SQLException
    {
        List<Transaction> transactions = transactionDAO.fetchAllTransactions();
        double totalIncome = 0.0;
        double totalExpense = 0.0;

        for (Transaction transaction : transactions)
        {
            if ("INCOME".equalsIgnoreCase(transaction.getType()))
            {
                totalIncome += transaction.getAmount();
            }
            else if ("EXPENSE".equalsIgnoreCase(transaction.getType()))
            {
                totalExpense += transaction.getAmount();
            }
        }

        return totalIncome - totalExpense;
    }

    public List<Transaction> getAllTransactions() throws SQLException
    {
        return transactionDAO.fetchAllTransactions();
    }
}