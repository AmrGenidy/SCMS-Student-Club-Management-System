package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import scms.application.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable
{
    @FXML private TextField studentIdField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "MEMBER"));
        roleComboBox.getSelectionModel().selectFirst();

        // Allow pressing Enter in the student ID field to trigger login
        studentIdField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin()
    {
        String studentId = studentIdField.getText().trim();
        String role = roleComboBox.getValue();

        if (studentId.isEmpty())
        {
            errorLabel.setText("⚠ Please enter your Student ID.");
            return;
        }
        if (role == null)
        {
            errorLabel.setText("⚠ Please select a role.");
            return;
        }

        try
        {
            boolean success = sessionManager.login(studentId, role);
            if (success)
            {
                navigateToDashboard();
            }
            else
            {
                errorLabel.setText("⚠ Student ID not found. Please check and try again.");
            }
        }
        catch (SQLException e)
        {
            errorLabel.setText("⚠ Database error: " + e.getMessage());
        }
        catch (IOException e)
        {
            errorLabel.setText("⚠ Failed to load dashboard: " + e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            errorLabel.setText("⚠ " + e.getMessage());
        }
    }

    private void navigateToDashboard() throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scms/presentation/DashboardView.fxml"));
        Parent root = loader.load();

        DashboardController controller = loader.getController();
        controller.initSession(sessionManager);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root, 980, 640));
        stage.setTitle("SCMS — Dashboard");
    }
}
