package model;

public class Manager extends Account {
    public Manager() {
        super();
    }

    public Manager(String accountId, String username, String password) {
        super(accountId, username, password, "MANAGER");
    }
}