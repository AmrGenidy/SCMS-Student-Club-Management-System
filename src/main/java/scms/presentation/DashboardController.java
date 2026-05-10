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
import scms.application.AccessControl;
import scms.application.EventManager;
import scms.application.FinanceManager;
import scms.application.MemberManager;
import scms.application.SessionManager;
import scms.application.model.Member;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Top-level navigation controller.
 *
 * <p>After {@link #initSession(SessionManager)} runs, the sidebar buttons that
 * correspond to admin-only screens (Members CRUD, Finance) are hidden when
 * the current user is not an administrator. Hiding the buttons isn't a real
 * security boundary on its own — the underlying managers also re-check the
 * caller's role via {@link AccessControl} — but it matches the behaviour
 * required by STP T-SRS-SCMS-004.</p>
 */
public class DashboardController implements Initializable
{
    @FXML private Label currentUserLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label totalEventsLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private VBox  dashboardPanel;

    @FXML private Button btnDashboard;
    @FXML private Button btnMembers;
    @FXML private Button btnEvents;
    @FXML private Button btnFinance;
    @FXML private Button btnProfile;
    @FXML private javafx.scene.layout.StackPane contentArea;

    private SessionManager sessionManager;
    private final MemberManager  memberManager  = new MemberManager();
    private final EventManager   eventManager   = new EventManager();
    private final FinanceManager financeManager = new FinanceManager();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Stats are loaded after initSession() is called.
    }

    public void initSession(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
        SessionContext.set(sessionManager);

        Member user = sessionManager.getCurrentUser();
        currentUserLabel.setText(user.getName() + " (" + user.getRole() + ")");

        applyRoleBasedVisibility(user);

        if (AccessControl.canManageFinances(user))
        {
            // Admin lands on the global dashboard.
            loadStats();
        }
        else
        {
            // Members land on their profile.
            showProfile();
        }
    }

    /**
     * Hide admin-only sidebar buttons when the current user is a regular
     * member. Disabled and invisible so they cannot be clicked even via
     * keyboard focus.
     */
    private void applyRoleBasedVisibility(Member user)
    {
        boolean isAdmin = AccessControl.canManageMembers(user);

        setVisible(btnMembers, isAdmin);
        setVisible(btnFinance, isAdmin);
        // Dashboard summary is admin-only (it shows organisation-wide counts).
        setVisible(btnDashboard, isAdmin);

        // Profile button is always visible. Events button is always visible.
    }

    private static void setVisible(Button button, boolean visible)
    {
        if (button == null)
        {
            return;
        }
        button.setVisible(visible);
        button.setManaged(visible);
        button.setDisable(!visible);
    }

    private void loadStats()
    {
        showDashboardPanelInContent();

        try
        {
            int memberCount = memberManager.getAllMembers().size();
            totalMembersLabel.setText(String.valueOf(memberCount));
        }
        catch (SQLException e)
        {
            totalMembersLabel.setText("—");
        }

        try
        {
            int eventCount = eventManager.getUpcomingEvents(new java.util.Date(0)).size();
            totalEventsLabel.setText(String.valueOf(eventCount));
        }
        catch (SQLException e)
        {
            totalEventsLabel.setText("—");
        }

        try
        {
            int txCount = financeManager.getAllTransactions().size();
            totalTransactionsLabel.setText(String.valueOf(txCount));
        }
        catch (SQLException e)
        {
            totalTransactionsLabel.setText("—");
        }
    }

    private void showDashboardPanelInContent()
    {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardPanel);
    }

    @FXML
    private void showDashboard()
    {
        if (sessionManager == null || !AccessControl.canManageFinances(sessionManager.getCurrentUser()))
        {
            AlertHelper.showValidationError("Access denied.");
            return;
        }
        setActiveButton(btnDashboard);
        loadStats();
    }

    @FXML
    private void showMembers()
    {
        if (sessionManager == null || !AccessControl.canManageMembers(sessionManager.getCurrentUser()))
        {
            AlertHelper.showValidationError("Access denied. Administrator privileges required.");
            return;
        }
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
        if (sessionManager == null || !AccessControl.canManageFinances(sessionManager.getCurrentUser()))
        {
            AlertHelper.showValidationError("Access denied. Administrator privileges required.");
            return;
        }
        setActiveButton(btnFinance);
        loadView("/scms/presentation/FinanceView.fxml");
    }

    @FXML
    private void showProfile()
    {
        setActiveButton(btnProfile);
        loadView("/scms/presentation/ProfileView.fxml");
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
        SessionContext.clear();

        Parent root = FXMLLoader.load(getClass().getResource("/scms/presentation/LoginView.fxml"));
        Stage stage = (Stage) currentUserLabel.getScene().getWindow();
        stage.setScene(new Scene(root, 980, 640));
        stage.setTitle("Student Club Management System");
    }

    private void setActiveButton(Button active)
    {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #a0a0b0; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-border-width: 0;";
        String activeStyle   = "-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-border-width: 0;";

        for (Button btn : new Button[]{btnDashboard, btnMembers, btnEvents, btnFinance, btnProfile})
        {
            if (btn == null)
            {
                continue;
            }
            btn.setStyle(btn == active ? activeStyle : inactiveStyle);
        }
    }
}
