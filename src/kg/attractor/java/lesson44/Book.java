package kg.attractor.java.lesson44;

public class Book {
    private String title;
    private String author;
    private int year;
    private Employee holder;

    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    public Employee getHolder() {
        return holder;
    }

    public void setHolder(Employee holder) {
        this.holder = holder;
    }

    public boolean isTaken() {
        return holder != null;
    }
}