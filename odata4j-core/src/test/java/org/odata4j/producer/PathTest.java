package org.odata4j.producer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tony Rozga
 */
public class PathTest {

  public PathTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testEmptyPath() {
    Path p = new Path("");
    assertTrue(p.isEmpty());
    assertTrue(p.getNComponents() == 0);
    assertTrue(p.getLastComponent() == null);
    assertTrue(p.getPath().equals(""));
    assertFalse(p.isWild());
    assertTrue(p.equals(new Path("")));
    p = p.addComponent("blar");
    assertTrue(p.equals(new Path("blar")));
    
    assertTrue(p.startsWith(new Path("")));
    assertFalse(p.startsWith(new Path("foo/bar")));
  }

  @Test
  public void testOnePath() {
    Path p = new Path("foo");
    assertFalse(p.isEmpty());
    assertTrue(p.getNComponents() == 1);
    assertTrue(p.getLastComponent().equals("foo"));
    assertTrue(p.getPath().equals("foo"));
    assertFalse(p.isWild());
    assertTrue(p.equals(new Path("foo")));
    p = p.addComponent("blar");
    assertTrue(p.equals(new Path("foo/blar")));
    
    assertTrue(p.startsWith(new Path("")));
    assertTrue(p.startsWith(new Path("foo")));
    assertTrue(p.startsWith(new Path("foo/blar")));
    assertFalse(p.startsWith(new Path("foobar/blar")));
  }

  @Test
  public void testMultiPath() {
    Path p = new Path("foo/bar");
    assertFalse(p.isEmpty());
    assertTrue(p.getNComponents() == 2);
    assertTrue(p.getLastComponent().equals("bar"));
    assertTrue(p.getPath().equals("foo/bar"));
    assertFalse(p.isWild());
    assertTrue(p.equals(new Path("foo/bar")));
    p = p.addComponent("blar");
    assertTrue(p.equals(new Path("foo/bar/blar")));
  }

  @Test
  public void testOneWild() {
    Path p = new Path("*");
    assertFalse(p.isEmpty());
    assertTrue(p.getNComponents() == 1);
    assertTrue(p.getLastComponent().equals("*"));
    assertTrue(p.getPath().equals("*"));
    assertTrue(p.isWild());
    assertTrue(p.equals(new Path("*")));
  }

  @Test
  public void testMultiWild() {
    Path p = new Path("foo/bar/*");
    assertFalse(p.isEmpty());
    assertTrue(p.getNComponents() == 3);
    assertTrue(p.getLastComponent().equals("*"));
    assertTrue(p.getPath().equals("foo/bar/*"));
    assertTrue(p.isWild());
    assertTrue(p.equals(new Path("foo/bar/*")));
  }

  @Test
  public void testRemoveFirst() {
    Path p = new Path("");
    p = p.removeFirstComponent();
    assertTrue(p.isEmpty());

    p = new Path("foobar");
    p = p.removeFirstComponent();
    assertTrue(p.isEmpty());

    p = new Path("foo/bar/blat");
    p = p.removeFirstComponent();
    assertFalse(p.isEmpty());
    assertTrue(p.equals(new Path("bar/blat")));
  }

  @Test
  public void testRemoveLast() {
    Path p = new Path("");
    p = p.removeLastComponent();
    assertTrue(p.isEmpty());

    p = new Path("foobar");
    p = p.removeLastComponent();
    assertTrue(p.isEmpty());

    p = new Path("foo/bar/blat");
    p = p.removeLastComponent();
    assertFalse(p.isEmpty());
    assertTrue(p.equals(new Path("foo/bar")));
  }
}
