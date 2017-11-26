/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bookly;

/**
 *
 * @author Machi
 */
import javafx.beans.property.SimpleStringProperty;

public final class Book {

    private final SimpleStringProperty firstName = new SimpleStringProperty("");
    private final SimpleStringProperty lastName = new SimpleStringProperty("");
    private final SimpleStringProperty title = new SimpleStringProperty("");
    private final SimpleStringProperty category = new SimpleStringProperty("");
    private final SimpleStringProperty location = new SimpleStringProperty("");
    private final SimpleStringProperty bookId = new SimpleStringProperty("");

    public Book() {
        this("", "", "", "", "");
    }

    public Book(String firstName, String lastName, String title, String bookId, String category, String location) {
        setFirstName(firstName);
        setLastName(lastName);
        setTitle(title);
        setBookId(bookId);
        setCategory(category);
        setLocation(location);
    }

    public Book(String firstName, String lastName, String title, String category, String location) {
        setFirstName(firstName);
        setLastName(lastName);
        setTitle(title);
        setCategory(category);
        setLocation(location);
    }

    public String getBookId() {
        return bookId.get();
    }

    public void setBookId(String id) {
        bookId.set(id);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String fName) {
        firstName.set(fName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String fName) {
        lastName.set(fName);
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String Title) {
        title.set(Title);
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String Category) {
        category.set(Category);
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String Location) {
        location.set(Location);
    }
}
