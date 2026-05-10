package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.model.Transaction;
import scms.data.dao.TransactionDAO;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers STP T-SRS-SCMS-003 (budget calculation).
 */
class FinanceManagerTest
{
    @Test
    void testFinance_InitialBudgetMinusExpense() throws SQLException
    {
        // The STP scenario: starting budget 1000 TL, an expense of 200 TL is
        // recorded, remaining budget should display 800 TL.
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        FinanceManager financeManager = new FinanceManager(transactionDAO);

        Transaction initialBudget = new Transaction(1, "INCOME", 1000.0, "Initial budget", new Date());
        Transaction expense       = new Transaction(2, "EXPENSE", 200.0, "Pizza for meeting", new Date());

        when(transactionDAO.fetchAllTransactions()).thenReturn(Arrays.asList(initialBudget, expense));

        assertEquals(800.0, financeManager.calculateRemainingBudget(), 0.0001);
    }

    @Test
    void testFinance_EmptyBudget() throws SQLException
    {
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        FinanceManager financeManager = new FinanceManager(transactionDAO);

        when(transactionDAO.fetchAllTransactions()).thenReturn(Collections.emptyList());

        assertEquals(0.0, financeManager.calculateRemainingBudget(), 0.0001);
    }

    @Test
    void testFinance_OnlyIncome() throws SQLException
    {
        TransactionDAO transactionDAO = mock(TransactionDAO.class);
        FinanceManager financeManager = new FinanceManager(transactionDAO);

        Transaction income = new Transaction(1, "INCOME", 500.0, "Gift", new Date());
        when(transactionDAO.fetchAllTransactions()).thenReturn(Collections.singletonList(income));

        assertEquals(500.0, financeManager.calculateRemainingBudget(), 0.0001);
    }

    @Test
    void testFinance_RecordTransaction_NullTransaction_Throws()
    {
        FinanceManager financeManager = new FinanceManager(mock(TransactionDAO.class));

        assertThrows(IllegalArgumentException.class,
            () -> financeManager.recordTransaction(null));
    }

    @Test
    void testFinance_RecordTransaction_NegativeAmount_Throws()
    {
        FinanceManager financeManager = new FinanceManager(mock(TransactionDAO.class));
        Transaction bogus = new Transaction(1, "EXPENSE", -50.0, "Refund?", new Date());

        assertThrows(IllegalArgumentException.class,
            () -> financeManager.recordTransaction(bogus));
    }

    @Test
    void testFinance_RecordTransaction_BadType_Throws()
    {
        FinanceManager financeManager = new FinanceManager(mock(TransactionDAO.class));
        Transaction bogus = new Transaction(1, "BANANA", 50.0, "?", new Date());

        assertThrows(IllegalArgumentException.class,
            () -> financeManager.recordTransaction(bogus));
    }

    @Test
    void testFinance_RecordTransaction_EmptyDescription_Throws()
    {
        FinanceManager financeManager = new FinanceManager(mock(TransactionDAO.class));
        Transaction bogus = new Transaction(1, "INCOME", 50.0, "  ", new Date());

        assertThrows(IllegalArgumentException.class,
            () -> financeManager.recordTransaction(bogus));
    }
}
