package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import scms.application.EventManager;
import scms.application.exception.OverQuotaException;
import scms.application.model.Event;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class EventsController implements Initializable
{
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField locationField;
    @FXML private TextField quotaField;

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> nameCol;
    @FXML private TableColumn<Event, Date> dateCol;
    @FXML private TableColumn<Event, String> locationCol;
    @FXML private TableColumn<Event, Integer> quotaCol;
    @FXML private TableColumn<Event, Integer> attendeesCol;
    @FXML private TableColumn<Event, Void> actionCol;

    private final EventManager eventManager = new EventManager();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        quotaCol.setCellValueFactory(new PropertyValueFactory<>("quota"));
        attendeesCol.setCellValueFactory(new PropertyValueFactory<>("currentAttendees"));

        setupActionColumn();
        loadEvents();
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
                        if (empty) {
                            setGraphic(null);
                        } else {
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
            // Load all events by passing a date in the past
            eventTable.setItems(FXCollections.observableArrayList(eventManager.getUpcomingEvents(new Date(0))));
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Error loading events: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateEvent()
    {
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
        try
        {
            if (eventManager.signMemberUp(event.getEventId()))
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
