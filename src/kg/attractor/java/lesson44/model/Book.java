package kg.attractor.java.lesson44.model;

public class Book {
    private String id;
    private String title;
    private String description;
    private String author;
    private String image;
    private String ownerEmail;
    private boolean available;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() { return description; }

    public String getOwnerEmail() { return ownerEmail; }

    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}