package scms.presentation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import scms.application.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Login screen controller.
 *
 * <p>The previous version of this controller accepted a Student ID plus a
 * user-chosen role and waved the request through. That allowed any member to
 * pick "ADMIN" and gain administrative privileges. This version requires a
 * Student ID + password, delegates verification to {@link SessionManager}, and
 * obtains the user's role exclusively from the database row.</p>
 */
public class LoginController implements Initializable
{
    @FXML private TextField     studentIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Pressing Enter in either field triggers login, matching common UX.
        studentIdField.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin()
    {
        String studentId = studentIdField.getText() == null ? "" : studentIdField.getText().trim();
        String password  = passwordField.getText()  == null ? "" : passwordField.getText();

        if (studentId.isEmpty())
        {
            errorLabel.setText("Please enter your Student ID.");
            return;
        }
        if (password.isEmpty())
        {
            errorLabel.setText("Please enter your password.");
            return;
        }

        try
        {
            boolean success = sessionManager.login(studentId, password);
            if (success)
            {
                navigateToDashboard();
            }
            else
            {
                // Intentionally generic — do not reveal which of the two was wrong.
                errorLabel.setText("Invalid Student ID or password.");
            }
        }
        catch (SQLException e)
        {
            errorLabel.setText("Database error: " + e.getMessage());
        }
        catch (IOException e)
        {
            errorLabel.setText("Failed to load dashboard: " + e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            errorLabel.setText(e.getMessage());
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
