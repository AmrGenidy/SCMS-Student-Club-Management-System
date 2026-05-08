package scms.presentation;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public final class AlertHelper
{
    private AlertHelper()
    {
    }

    public static void showSuccess(String message)
    {
        showAlert(AlertType.INFORMATION, "Success", message);
    }

    public static void showValidationError(String message)
    {
        showAlert(AlertType.WARNING, "Validation Error", message);
    }

    public static void showException(String message)
    {
        showAlert(AlertType.ERROR, "Error", message);
    }

    private static void showAlert(AlertType alertType, String title, String message)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
