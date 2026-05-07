package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import scms.application.model.Event;
import scms.data.dao.EventDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class EventController {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colName;
    @FXML private TableColumn<Event, Date> colDate;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, Integer> colQuota;

    private final EventDAO eventDAO = new EventDAO();

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colQuota.setCellValueFactory(new PropertyValueFactory<>("quota"));

        loadUpcomingEvents();
    }

    private void loadUpcomingEvents() {
        try {
            // Using current date to filter upcoming events as per your DAO method
            List<Event> events = eventDAO.getUpcomingEvents(new Date());
            eventTable.setItems(FXCollections.observableArrayList(events));
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load events: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterForEvent() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            showAlert("Selection Error", "Please select an event from the table.");
            return;
        }

        try {
            if (selectedEvent.getCurrentAttendees() < selectedEvent.getQuota()) {
                if (eventDAO.incrementAttendeeCount(selectedEvent.getEventId())) {
                    showAlert("Success", "Registered for " + selectedEvent.getName());
                    loadUpcomingEvents(); // Refresh to show updated counts
                }
            } else {
                showAlert("Full", "This event has reached its quota.");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Registration failed: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}