package org.odata4j.examples.producer.jpa.addressbook;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Employees {

  @Id
  private String EmployeeId;
  private String EmployeeName;
  private short Age;

  public String getEmployeeId() {
    return EmployeeId;
  }

  public void setEmployeeId(String EmployeeId) {
    this.EmployeeId = EmployeeId;
  }

  public String getEmployeeName() {
    return EmployeeName;
  }

  public void setEmployeeName(String EmployeeName) {
    this.EmployeeName = EmployeeName;
  }

  public short getAge() {
    return Age;
  }

  public void setAge(short Age) {
    this.Age = Age;
  }
}
