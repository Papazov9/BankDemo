package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Statement {

    private final String accountNumber;
    private final LocalDate from;
    private final LocalDate to;
    private final BigDecimal startingBalance;
    private final BigDecimal endingBalance;
    private final List<Transaction> transactions;

    public Statement(String accountNumber,
                     LocalDate from,
                     LocalDate to,
                     BigDecimal startingBalance,
                     BigDecimal endingBalance,
                     List<Transaction> transactions) {
        this.accountNumber = accountNumber;
        this.from = from;
        this.to = to;
        this.startingBalance = startingBalance;
        this.endingBalance = endingBalance;
        this.transactions = List.copyOf(transactions);
    }

    public String getAccountNumber()         { return accountNumber; }
    public LocalDate getFrom()               { return from; }
    public LocalDate getTo()                 { return to; }
    public BigDecimal getStartingBalance()   { return startingBalance; }
    public BigDecimal getEndingBalance()     { return endingBalance; }
    public List<Transaction> getTransactions(){ return transactions; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statement for ").append(accountNumber)
          .append(" (").append(from).append(" -> ").append(to).append(") ===\n");
        sb.append("Starting balance: ").append(startingBalance).append("\n");
        transactions.forEach(t -> sb.append("  ").append(t).append("\n"));
        sb.append("Ending balance:   ").append(endingBalance).append("\n");
        return sb.toString();
    }
}
