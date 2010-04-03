package org.odata4j.edm;

public enum EdmMultiplicity {

    ZERO_TO_ONE("0..1"), MANY("*"), ONE("1"), ;

    private final String symbolString;

    private EdmMultiplicity(String symbolString) {
        this.symbolString = symbolString;
    }

    public String getSymbolString() {
        return symbolString;
    }
}
