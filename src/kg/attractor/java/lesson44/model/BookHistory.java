package kg.attractor.java.lesson44.model;

import java.util.ArrayList;
import java.util.List;

public class BookHistory {

    private List<Book> currentBooks = new ArrayList<>();
    private List<Book> oldBooks = new ArrayList<>();

    public List<Book> getCurrentBooks() {
        return currentBooks;
    }

    public List<Book> getOldBooks() {
        return oldBooks;
    }
}