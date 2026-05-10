package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import scms.application.EventManager;
import scms.application.SessionManager;
import scms.application.model.Event;
import scms.application.model.Member;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Read-only "My Profile" view. Satisfies STP T-SRS-SCMS-004 step 2.
 *
 * <p>Shows the logged-in member's name, ID, email and role, and lists the
 * events that haven't happened yet so the member can see what's coming up.
 * Filtering to today/future dates satisfies SRS-SCMS-004.3.</p>
 */
public class ProfileController implements Initializable
{
    @FXML private Label nameLabel;
    @FXML private Label idLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private ListView<String> upcomingEventsList;

    private final EventManager eventManager = new EventManager();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        SessionManager session = SessionContext.get();
        if (session == null || session.getCurrentUser() == null)
        {
            // Should not happen in normal flow — controller only loads after
            // a successful login.
            statusLabel.setText("Not logged in");
            return;
        }

        Member user = session.getCurrentUser();
        nameLabel.setText(user.getName());
        idLabel.setText(user.getStudentId());
        emailLabel.setText(user.getEmail());
        roleLabel.setText(user.getRole());
        statusLabel.setText("Active");

        loadUpcomingEvents();
    }

    private void loadUpcomingEvents()
    {
        try
        {
            // Only show events whose date is today-or-later — SRS-SCMS-004.3.
            List<Event> events = eventManager.getUpcomingEvents(new Date());
            List<String> rows = new ArrayList<>();
            for (Event event : events)
            {
                rows.add(dateFormat.format(event.getDate())
                    + "  •  " + event.getName()
                    + "  @ " + event.getLocation()
                    + "   (" + event.getCurrentAttendees() + "/" + event.getQuota() + ")");
            }
            upcomingEventsList.setItems(FXCollections.observableArrayList(rows));
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Error loading upcoming events: " + e.getMessage());
        }
    }
}
