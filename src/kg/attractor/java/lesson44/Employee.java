package kg.attractor.java.lesson44;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String firstName;
    private String lastName;

    private List<Book> currentBooks = new ArrayList<>();
    private List<Book> oldBooks = new ArrayList<>();

    public Employee(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void addCurrentBook(Book book) {
        currentBooks.add(book);
        book.setHolder(this);
    }

    public void addOldBook(Book book) {
        oldBooks.add(book);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public List<Book> getCurrentBooks() {
        return currentBooks;
    }

    public List<Book> getOldBooks() {
        return oldBooks;
    }
}