package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import scms.application.MemberManager;
import scms.application.model.Member;
import scms.data.dao.MemberDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MembersController implements Initializable
{
    @FXML private TextField nameField;
    @FXML private TextField studentIdField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private TableView<Member> memberTable;
    @FXML private TableColumn<Member, String> idCol;
    @FXML private TableColumn<Member, String> nameCol;
    @FXML private TableColumn<Member, String> emailCol;
    @FXML private TableColumn<Member, String> roleCol;

    private final MemberManager memberManager = new MemberManager();
    private final MemberDAO memberDAO = new MemberDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "MEMBER"));
        roleComboBox.getSelectionModel().selectLast(); // Default to MEMBER

        idCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadMembers();
    }

    private void loadMembers()
    {
        try
        {
            memberTable.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Error loading members: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister()
    {
        String name = nameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || role == null)
        {
            AlertHelper.showValidationError("Please fill all fields.");
            return;
        }

        Member member = new Member(name, studentId, email, role);
        try
        {
            if (memberManager.registerMember(member))
            {
                AlertHelper.showSuccess("Member registered successfully!");
                clearFields();
                loadMembers();
            }
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Database error: " + e.getMessage());
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            AlertHelper.showValidationError(e.getMessage());
        }
    }

    private void clearFields()
    {
        nameField.clear();
        studentIdField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().selectLast();
    }
}
