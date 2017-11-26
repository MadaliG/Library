/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bookly;

import com.itextpdf.text.DocumentException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.JFileChooser;

/**
 *
 * @author Machi
 */
public class BooklyController implements Initializable {

    DBconnect conn = new DBconnect();
    Book book = new Book();
    private ObservableList<Book> masterData;

    @FXML
    private TableView<Book> tableView;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField filterField;

    //Table and columns
    @FXML
    private TableColumn<Book, String> firstNameColumn;

    @FXML
    private TableColumn<Book, String> lastNameColumn;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> categoryColumn;

    @FXML
    private TableColumn<Book, String> locationColumn;

    //Items
    //Column settings
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("lastName"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("title"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("location"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("category"));

        titleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//        titleColumn.setOnEditCommit(
//                (TableColumn.CellEditEvent<Book, String> t)
//                -> (t.getTableView().getItems().get(
//                        t.getTablePosition().getRow())).setTitle(t.getNewValue())
//                 
//        );
        titleColumn.setOnEditCommit(event -> {
            final String value = event.getNewValue() != null ? event.getNewValue()
                    : event.getOldValue();
            Book myBook = (Book) event.getTableView().getItems().get(event.getTablePosition().getRow());
            myBook.setTitle(value);
            try {
                conn.editBook(myBook);
            } catch (SQLException ex) {
                Logger.getLogger(BooklyController.class.getName()).log(Level.SEVERE, null, ex);
            }
            tableView.refresh();
        });

        try {
            // 1. Wrap the ObservableList in a FilteredList (initially display all data).
            masterData = FXCollections.observableArrayList(conn.getBooks());
        } catch (SQLException ex) {
            Logger.getLogger(BooklyController.class.getName()).log(Level.SEVERE, null, ex);
        }

        FilteredList<Book> filteredData = new FilteredList<>(masterData, p -> true);
        // 2. Set the filter Predicate whenever the filter changes.
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (book.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches first name.
                } else if (book.getLastName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                } else if (book.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches title.
                }
                return false; // Does not match.
            });
        });

        // 3. Wrap the FilteredList in a SortedList. 
        SortedList<Book> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        tableView.setItems(sortedData);
    }

//        try {
//            tableView.getItems().setAll(conn.getBooks());
//    }
//    catch (SQLException ex
//
//    
//        ) {
//            Logger.getLogger(BooklyController.class.getName()).log(Level.SEVERE, null, ex);
//    }
//}
    @FXML
    protected void exportTable(ActionEvent event) throws SQLException, DocumentException, FileNotFoundException {
        JFileChooser dialog = new JFileChooser();
        int dialogResult = dialog.showSaveDialog(null);
        if (dialogResult == JFileChooser.APPROVE_OPTION) {
            String filePath = dialog.getSelectedFile().getPath();
            try {
                conn.exportAsPDF(filePath);
            } catch (IOException ex) {
                Logger.getLogger(BooklyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    protected void deleteBook(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmare");
        alert.setHeaderText("Sigur doresti stergerea cartii?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            TablePosition pos = tableView.getSelectionModel().getSelectedCells().get(0);
            int row = pos.getRow();
            try {
                conn.deleteBook(tableView.getItems().get(row).getBookId());
                tableView.getItems().removeAll(
                        tableView.getSelectionModel().getSelectedItems()
                );

            } catch (Exception ex) {
                Logger.getLogger(BooklyController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    @FXML
    protected void addBook(ActionEvent event) {
        ObservableList<Book> data = tableView.getItems();
        try {
            conn.addBook(new Book(firstNameField.getText(),
                    lastNameField.getText(),
                    titleField.getText(),
                    categoryField.getText(),
                    locationField.getText()));
            tableView.getItems().setAll(conn.getBooks());

        } catch (SQLException ex) {
            Logger.getLogger(BooklyController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        firstNameField.setText("");
        lastNameField.setText("");
        titleField.setText("");
        categoryField.setText("");
        locationField.setText("");
    }

//    @FXML
//    protected void editBook(ActionEvent event) throws SQLException {
//        TablePosition pos = tableView.getSelectionModel().getSelectedCells().get(0);
//        int row = pos.getRow();
//        System.out.println(tableView.getItems().get(row).getTitle());
//        conn.editBook(tableView.getItems().get(row));
//    }

}
