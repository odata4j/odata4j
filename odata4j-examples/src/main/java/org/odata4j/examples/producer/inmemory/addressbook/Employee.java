package org.odata4j.examples.producer.inmemory.addressbook;

import java.util.Date;

public class Employee {

  private String employeeId;
  private String employeeName;
  private short age;
  private Date entryDate;

  public Employee(String employeeId, String employeeName, short age, Date entryDate) {
    this.employeeId = employeeId;
    this.employeeName = employeeName;
    this.age = age;
    this.entryDate = entryDate;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setEmployeeName(String employeeName) {
    this.employeeName = employeeName;
  }

  public short getAge() {
    return age;
  }

  public void setAge(short age) {
    this.age = age;
  }

  public Date getEntryDate() {
    return entryDate;
  }

  public void setEntryDate(Date entryDate) {
    this.entryDate = entryDate;
  }
}
