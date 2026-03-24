package model;

public class SavingsAccount extends BankAccount{

    private double interestRate;

    public SavingsAccount(String owner, double interestRate) {
        super(owner);
        this.interestRate = interestRate;
    }

    public double applyInterest() {
        double currentBalance = this.getBalance();
        this.setBalance(currentBalance + (currentBalance * this.interestRate));
        transactionHistory.add("Interest applied");
        System.out.println("Interest applied. New balance: " + this.getBalance());
        return this.getBalance();
    }
}
