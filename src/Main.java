import exception.*;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        Bank bank = new Bank("DemoBank");
        bank.setWithdrawalFee(new BigDecimal("0.50"));
        bank.setOverdraftFee(new BigDecimal("25.00"));

        Customer alice   = bank.registerCustomer("Alice");
        Customer bob     = bank.registerCustomer("Bob");
        Customer charlie = bank.registerCustomer("Charlie");

        BankAccount aliceChecking = bank.openCheckingAccount(alice, new BigDecimal("200"));
        BankAccount bobChecking   = bank.openCheckingAccount(bob);
        SavingsAccount charlieSavings = bank.openSavingsAccount(charlie, 0.05);

        aliceChecking.setDailyWithdrawalLimit(new BigDecimal("1000"));
        charlieSavings.setMaximumBalance(new BigDecimal("50000"));

        bank.listAccounts().forEach(a ->
                System.out.println("  " + a.getAccountNumber() + " | "
                        + a.getOwner() + " | " + a.getClass().getSimpleName()));


        bank.deposit(aliceChecking.getAccountNumber(), new BigDecimal("500"));
        System.out.println("After deposit 500: " + aliceChecking.getBalance());

        bank.withdraw(aliceChecking.getAccountNumber(), new BigDecimal("600"));
        System.out.println("After withdraw 600 (overdraft): " + aliceChecking.getBalance());

        try {
            bank.withdraw(aliceChecking.getAccountNumber(), new BigDecimal("200"));
        } catch (OverdraftLimitExceededException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        bank.deposit(bobChecking.getAccountNumber(), new BigDecimal("100"));
        bank.deposit(aliceChecking.getAccountNumber(), new BigDecimal("300"));
        bank.transfer(aliceChecking.getAccountNumber(), bobChecking.getAccountNumber(), new BigDecimal("50"));
        System.out.println("Alice after transfer: " + aliceChecking.getBalance());
        System.out.println("Bob after transfer:   " + bobChecking.getBalance());

        bank.deposit(charlieSavings.getAccountNumber(), new BigDecimal("1000"));

        BigDecimal afterInterest = charlieSavings.applyInterest(CompoundingMode.MONTHLY);
        System.out.println("After monthly interest: " + afterInterest);

        afterInterest = charlieSavings.applyInterest(CompoundingMode.YEARLY);
        System.out.println("After yearly interest:  " + afterInterest);

        try {
            bank.withdraw(charlieSavings.getAccountNumber(), new BigDecimal("99999"));
        } catch (InsufficientFundsException e) {
            System.out.println("Expected (no overdraft): " + e.getMessage());
        }

        bank.deposit(aliceChecking.getAccountNumber(), new BigDecimal("5000"));
        try {
            bank.withdraw(aliceChecking.getAccountNumber(), new BigDecimal("900"));
        } catch (DailyLimitExceededException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        try {
            bank.deposit(aliceChecking.getAccountNumber(), new BigDecimal("-50"));
        } catch (IllegalArgumentException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        try {
            bank.findAccount("ACC-999999");
        } catch (AccountNotFoundException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        try {
            aliceChecking.transferTo(aliceChecking, new BigDecimal("10"));
        } catch (IllegalArgumentException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        charlieSavings.setMaximumBalance(new BigDecimal("1100"));
        try {
            bank.deposit(charlieSavings.getAccountNumber(), new BigDecimal("9999"));
        } catch (MaximumBalanceExceededException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        Statement stmt = bank.generateStatement(
                aliceChecking.getAccountNumber(),
                LocalDate.now().minusDays(1),
                LocalDate.now());
        System.out.println(stmt);

        bank.closeAccount(bobChecking.getAccountNumber());
        System.out.println("Accounts remaining: " + bank.getAccountCount());

        try {
            bank.findAccount(bobChecking.getAccountNumber());
        } catch (AccountNotFoundException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        System.out.println(alice + " → accounts: " + alice.getAccounts().size());
        System.out.println(bob   + " → accounts: " + bob.getAccounts().size());

        bank.listAccounts().forEach(a -> {
            System.out.println(a.getOwner() + " (" + a.getAccountNumber() + "):");
            a.getTransactionHistory().forEach(t -> System.out.println("  " + t));
        });
    }
}