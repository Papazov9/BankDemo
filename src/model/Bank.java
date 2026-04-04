package model;

import exception.AccountNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Bank {

    private final String name;
    private final Map<String, BankAccount> accounts = new ConcurrentHashMap<>();
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final AtomicLong accountSequence = new AtomicLong(1);
    private final AtomicLong customerSequence = new AtomicLong(1);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private BigDecimal withdrawalFee = BigDecimal.ZERO;
    private BigDecimal overdraftFee  = BigDecimal.ZERO;

    private final Map<String, LocalDate> overdraftFeeChargedOn = new ConcurrentHashMap<>();

    public Bank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bank name must not be null or blank.");
        }
        this.name = name;
    }

    public void setWithdrawalFee(BigDecimal fee) {
        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Withdrawal fee must be non-negative.");
        }
        this.withdrawalFee = fee;
    }

    public void setOverdraftFee(BigDecimal fee) {
        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overdraft fee must be non-negative.");
        }
        this.overdraftFee = fee;
    }

    public BigDecimal getWithdrawalFee() { return withdrawalFee; }
    public BigDecimal getOverdraftFee()  { return overdraftFee; }


    public Customer registerCustomer(String name) {
        String id = "CUST-" + String.format("%06d", customerSequence.getAndIncrement());
        Customer customer = new Customer(id, name);
        customers.put(id, customer);
        return customer;
    }

    public Customer findCustomer(String customerId) {
        Customer c = customers.get(customerId);
        if (c == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        return c;
    }

    public BankAccount openAccount(Customer customer, AccountType type,
                                   double interestRate, BigDecimal overdraftLimit) {
        Objects.requireNonNull(customer, "Customer must not be null.");
        Objects.requireNonNull(type, "Account type must not be null.");

        BankAccount account = switch (type) {
            case CHECKING -> new CheckingAccount(customer,
                    overdraftLimit != null ? overdraftLimit : BigDecimal.ZERO);
            case SAVINGS  -> new SavingsAccount(customer, interestRate);
        };

        lock.writeLock().lock();
        try {
            String number = generateAccountNumber();
            account.setAccountNumber(number);
            accounts.put(number, account);
            customer.addAccount(account);
        } finally {
            lock.writeLock().unlock();
        }
        return account;
    }

    // overloaded versions
    public BankAccount openCheckingAccount(Customer customer) {
        return openAccount(customer, AccountType.CHECKING, 0, BigDecimal.ZERO);
    }

    public BankAccount openCheckingAccount(Customer customer, BigDecimal overdraftLimit) {
        return openAccount(customer, AccountType.CHECKING, 0, overdraftLimit);
    }

    public SavingsAccount openSavingsAccount(Customer customer, double interestRate) {
        return (SavingsAccount) openAccount(customer, AccountType.SAVINGS, interestRate, null);
    }

    public void closeAccount(String accountNumber) {
        lock.writeLock().lock();
        try {
            BankAccount removed = accounts.remove(accountNumber);
            if (removed == null) {
                throw new AccountNotFoundException("Account not found: " + accountNumber);
            }
            removed.getCustomer().removeAccount(removed);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void findAccount(String accountNumber) {
        lock.readLock().lock();
        try {
            findAccountInternal(accountNumber);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<BankAccount> listAccounts() {
        lock.readLock().lock();
        try {
            return List.copyOf(accounts.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void deposit(String accountNumber, BigDecimal amount) {
        lock.readLock().lock();
        try {
            findAccountInternal(accountNumber).deposit(amount);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void withdraw(String accountNumber, BigDecimal amount) {
        lock.readLock().lock();
        try {
            BankAccount account = findAccountInternal(accountNumber);
            account.withdraw(amount);
            chargeWithdrawalFee(account);
            chargeOverdraftFeeIfNeeded(account);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void transfer(String fromNumber, String toNumber, BigDecimal amount) {
        lock.readLock().lock();
        try {
            BankAccount from = findAccountInternal(fromNumber);
            BankAccount to   = findAccountInternal(toNumber);
            from.transferTo(to, amount);
            chargeWithdrawalFee(from);
            chargeOverdraftFeeIfNeeded(from);
        } finally {
            lock.readLock().unlock();
        }
    }


    public Statement generateStatement(String accountNumber,
                                       LocalDate fromDate,
                                       LocalDate toDate) {
        lock.readLock().lock();
        try {
            BankAccount account = findAccountInternal(accountNumber);
            List<Transaction> all = account.getTransactionHistory();

            List<Transaction> filtered = all.stream()
                    .filter(t -> {
                        LocalDate d = t.getTimestamp().toLocalDate();
                        return !d.isBefore(fromDate) && !d.isAfter(toDate);
                    })
                    .collect(Collectors.toList());


            BigDecimal startingBalance = all.stream()
                    .filter(t -> t.getTimestamp().toLocalDate().isBefore(fromDate))
                    .reduce((a, b) -> b)
                    .map(Transaction::getResultingBalance)
                    .orElse(BigDecimal.ZERO);

            BigDecimal endingBalance = filtered.isEmpty()
                    ? startingBalance
                    : filtered.getLast().getResultingBalance();

            return new Statement(accountNumber, fromDate, toDate,
                    startingBalance, endingBalance, filtered);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getName()        { return name; }
    public int getAccountCount()   {
        lock.readLock().lock();
        try { return accounts.size(); }
        finally { lock.readLock().unlock(); }
    }

    private BankAccount findAccountInternal(String accountNumber) {
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return account;
    }

    private void chargeWithdrawalFee(BankAccount account) {
        if (withdrawalFee.compareTo(BigDecimal.ZERO) > 0) {
            account.applyFee(TransactionType.WITHDRAWAL_FEE, withdrawalFee);
        }
    }

    private void chargeOverdraftFeeIfNeeded(BankAccount account) {
        if (overdraftFee.compareTo(BigDecimal.ZERO) <= 0) return;
        if (!(account instanceof CheckingAccount)) return;
        if (account.getBalance().compareTo(BigDecimal.ZERO) >= 0) return;

        LocalDate today = LocalDate.now();
        LocalDate lastCharged = overdraftFeeChargedOn.get(account.getAccountNumber());
        if (today.equals(lastCharged)) return;

        account.applyFee(TransactionType.OVERDRAFT_FEE, overdraftFee);
        overdraftFeeChargedOn.put(account.getAccountNumber(), today);
    }

    private String generateAccountNumber() {
        return "ACC-" + String.format("%06d", accountSequence.getAndIncrement());
    }
}