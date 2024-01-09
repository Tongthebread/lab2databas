package kth.decitong.librarydb.model;

import java.util.ArrayList;
import java.util.Date;

public class Book implements Comparable<Book>{

    private final int bookId;
    private final String isbn;
    private final String title;
    private final Date published;
    private int rating;
    private final ArrayList<Author> authors;
    private final Genre genre;

    public Book(int bookId, String isbn, String title, Date published, int rating, Genre genre) {
        this.bookId = bookId;
        if(checkISBN(isbn)){
            this.isbn = isbn;
        }
        else throw new IllegalArgumentException("Invalid ISBN");

        this.title = title;
        this.published = published;
        if (checkRating(rating)){
            this.rating = rating;
        }
        else throw new IllegalArgumentException("Invalid rating");
        authors = new ArrayList<>();
        this.genre = genre;
    }

    public int getBookId() { return bookId; }

    public String getIsbn() { return isbn; }

    public String getTitle() { return title; }

    public Date getPublished() { return published; }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating){
        if(checkRating(rating)){
            this.rating = rating;
        }
    }

    public ArrayList<Author> getAuthors() {
        return new ArrayList<>(authors);
    }

    public void addAuthors(Author authors) {
        if (!this.authors.contains(authors)){
            this.authors.add(authors);
        }
    }

    private boolean checkISBN(String isbn) {
        String regex = "\\d{10}|\\d{13}";
        return isbn.matches(regex);
    }

    private boolean checkRating(int rating){
        return rating >= 1 && rating <= 5;
    }

    public Genre getGenre() {
        return genre;
    }

    @Override
    public int compareTo(Book o) {
        int titleCompare = title.compareTo(o.title);
        if (titleCompare == 0) {
            return Integer.compare(bookId, o.bookId);
        }

        return titleCompare;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Book){
            return this.compareTo((Book) o) == 0;
        }
        return false;
    }
}