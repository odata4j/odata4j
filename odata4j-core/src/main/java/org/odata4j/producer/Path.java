
package org.odata4j.producer;

import java.util.Arrays;

/**
 * a path in an object graph
 * @author Tony Rozga
 */
public class Path {
    
    public Path(String path) {
        this.spath = path;
        this.pathComponents = path.isEmpty() ? null : path.split("/");
    }
    
    public Path(Path path) {
        this.spath = path.spath;
        this.pathComponents = path.isEmpty() ? null : Arrays.<String>copyOf(path.pathComponents, path.pathComponents.length);
    }
    
    public int getNComponents() { return isEmpty() ? 0 : pathComponents.length; }
    
    public boolean isEmpty() { return null == pathComponents; }
    
    // components numbered from 0
    public String getNthComponent(int n) { 
        
        if (n < 0 || n > (getNComponents() - 1)) { throw new java.lang.IndexOutOfBoundsException(); }
        
        return pathComponents[n];    }
    
    public String getPath() { return spath; }
    
    public Path addComponent(String component) {
        return new Path(spath.isEmpty() ? component : (spath + "/" + component));
    }
    
    public Path removeLastComponent() {
        if (isEmpty()) { return this; }
        else if (this.getNComponents() == 1) {
            return new Path("");
        } else {
            StringBuilder sb = new StringBuilder(pathComponents[0]);
            for (int i = 1; i < pathComponents.length - 1; i++) {
                sb.append("/").append(pathComponents[i]);
            }
            return new Path(sb.toString());
        }
    }
    
    public Path removeFirstComponent() {
        if (isEmpty()) { return this; }
        else if (this.getNComponents() == 1) {
            return new Path("");
        } else {
            StringBuilder sb = new StringBuilder(pathComponents[1]);
            for (int i = 2; i < pathComponents.length; i++) {
                sb.append("/").append(pathComponents[i]);
            }
            return new Path(sb.toString());
        }
    }
    
    public String getFirstComponent() {
        return isEmpty() ? null : pathComponents[0];
    }
    
    public String getLastComponent() {
        return isEmpty() ? null: pathComponents[pathComponents.length - 1];
    }
    
    public boolean isWild() {
        // roar!
        return !isEmpty() && getLastComponent().equals("*");
    }
    
    @Override
    public boolean equals(Object rhso) {
        if (!rhso.getClass().equals(Path.class)) { return false; }
        Path rhs = (Path) rhso;
        
        return spath.equals(rhs.spath);
    }

    @Override
    public int hashCode() {
        return spath.hashCode();
    }
    
    @Override
    public String toString() {
        return spath;
    }
    
    private String[] pathComponents;
    private String spath;

  
}
