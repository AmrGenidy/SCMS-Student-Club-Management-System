package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import scms.application.FinanceManager;
import scms.application.model.Transaction;

import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

public class FinanceController implements Initializable
{
    @FXML private Label budgetLabel;
    @FXML private TextField idField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionArea;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Date> dateCol;
    @FXML private TableColumn<Transaction, String> typeCol;
    @FXML private TableColumn<Transaction, Double> amountCol;
    @FXML private TableColumn<Transaction, String> descCol;

    private final FinanceManager financeManager = new FinanceManager();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        typeComboBox.setItems(FXCollections.observableArrayList("INCOME", "EXPENSE"));
        typeComboBox.getSelectionModel().selectFirst();

        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        loadData();
    }

    private void loadData()
    {
        try
        {
            double budget = financeManager.calculateRemainingBudget();
            budgetLabel.setText(String.format("$%.2f", budget));

            transactionTable.setItems(FXCollections.observableArrayList(financeManager.getAllTransactions()));
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Error loading finance data: " + e.getMessage());
        }
    }

    @FXML
    private void handleRecordTransaction()
    {
        try
        {
            int id = Integer.parseInt(idField.getText().trim());
            String type = typeComboBox.getValue();
            double amount = Double.parseDouble(amountField.getText().trim());
            String desc = descriptionArea.getText().trim();

            if (desc.isEmpty())
            {
                AlertHelper.showValidationError("Please enter a description.");
                return;
            }

            Transaction tx = new Transaction(id, type, amount, desc, new Date());

            if (financeManager.recordTransaction(tx))
            {
                AlertHelper.showSuccess("Transaction recorded successfully!");
                clearFields();
                loadData();
            }
        }
        catch (NumberFormatException e)
        {
            AlertHelper.showValidationError("ID and Amount must be numeric.");
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Database error: " + e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            AlertHelper.showValidationError(e.getMessage());
        }
    }

    private void clearFields()
    {
        idField.clear();
        amountField.clear();
        descriptionArea.clear();
        typeComboBox.getSelectionModel().selectFirst();
    }
}
