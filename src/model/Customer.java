package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Customer {

    private final String id;
    private final String name;
    private final List<BankAccount> accounts = new ArrayList<>();

    public Customer(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Customer id must not be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be null or blank.");
        }
        this.id = id;
        this.name = name;
    }

    public String getId()   { return id; }
    public String getName() { return name; }

    public List<BankAccount> getAccounts() {
        return List.copyOf(accounts);
    }

    void addAccount(BankAccount account) {
        Objects.requireNonNull(account);
        accounts.add(account);
    }

    void removeAccount(BankAccount account) {
        accounts.remove(account);
    }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + name + "', accounts=" + accounts.size() + "}";
    }
}
