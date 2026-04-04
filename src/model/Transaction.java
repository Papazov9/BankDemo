package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Transaction {

    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String fromAccount;
    private final String toAccount;
    private final BigDecimal resultingBalance;

    public Transaction(TransactionType type,
                       BigDecimal amount,
                       String fromAccount,
                       String toAccount,
                       BigDecimal resultingBalance) {
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.resultingBalance = resultingBalance;
    }

    public LocalDateTime getTimestamp()        { return timestamp; }
    public TransactionType getType()           { return type; }
    public BigDecimal getAmount()              { return amount; }
    public String getFromAccount()             { return fromAccount; }
    public String getToAccount()               { return toAccount; }
    public BigDecimal getResultingBalance()    { return resultingBalance; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ")
          .append(type).append(" ").append(amount);
        if (fromAccount != null) sb.append(" from=").append(fromAccount);
        if (toAccount   != null) sb.append(" to=").append(toAccount);
        sb.append(" -> balance=").append(resultingBalance);
        return sb.toString();
    }
}
