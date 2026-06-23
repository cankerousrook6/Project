package kg.attractor.java.lesson44;

import java.util.ArrayList;
import java.util.List;

public class LibraryDataModel {

    private List<Book> books = new ArrayList<>();
    private Employee employee;

    public LibraryDataModel() {

        employee = new Employee("Vladislav", "Ivanov");

        Book java = new Book("Java Basic", "John Smith", 2020);

        Book html = new Book("HTML and CSS", "Alex Brown", 2021);

        Book sql = new Book("SQL Start", "Mary White", 2019);

        employee.addCurrentBook(java);
        employee.addOldBook(sql);

        books.add(java);
        books.add(html);
        books.add(sql);
    }

    public List<Book> getBooks() {
        return books;
    }

    public Book getBook() {
        return books.get(0);
    }

    public Employee getEmployee() {
        return employee;
    }
}