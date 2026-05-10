package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import scms.application.AccessControl;
import scms.application.EventManager;
import scms.application.SessionManager;
import scms.application.exception.OverQuotaException;
import scms.application.model.Event;
import scms.application.model.Member;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class EventsController implements Initializable
{
    @FXML private TextField   idField;
    @FXML private TextField   nameField;
    @FXML private DatePicker  datePicker;
    @FXML private TextField   locationField;
    @FXML private TextField   quotaField;
    @FXML private VBox        createEventForm;

    @FXML private TableView<Event>           eventTable;
    @FXML private TableColumn<Event, String>  nameCol;
    @FXML private TableColumn<Event, Date>    dateCol;
    @FXML private TableColumn<Event, String>  locationCol;
    @FXML private TableColumn<Event, Integer> quotaCol;
    @FXML private TableColumn<Event, Integer> attendeesCol;
    @FXML private TableColumn<Event, Void>    actionCol;

    private final EventManager eventManager = new EventManager();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        quotaCol.setCellValueFactory(new PropertyValueFactory<>("quota"));
        attendeesCol.setCellValueFactory(new PropertyValueFactory<>("currentAttendees"));

        applyRoleVisibility();
        setupActionColumn();
        loadEvents();
    }

    /**
     * Hide the "Create Event" form when a non-administrator is viewing.
     * Members can still see and sign up to events.
     */
    private void applyRoleVisibility()
    {
        SessionManager session = SessionContext.get();
        Member user = session == null ? null : session.getCurrentUser();
        boolean canCreate = AccessControl.canManageEvents(user);

        if (createEventForm != null)
        {
            createEventForm.setVisible(canCreate);
            createEventForm.setManaged(canCreate);
        }
    }

    private void setupActionColumn()
    {
        Callback<TableColumn<Event, Void>, TableCell<Event, Void>> cellFactory = new Callback<>()
        {
            @Override
            public TableCell<Event, Void> call(final TableColumn<Event, Void> param)
            {
                return new TableCell<>()
                {
                    private final Button btn = new Button("Sign Up");

                    {
                        btn.setStyle("-fx-background-color: #4ecdc4; -fx-text-fill: white; -fx-font-size: 11px;");
                        btn.setOnAction(event -> {
                            Event data = getTableView().getItems().get(getIndex());
                            handleSignUp(data);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (empty)
                        {
                            setGraphic(null);
                        }
                        else
                        {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };

        actionCol.setCellFactory(cellFactory);
    }

    private void loadEvents()
    {
        try
        {
            SessionManager session = SessionContext.get();
            Member user = session == null ? null : session.getCurrentUser();

            // Admins see everything (including past), so they can audit. Members
            // see today and future only — SRS-SCMS-004.3.
            Date threshold = AccessControl.canManageEvents(user) ? new Date(0) : new Date();
            eventTable.setItems(FXCollections.observableArrayList(eventManager.getUpcomingEvents(threshold)));
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Error loading events: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateEvent()
    {
        SessionManager session = SessionContext.get();
        if (session == null || !AccessControl.canManageEvents(session.getCurrentUser()))
        {
            AlertHelper.showValidationError("Access denied. Administrator privileges required.");
            return;
        }

        try
        {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            LocalDate localDate = datePicker.getValue();
            String loc = locationField.getText().trim();
            int quota = Integer.parseInt(quotaField.getText().trim());

            if (name.isEmpty() || localDate == null || loc.isEmpty())
            {
                AlertHelper.showValidationError("Please fill all fields.");
                return;
            }

            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Event event = new Event(id, name, date, loc, quota, 0);

            if (eventManager.createEvent(event))
            {
                AlertHelper.showSuccess("Event created successfully!");
                clearFields();
                loadEvents();
            }
        }
        catch (NumberFormatException e)
        {
            AlertHelper.showValidationError("ID and Quota must be numeric.");
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

    private void handleSignUp(Event event)
    {
        SessionManager session = SessionContext.get();
        Member user = session == null ? null : session.getCurrentUser();
        if (!AccessControl.canSignUpForEvents(user))
        {
            AlertHelper.showValidationError("You must be logged in to sign up for events.");
            return;
        }

        try
        {
            if (eventManager.signMemberUp(event.getEventId(), user.getStudentId()))
            {
                AlertHelper.showSuccess("Signed up for " + event.getName() + "!");
                loadEvents();
            }
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Database error: " + e.getMessage());
        }
        catch (OverQuotaException e)
        {
            AlertHelper.showValidationError("Sorry, the event is full.");
        }
        catch (IllegalStateException e)
        {
            AlertHelper.showValidationError(e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            AlertHelper.showValidationError(e.getMessage());
        }
    }

    private void clearFields()
    {
        idField.clear();
        nameField.clear();
        datePicker.setValue(null);
        locationField.clear();
        quotaField.clear();
    }
}
