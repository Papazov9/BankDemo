package model;

import exception.DailyLimitExceededException;
import exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BankAccount {

    static final int SCALE = 2;
    static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final Customer customer;
    private String accountNumber;          // set once by Bank (package-private)

    private BigDecimal balance;
    private final List<Transaction> transactionHistory = new ArrayList<>();

    private BigDecimal dailyWithdrawalLimit;          // null = unlimited
    private BigDecimal withdrawnToday = BigDecimal.ZERO;
    private LocalDate  withdrawalTrackingDate = LocalDate.now();


    public BankAccount(Customer customer) {
        Objects.requireNonNull(customer, "Customer must not be null.");
        this.customer = customer;
        this.balance = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
    }


    public synchronized void deposit(BigDecimal amount) {
        requirePositive(amount, "Deposit");
        balance = balance.add(amount).setScale(SCALE, ROUNDING);
        recordTransaction(TransactionType.DEPOSIT, amount, null, accountNumber);
    }

    public synchronized void withdraw(BigDecimal amount) {
        requirePositive(amount, "Withdrawal");
        enforceDailyLimit(amount);
        enforceWithdrawalAllowed(amount);

        balance = balance.subtract(amount).setScale(SCALE, ROUNDING);
        trackDailyWithdrawal(amount);
        recordTransaction(TransactionType.WITHDRAWAL, amount, accountNumber, null);
    }

    public void transferTo(BankAccount destAccount, BigDecimal amount) {
        Objects.requireNonNull(destAccount, "Destination account must not be null.");
        if (destAccount == this) {
            throw new IllegalArgumentException("Cannot transfer to the same account.");
        }
        requirePositive(amount, "Transfer");
        BankAccount first = System.identityHashCode(this) < System.identityHashCode(destAccount) ? this : destAccount;
        BankAccount second = (first == this) ? destAccount : this;

        synchronized (first) {
            synchronized (second) {
                enforceDailyLimit(amount);
                enforceWithdrawalAllowed(amount);

                balance = balance.subtract(amount).setScale(SCALE, ROUNDING);
                trackDailyWithdrawal(amount);
                recordTransaction(TransactionType.TRANSFER, amount, accountNumber, destAccount.getAccountNumber());

                destAccount.depositInternal(amount, accountNumber);
            }
        }
    }


    public synchronized void setDailyWithdrawalLimit(BigDecimal limit) {
        if (limit != null && limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Daily withdrawal limit must be non-negative or null (unlimited).");
        }
        this.dailyWithdrawalLimit = (limit != null) ? limit.setScale(SCALE, ROUNDING) : null;
    }

    public synchronized BigDecimal getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public synchronized BigDecimal getWithdrawnToday() {
        resetDailyTrackingIfNeeded();
        return withdrawnToday;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getOwner() {
        return customer.getName();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public synchronized List<Transaction> getTransactionHistory() {
        return List.copyOf(transactionHistory);
    }


    protected synchronized void adjustBalance(BigDecimal delta) {
        BigDecimal newBalance = balance.add(delta).setScale(SCALE, ROUNDING);
        requirePositive(newBalance, "Balance adjustment");
        balance = newBalance;
    }


    protected void enforceWithdrawalAllowed(BigDecimal amount) {
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available: " + balance + ", requested: " + amount);
        }
    }

    protected synchronized void recordTransaction(TransactionType type,
                                                   BigDecimal amount,
                                                   String fromAccount,
                                                   String toAccount) {
        transactionHistory.add(new Transaction(type, amount, fromAccount, toAccount, balance));
    }

    synchronized void depositInternal(BigDecimal amount, String fromAccountNumber) {
        requirePositive(amount, "Deposit");
        balance = balance.add(amount).setScale(SCALE, ROUNDING);
        recordTransaction(TransactionType.TRANSFER, amount, fromAccountNumber, accountNumber);
    }

    synchronized void applyFee(TransactionType feeType, BigDecimal feeAmount) {
        requirePositive(feeAmount, "Fee");
        balance = balance.subtract(feeAmount).setScale(SCALE, ROUNDING);
        recordTransaction(feeType, feeAmount, accountNumber, null);
    }


    private void enforceDailyLimit(BigDecimal amount) {
        resetDailyTrackingIfNeeded();
        if (dailyWithdrawalLimit != null) {
            BigDecimal projected = withdrawnToday.add(amount);
            if (projected.compareTo(dailyWithdrawalLimit) > 0) {
                throw new DailyLimitExceededException(
                        "Daily withdrawal limit exceeded. Limit: " + dailyWithdrawalLimit
                        + ", already withdrawn today: " + withdrawnToday
                        + ", requested: " + amount);
            }
        }
    }

    private void trackDailyWithdrawal(BigDecimal amount) {
        withdrawnToday = withdrawnToday.add(amount);
    }

    private void resetDailyTrackingIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(withdrawalTrackingDate)) {
            withdrawnToday = BigDecimal.ZERO;
            withdrawalTrackingDate = today;
        }
    }

    static void requirePositive(BigDecimal amount, String operationName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(operationName + " amount must be positive.");
        }
    }
}
