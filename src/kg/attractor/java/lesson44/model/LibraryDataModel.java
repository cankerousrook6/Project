package kg.attractor.java.lesson44.model;

import java.util.ArrayList;
import java.util.List;

public class LibraryDataModel {
    private List<Book> books = new ArrayList<>();
    private List<Employee> employees = new ArrayList<>();

    public List<Book> getBooks() {
        return books;
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}