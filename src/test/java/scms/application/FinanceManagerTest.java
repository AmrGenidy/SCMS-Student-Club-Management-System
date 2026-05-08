package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.model.Transaction;
import scms.data.dao.TransactionDAO;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinanceManagerTest
{
    @Test
    void testFinance_BudgetCalculation() throws SQLException
    {
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        FinanceManager financeManager = new FinanceManager(transactionDAO);

        Transaction income = new Transaction(1, "INCOME", 1000.0, "Sponsorship", new Date());
        Transaction expense = new Transaction(2, "EXPENSE", 200.0, "Venue", new Date());

        when(transactionDAO.fetchAllTransactions()).thenReturn(Arrays.asList(income, expense));

        double remainingBudget = financeManager.calculateRemainingBudget();

        assertEquals(800.0, remainingBudget, 0.0001);
    }

    @Test
    void testFinance_EmptyBudget() throws SQLException
    {
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        FinanceManager financeManager = new FinanceManager(transactionDAO);

        when(transactionDAO.fetchAllTransactions()).thenReturn(Arrays.asList());

        double remainingBudget = financeManager.calculateRemainingBudget();

        assertEquals(0.0, remainingBudget, 0.0001);
    }

    @Test
    void testFinance_OnlyIncome() throws SQLException
    {
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        FinanceManager financeManager = new FinanceManager(transactionDAO);

        Transaction income = new Transaction(1, "INCOME", 500.0, "Gift", new Date());
        when(transactionDAO.fetchAllTransactions()).thenReturn(Arrays.asList(income));

        double remainingBudget = financeManager.calculateRemainingBudget();

        assertEquals(500.0, remainingBudget, 0.0001);
    }
}
