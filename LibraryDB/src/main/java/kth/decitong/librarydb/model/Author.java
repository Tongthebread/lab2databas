package kth.decitong.librarydb.model;

import java.util.ArrayList;
import java.util.Date;

public class Author implements Comparable<Author> {
    private final int authorID;
    private final String firstName;
    private final String lastName;
    private final Date birthDate;
    private final ArrayList<Book> books;

    public Author(int authorID, String firstName, String lastName, Date birthDate) {
        this.authorID = authorID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        books = new ArrayList<>();
    }


    public void addBook(Book book) {
        if (!books.contains(book)) {
            books.add(book);
        }
    }

    public void deleteBook(Book book) {
        for (Book b : books) {
            if (b.equals(book)) {
                books.remove(book);
            }
        }
    }

    public int getAuthorID() {
        return authorID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public ArrayList<Book> getBooks() {
        return new ArrayList<>(books);
    }

    @Override
    public int compareTo(Author o) {
        int lastNameCompare = lastName.compareTo(o.lastName);
        if(lastNameCompare == 0){
            return Integer.compare(authorID, o.authorID);
        }
        return lastNameCompare;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Author) {
            return this.compareTo((Author) o) == 0;
        }
        return false;
    }
}
