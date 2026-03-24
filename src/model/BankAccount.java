package model;

import java.util.ArrayList;
import java.util.List;

public class BankAccount {
    protected final String owner;
    private double balance;
    protected List<String> transactionHistory = new ArrayList<>();

    public BankAccount(String owner) {
        this.owner = owner;
        this.balance = 0;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactionHistory.add("deposited " + amount);
            System.out.println("Deposit successful. New balance: " + balance);
        }else {
            System.out.println("Deposit amount must be positive.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0) {
            if (amount <= balance) {
                balance -= amount;
                transactionHistory.add("withdrawn " + amount);
                System.out.println("Withdrawal successful. New balance: " + balance);
            } else {
                System.out.println("Insufficient funds.");
            }
        }else {
            System.out.println("Withdrawal amount must be positive.");
        }
    }

    public void transferTo(BankAccount destAccount, int amount) {
        this.withdraw(amount);
        destAccount.deposit(amount);
        transactionHistory.add("transfered " + amount);
        System.out.println("Transfer successful. Transferred " + amount + " to " + destAccount.getOwner());
    }

    public double getBalance() {
        return balance;
    }

    protected void setBalance(double balance) {
        if (balance >= 0) {
            this.balance = balance;
            transactionHistory.add("balance " + balance);
            System.out.println("Balance updated successfully. New balance: " + this.balance);
        } else {
            System.out.println("Balance cannot be negative.");
        }
    }

    public String getOwner() {
        return owner;
    }

    public void printTransactionHistory() {
        System.out.println("Transaction history for " + owner + ":");
        for (String entry : transactionHistory) {
            System.out.println("  - " + entry);
        }
    }
}
