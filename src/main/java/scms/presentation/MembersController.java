package scms.presentation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import scms.application.AccessControl;
import scms.application.MemberManager;
import scms.application.PasswordUtil;
import scms.application.SessionManager;
import scms.application.model.Member;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MembersController implements Initializable
{
    @FXML private TextField     nameField;
    @FXML private TextField     studentIdField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private TableView<Member>           memberTable;
    @FXML private TableColumn<Member, String>  idCol;
    @FXML private TableColumn<Member, String>  nameCol;
    @FXML private TableColumn<Member, String>  emailCol;
    @FXML private TableColumn<Member, String>  roleCol;
    @FXML private TableColumn<Member, Void>    actionCol;

    private final MemberManager memberManager = new MemberManager();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Defence in depth: if a non-admin somehow lands here, disable the form.
        SessionManager session = SessionContext.get();
        if (session == null || !AccessControl.canManageMembers(session.getCurrentUser()))
        {
            AlertHelper.showValidationError("Access denied. Administrator privileges required.");
        }

        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "MEMBER"));
        roleComboBox.getSelectionModel().selectLast(); // default to MEMBER

        idCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        setupActionColumn();
        loadMembers();
    }

    private void setupActionColumn()
    {
        if (actionCol == null)
        {
            return;
        }
        Callback<TableColumn<Member, Void>, TableCell<Member, Void>> cellFactory = column -> new TableCell<>()
        {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle(
                    "-fx-background-color: #ff6b6b; -fx-text-fill: white; "
                        + "-fx-font-size: 11px; -fx-background-radius: 5; "
                        + "-fx-cursor: hand;");
                deleteBtn.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    handleDelete(member);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        };
        actionCol.setCellFactory(cellFactory);
    }

    private void loadMembers()
    {
        try
        {
            memberTable.setItems(FXCollections.observableArrayList(memberManager.getAllMembers()));
        }
        catch (SQLException e)
        {
            AlertHelper.showException("Error loading members: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister()
    {
        SessionManager session = SessionContext.get();
        if (session == null || !AccessControl.canManageMembers(session.getCurrentUser()))
        {
            AlertHelper.showValidationError("Access denied. Administrator privileges required.");
            return;
        }

        String name = nameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || role == null)
        {
            AlertHelper.showValidationError("Please fill all required fields.");
            return;
        }
        if (password == null || password.isEmpty())
        {
            AlertHelper.showValidationError("Please set an initial password for the new member.");
            return;
        }

        String passwordHash = PasswordUtil.hash(password);
        Member member = new Member(name, studentId, email, role, passwordHash);

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

    /**
     * Confirms with the admin, then deletes the selected member through the
     * application layer. Self-deletion is rejected by the manager.
     */
    private void handleDelete(Member member)
    {
        SessionManager session = SessionContext.get();
        if (session == null || !AccessControl.canManageMembers(session.getCurrentUser()))
        {
            AlertHelper.showValidationError("Access denied. Administrator privileges required.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Member");
        confirm.setHeaderText(null);
        confirm.setContentText(
            "Are you sure you want to permanently delete "
                + member.getName() + " (" + member.getStudentId() + ")?");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK)
            {
                return;
            }
            try
            {
                String currentUserId = session.getCurrentUser().getStudentId();
                if (memberManager.deleteMember(member.getStudentId(), currentUserId))
                {
                    AlertHelper.showSuccess("Member deleted.");
                    loadMembers();
                }
                else
                {
                    AlertHelper.showValidationError("Nothing was deleted.");
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
        });
    }

    private void clearFields()
    {
        nameField.clear();
        studentIdField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().selectLast();
    }
}
