package model;

public class Librarian extends Account {
    public Librarian() {
        super();
    }

    public Librarian(String accountId, String username, String password) {
        super(accountId, username, password, "LIBRARIAN");
    }
}