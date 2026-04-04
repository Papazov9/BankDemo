package model;

public enum CompoundingMode {
    MONTHLY(12),
    YEARLY(1);

    private final int periodsPerYear;

    CompoundingMode(int periodsPerYear) {
        this.periodsPerYear = periodsPerYear;
    }

    public int getPeriodsPerYear() {
        return periodsPerYear;
    }
}
