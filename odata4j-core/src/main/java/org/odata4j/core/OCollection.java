package org.odata4j.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.odata4j.edm.EdmBaseType;

/**
 * a homogeneous collection of objects of the given EdmBaseType.
 * T is the java type of instances in the collection.
 *
 * Example:
 *  new OCollection<OSimpleObject>(EdmType.STRING);
 *  new OCollection<OComplexObject>(metadata.findEdmComplexType("com.foo.bar.Address"));
 * 
 * TODO:
 * - should OCollection be immutable like other OThings?
 *
 */
public class OCollection<T extends OObject> implements OObject, Collection<T> {

  public OCollection(EdmBaseType type) {
    this.type = type;
  }

  @Override
  public EdmBaseType getType() {
    return this.type;
  }

  @Override
  public Iterator<T> iterator() {
    return items.iterator();
  }

  private EdmBaseType type = null;
  private List<T> items = new LinkedList<T>();

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return items.contains(o);
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return items.toArray(a);
    }

    @Override
    public boolean add(T e) {
        return items.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return items.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return items.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return items.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return items.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return items.retainAll(c);
    }

    @Override
    public void clear() {
        items.clear();
    }
}
