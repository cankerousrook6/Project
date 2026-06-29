package kg.attractor.java.lesson44.model;

public class Employee {
    private String id;
    private String firstName;
    private String lastName;
    private BookHistory history = new BookHistory();

    public BookHistory getHistory() {
        return history;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}