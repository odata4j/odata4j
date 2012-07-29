package org.odata4j.examples.producer.inmemory;

import static org.odata4j.examples.JaxRsImplementation.JERSEY;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.core4j.Func;
import org.odata4j.examples.ODataServerFactory;
import org.odata4j.examples.producer.inmemory.addressbook.Employee;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

public class AddressBookInMemoryExample {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static Date getDate(String dateAsString) {
    try {
      return DATE_FORMAT.parse(dateAsString);
    } catch (ParseException e) {
      return null;
    }
  }

  public static InMemoryProducer createProducer() {
    InMemoryProducer producer = new InMemoryProducer(AddressBookInMemoryExample.class.getName());

    producer.register(Employee.class, "Employees", new Func<Iterable<Employee>>() {
      public Iterable<Employee> apply() {
        List<Employee> employees = new ArrayList<Employee>();
        employees.add(new Employee("1", "Walter Winter", (short) 52, getDate("1999-01-01")));
        employees.add(new Employee("2", "Frederic Fall", (short) 32, getDate("2003-07-01")));
        return employees;
      }
    }, "EmployeeId");

    return producer;
  }

  public static void main(String[] args) {
    DefaultODataProducerProvider.setInstance(createProducer());
    new ODataServerFactory(JERSEY).hostODataServer("http://localhost:8888/AddressBookInMemoryExample.svc/");
  }
}
