package scms.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.sql.SQLException;

public class MemberController {

    @FXML private TextField nameField;
    @FXML private TextField studentIdField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;

    private final MemberDAO memberDAO = new MemberDAO();

    @FXML
    private void handleAddMember() {
        String name = nameField.getText();
        String id = studentIdField.getText();
        String email = emailField.getText();
        String role = roleField.getText();

        if (name.isEmpty() || id.isEmpty() || email.isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields.");
            return;
        }

        try {
            if (!memberDAO.isIdUnique(id)) {
                showAlert("Duplicate ID", "A member with this ID already exists.");
                return;
            }

            Member newMember = new Member(name, id, email, role);
            if (memberDAO.insertMember(newMember)) {
                showAlert("Success", "Member added successfully.");
                clearFields();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error accessing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.clear();
        studentIdField.clear();
        emailField.clear();
        roleField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
