package model;

import exception.OverdraftLimitExceededException;

import java.math.BigDecimal;

public class CheckingAccount extends BankAccount {

    private final BigDecimal overdraftLimit;

    public CheckingAccount(Customer customer, BigDecimal overdraftLimit) {
        super(customer);
        if (overdraftLimit == null || overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Overdraft limit must be non-negative, got: " + overdraftLimit);
        }
        this.overdraftLimit = overdraftLimit.setScale(SCALE, ROUNDING);
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    @Override
    protected void enforceWithdrawalAllowed(BigDecimal amount) {
        BigDecimal resultingBalance = getBalance().subtract(amount);
        BigDecimal floor = overdraftLimit.negate();
        if (resultingBalance.compareTo(floor) < 0) {
            throw new OverdraftLimitExceededException(
                    "Overdraft limit exceeded. Balance: " + getBalance()
                    + ", overdraft limit: " + overdraftLimit
                    + ", requested: " + amount
                    + ", resulting: " + resultingBalance);
        }
    }
}
