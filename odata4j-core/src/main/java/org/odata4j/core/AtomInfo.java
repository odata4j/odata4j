package org.odata4j.core;

/**
 * Additional Atom information.
 */
public interface AtomInfo {

    /**
     * Gets the Atom title.
     * 
     * @return the Atom title
     */
    public abstract String getTitle();
    
    /**
     * Gets the Atom category term.
     * 
     * @return the Atom category term
     */
    public abstract String getCategoryTerm();
}
