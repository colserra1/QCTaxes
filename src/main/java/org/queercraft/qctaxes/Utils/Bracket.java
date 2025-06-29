package org.queercraft.qctaxes.Utils;

public class Bracket {
    private final double limit;
    private final double taxRate;

    public Bracket(double limit, double taxRate) {
        this.limit = limit;
        this.taxRate = taxRate;
    }
    public double getLimit() { return limit; }
    public double getTaxRate() {
        return taxRate;
    }
}
