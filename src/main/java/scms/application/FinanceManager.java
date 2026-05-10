package scms.application;

import scms.application.model.Transaction;
import scms.data.dao.TransactionDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for club finances.
 *
 * <p>Remaining budget is computed as total income minus total expense across
 * all persisted transactions. The "initial budget" requested by STP
 * T-SRS-SCMS-003 is bootstrapped via {@code schema.sql} as a normal income
 * row, which keeps the calculation simple and auditable.</p>
 */
public class FinanceManager
{
    public static final String TYPE_INCOME  = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

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
        if (transaction.getType() == null
            || (!TYPE_INCOME.equalsIgnoreCase(transaction.getType())
                && !TYPE_EXPENSE.equalsIgnoreCase(transaction.getType())))
        {
            throw new IllegalArgumentException("Transaction type must be INCOME or EXPENSE");
        }
        if (transaction.getAmount() <= 0)
        {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        if (transaction.getDescription() == null || transaction.getDescription().isBlank())
        {
            throw new IllegalArgumentException("Transaction description is required");
        }
        if (transaction.getDate() == null)
        {
            throw new IllegalArgumentException("Transaction date is required");
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
            if (TYPE_INCOME.equalsIgnoreCase(transaction.getType()))
            {
                totalIncome += transaction.getAmount();
            }
            else if (TYPE_EXPENSE.equalsIgnoreCase(transaction.getType()))
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
