package org.odata4j.producer.jpa.northwind.test;

public interface NorthwindTestUtils {

  void testJSONResult(String endpointUri, String uri, String inp);

  void testAtomResult(String endpointUri, String uri, String inp);

  void writeStringToFile(String fileName, String contents);

  String getCount(String endpointUri, String uri);

}