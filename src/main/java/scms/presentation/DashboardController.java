package scms.presentation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import scms.application.SessionManager;
import scms.data.dao.EventDAO;
import scms.data.dao.MemberDAO;
import scms.data.dao.TransactionDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardController implements Initializable
{
    @FXML private Label currentUserLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label totalEventsLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private VBox dashboardPanel;

    @FXML private Button btnDashboard;
    @FXML private Button btnMembers;
    @FXML private Button btnEvents;
    @FXML private Button btnFinance;
    @FXML private javafx.scene.layout.StackPane contentArea;

    private SessionManager sessionManager;
    private final MemberDAO memberDAO = new MemberDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Stats are loaded after initSession() is called
    }

    public void initSession(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;

        String name = sessionManager.getCurrentUser().getName();
        String role = sessionManager.getCurrentUser().getRole();
        currentUserLabel.setText(name + " (" + role + ")");

        loadStats();
    }

    private void loadStats()
    {
        try
        {
            int memberCount = memberDAO.getAllMembers().size();
            totalMembersLabel.setText(String.valueOf(memberCount));
        }
        catch (SQLException e)
        {
            totalMembersLabel.setText("—");
        }

        try
        {
            int eventCount = eventDAO.getUpcomingEvents(new java.util.Date(0)).size();
            totalEventsLabel.setText(String.valueOf(eventCount));
        }
        catch (SQLException e)
        {
            totalEventsLabel.setText("—");
        }

        try
        {
            int txCount = transactionDAO.fetchAllTransactions().size();
            totalTransactionsLabel.setText(String.valueOf(txCount));
        }
        catch (SQLException e)
        {
            totalTransactionsLabel.setText("—");
        }
    }

    @FXML
    private void showDashboard()
    {
        setActiveButton(btnDashboard);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardPanel);
        loadStats(); // Refresh stats when returning to dashboard
    }

    @FXML
    private void showMembers()
    {
        setActiveButton(btnMembers);
        loadView("/scms/presentation/MembersView.fxml");
    }

    @FXML
    private void showEvents()
    {
        setActiveButton(btnEvents);
        loadView("/scms/presentation/EventsView.fxml");
    }

    @FXML
    private void showFinance()
    {
        setActiveButton(btnFinance);
        loadView("/scms/presentation/FinanceView.fxml");
    }

    private void loadView(String fxmlPath)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        }
        catch (IOException e)
        {
            AlertHelper.showException("Error loading view: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() throws IOException
    {
        if (sessionManager != null)
        {
            sessionManager.logout();
        }

        Parent root = FXMLLoader.load(getClass().getResource("/scms/presentation/LoginView.fxml"));
        Stage stage = (Stage) currentUserLabel.getScene().getWindow();
        stage.setScene(new Scene(root, 980, 640));
        stage.setTitle("Student Club Management System");
    }

    private void setActiveButton(Button active)
    {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #a0a0b0; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-border-width: 0;";
        String activeStyle   = "-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-border-width: 0;";

        for (Button btn : new Button[]{btnDashboard, btnMembers, btnEvents, btnFinance})
        {
            btn.setStyle(btn == active ? activeStyle : inactiveStyle);
        }
    }
}
