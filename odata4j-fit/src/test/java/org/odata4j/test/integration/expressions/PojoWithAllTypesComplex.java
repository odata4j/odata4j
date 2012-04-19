package org.odata4j.test.integration.expressions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.core.UnsignedByte;

/**
 *
 */
public class PojoWithAllTypesComplex extends PojoWithAllTypes {

  private final PojoWithAllTypes complexType;
  private List<String> stringList; // can't be final because we need a setter.
  private List<Complex1> complexes;
  
  public PojoWithAllTypesComplex(byte[] binary, boolean boolean_, UnsignedByte byte_, byte sbyte, LocalDateTime dateTime, BigDecimal decimal,
          double double_, Guid guid, short int16, int int32, long int64, float single, String string, LocalTime time, DateTime dateTimeOffset,
          List<String> stringList, PojoWithAllTypes complexType) {

    super(binary, boolean_, byte_, sbyte, dateTime, decimal, double_, guid,
            int16, int32, int64, single, string, time, dateTimeOffset);
    this.complexType = complexType;
    this.stringList = stringList;
  }

  public PojoWithAllTypes getComplexType() {
    return this.complexType;
  }

  public List<String> getStringList() {
    return stringList;
  }

  public void setStringList(List<String> value) {
    stringList = value;
  }
  
  public static class Complex2 {
    public Complex2(String a, String b) {
      z1 = a;
      z2 = b;
    }

    public String getZ1() {
      return z1;
    }

    public String getZ2() {
      return z2;
    }

    private String z1;
    private String z2;
  }
  
  public static class Complex1 {

    public Complex1(String a, String b, Complex2 c2, List<Complex2> cc, List<String> cs) {
      s1 = a;
      s2 = b;
      this.embeddedComplex2 = c2;
      this.embeddedCollectionComplex2 = cc;
      this.embeddedCollectionString = cs;
    }

    public String getS1() {
      return s1;
    }

    public String getS2() {
      return s2;
    }
    
    public Complex2 getEmbeddedComplex2() { return this.embeddedComplex2; }
    public void setEmbeddedComplex2(Complex2 value) { this.embeddedComplex2 = value; }
    
    public List<Complex2> getEmbeddedCollectionComplex2() { return this.embeddedCollectionComplex2; }
    public void setEmbeddedCollectionComplex2(List<Complex2> value) { this.embeddedCollectionComplex2 = value; }
    
    public List<String> getEmbeddedCollectionString() { return this.embeddedCollectionString; }
    public void setEmbeddedCollectionString(List<String> value) { this.embeddedCollectionString = value; }
    
    private String s1;
    private String s2;
    private Complex2 embeddedComplex2;
    private List<Complex2> embeddedCollectionComplex2;
    private List<String> embeddedCollectionString;
  }
  
  public List<Complex1> getComplexes() {
    return complexes;
  }

  public void setComplexes(List<Complex1> value) {
    complexes = value;
  }
  
  public PojoWithAllTypesComplex addComplex1(Complex1 c) {
    if (null == complexes) { complexes = new ArrayList<Complex1>(); }
    complexes.add(c);
    return this;
  }
}
