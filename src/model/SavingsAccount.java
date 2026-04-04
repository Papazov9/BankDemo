package model;

import exception.InsufficientFundsException;
import exception.MaximumBalanceExceededException;

import java.math.BigDecimal;

public class SavingsAccount extends BankAccount {

    private final double interestRate;
    private BigDecimal maximumBalance;

    public SavingsAccount(Customer customer, double interestRate) {
        super(customer);
        if (interestRate < 0 || interestRate > 1) {
            throw new IllegalArgumentException(
                    "Interest rate must be between 0 and 1 (inclusive), got: " + interestRate);
        }
        this.interestRate = interestRate;
    }


    public synchronized void setMaximumBalance(BigDecimal max) {
        if (max != null && max.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Maximum balance must be positive or null (unlimited).");
        }
        this.maximumBalance = (max != null) ? max.setScale(SCALE, ROUNDING) : null;
    }

    public synchronized BigDecimal getMaximumBalance() {
        return maximumBalance;
    }


    @Override
    public synchronized void deposit(BigDecimal amount) {
        requirePositive(amount, "Deposit");
        enforceMaximumBalance(getBalance().add(amount));
        super.deposit(amount);
    }

    @Override
    protected void enforceWithdrawalAllowed(BigDecimal amount) {
        if (amount.compareTo(getBalance()) > 0) {
            throw new InsufficientFundsException(
                    "SavingsAccount does not allow overdraft. Balance: " + getBalance()
                    + ", requested: " + amount);
        }
    }

    public synchronized BigDecimal applyInterest(CompoundingMode mode) {
        BigDecimal rate = BigDecimal.valueOf(interestRate)
                .divide(BigDecimal.valueOf(mode.getPeriodsPerYear()), 10, ROUNDING);

        BigDecimal interest = getBalance()
                .multiply(rate)
                .setScale(SCALE, ROUNDING);

        if (interest.compareTo(BigDecimal.ZERO) <= 0) {
            return getBalance();
        }

        enforceMaximumBalance(getBalance().add(interest));
        adjustBalance(interest);
        recordTransaction(TransactionType.INTEREST, interest, null, getAccountNumber());
        return getBalance();
    }

    public synchronized BigDecimal applyInterest() {
        return applyInterest(CompoundingMode.YEARLY);
    }

    public double getInterestRate() {
        return interestRate;
    }

    private void enforceMaximumBalance(BigDecimal projected) {
        if (maximumBalance != null && projected.compareTo(maximumBalance) > 0) {
            throw new MaximumBalanceExceededException(
                    "Operation would exceed maximum balance. Max: " + maximumBalance
                    + ", projected: " + projected);
        }
    }
}
