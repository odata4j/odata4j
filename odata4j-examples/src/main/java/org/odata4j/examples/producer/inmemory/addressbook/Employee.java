package org.odata4j.examples.producer.inmemory.addressbook;

public class Employee {

  private String employeeId;
  private String employeeName;
  private short age;

  public Employee(String employeeId, String employeeName, short age) {
    this.employeeId = employeeId;
    this.employeeName = employeeName;
    this.age = age;
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
}
