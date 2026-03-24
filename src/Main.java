import model.BankAccount;
import model.SavingsAccount;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== BankAccount Tests ===");

        BankAccount alice = new BankAccount("Alice");
        BankAccount bob = new BankAccount("Bob");

        System.out.println("Owner: " + alice.getOwner());
        System.out.println("Initial balance: " + alice.getBalance());

        alice.deposit(500);
        alice.deposit(-100);

        alice.withdraw(200);
        alice.withdraw(1000);
        alice.withdraw(-50);

        System.out.println("Alice balance after operations: " + alice.getBalance());

        bob.deposit(100);
        alice.transferTo(bob, 150);
        System.out.println("Alice after transfer: " + alice.getBalance());
        System.out.println("Bob after transfer: " + bob.getBalance());

        alice.transferTo(bob, 9999);

        System.out.println();
        System.out.println("=== SavingsAccount Tests ===");

        SavingsAccount savings = new SavingsAccount("Charlie", 0.05);
        System.out.println("Savings owner: " + savings.getOwner());
        System.out.println("Savings initial balance: " + savings.getBalance());

        savings.deposit(1000);

        double newBalance = savings.applyInterest();
        System.out.println("Balance after interest: " + newBalance);

        newBalance = savings.applyInterest();
        System.out.println("Balance after 2nd interest: " + newBalance);

        savings.withdraw(100);
        System.out.println("Savings after withdraw: " + savings.getBalance());

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.println("Alice: " + alice.getBalance());
        System.out.println("Bob: " + bob.getBalance());
        System.out.println("Charlie (savings): " + savings.getBalance());

        alice.printTransactionHistory();
        bob.printTransactionHistory();
        savings.printTransactionHistory();
    }
}