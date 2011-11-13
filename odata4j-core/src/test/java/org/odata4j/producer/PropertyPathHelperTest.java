/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.odata4j.producer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.expression.EntitySimpleProperty;

public class PropertyPathHelperTest {

  public PropertyPathHelperTest() {
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

  // this is the model for the tests:
  //   -------------0..* (Containers) -------EntityContainer
  //   |
  // Schema---------0..* (EntityTypes) ------EntityType---------(0..*)SubTypes----------EntityType
  //                                             |
  //                                             |---------------(0..8) Properties-------Property
  @Test
  public void testEmpty() {
    PropertyPathHelper h = new PropertyPathHelper(Collections.<EntitySimpleProperty>emptyList(), Collections.<EntitySimpleProperty>emptyList());

    // Path: "" type: Schema
    assertFalse(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());

    assertTrue(h.isSelected("Name"));
    assertTrue(h.isSelected("Alias"));
    assertTrue(h.isSelected("Namespace"));
    assertFalse(h.isExpanded("EntityTypes"));
    assertFalse(h.isExpanded("SubTypes"));
    assertFalse(h.isExpanded("Properties"));
    assertFalse(h.isExpanded("OtherNavProp"));

    // Path: "EntityTypes"
    h.navigate("EntityTypes");
    assertFalse(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());
    assertFalse(h.isExpanded("SubTypes"));   // EntityTypes/SubTypes
    assertFalse(h.isExpanded("Properties")); // EntityTypes/Properties
    assertFalse(h.isExpanded("EntityTypes")); // EntityTypes/EntityTypes

    assertTrue(h.isSelected("Namespace"));  // EntityTypes/Namespace
    assertTrue(h.isSelected("Name"));      // EntityTypes/Name
    assertTrue(h.isSelected("AnotherProp")); // EntityTypes/AnotherProp

    for (int i = 1; i < 100; i++) {
      h.navigate("SubTypes");
      assertFalse(h.isSelectionLimited());
      assertFalse(h.isSelectionLimitedRecursive());
      assertFalse(h.isExpanded("SubTypes"));
      assertFalse(h.isExpanded("Properties"));
      assertFalse(h.isExpanded("NotExpandedNavProp"));

      assertTrue(h.isSelected("Namespace"));
      assertTrue(h.isSelected("Name"));
      assertTrue(h.isSelected("NotSelected"));
    }
  }

  @Test
  public void testOne() {
    PropertyPathHelper h = new PropertyPathHelper("Name", "EntityTypes");

    // Path: "" type: Schema
    assertTrue(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());

    assertTrue(h.isSelected("Name"));
    assertFalse(h.isSelected("Alias"));
    assertFalse(h.isSelected("Namespace"));
    assertTrue(h.isExpanded("EntityTypes"));
    assertFalse(h.isExpanded("SubTypes"));
    assertFalse(h.isExpanded("Properties"));
    assertFalse(h.isExpanded("OtherNavProp"));

    // Path: "EntityTypes"
    h.navigate("EntityTypes");
    assertFalse(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());
    assertFalse(h.isExpanded("SubTypes"));   // EntityTypes/SubTypes
    assertFalse(h.isExpanded("Properties")); // EntityTypes/Properties
    assertFalse(h.isExpanded("EntityTypes")); // EntityTypes/EntityTypes

    assertTrue(h.isSelected("Namespace"));  // EntityTypes/Namespace
    assertTrue(h.isSelected("Name"));      // EntityTypes/Name
    assertTrue(h.isSelected("AnotherProp")); // EntityTypes/AnotherProp

    for (int i = 1; i < 100; i++) {
      h.navigate("SubTypes");
      assertFalse(h.isSelectionLimited());
      assertFalse(h.isSelectionLimitedRecursive());
      assertFalse(h.isExpanded("SubTypes"));
      assertFalse(h.isExpanded("Properties"));
      assertFalse(h.isExpanded("NotExpandedNavProp"));

      assertTrue(h.isSelected("Namespace"));
      assertTrue(h.isSelected("Name"));
      assertTrue(h.isSelected("NotSelected"));
    }
  }

  @Test
  public void testMulti() {
    PropertyPathHelper h = new PropertyPathHelper("Name,EntityTypes/Name", "EntityTypes,EntityTypes/SubTypes/Properties");

    // Path: "" type: Schema
    assertTrue(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());

    assertTrue(h.isSelected("Name"));
    assertFalse(h.isSelected("Alias"));
    assertFalse(h.isSelected("Namespace"));
    assertTrue(h.isExpanded("EntityTypes"));
    assertFalse(h.isExpanded("SubTypes"));
    assertFalse(h.isExpanded("Properties"));
    assertFalse(h.isExpanded("OtherNavProp"));

    // Path: "EntityTypes"
    h.navigate("EntityTypes");
    assertTrue(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());
    assertFalse(h.isExpanded("SubTypes"));   // EntityTypes/SubTypes
    assertFalse(h.isExpanded("Properties")); // EntityTypes/Properties
    assertFalse(h.isExpanded("EntityTypes")); // EntityTypes/EntityTypes

    assertFalse(h.isSelected("Namespace"));  // EntityTypes/Namespace
    assertTrue(h.isSelected("Name"));      // EntityTypes/Name
    assertFalse(h.isSelected("AnotherProp")); // EntityTypes/AnotherProp

    for (int i = 1; i < 100; i++) {
      h.navigate("SubTypes");
      assertFalse(h.isSelectionLimited());
      assertFalse(h.isSelectionLimitedRecursive());
      assertFalse(h.isExpanded("SubTypes"));

      assertTrue((i == 1 && h.isExpanded("Properties"))
              || (i > 1 && !h.isExpanded("Properties")));
      assertFalse(h.isExpanded("NotExpandedNavProp"));

      assertTrue(h.isSelected("Namespace"));
      assertTrue(h.isSelected("Name"));
      assertTrue(h.isSelected("NotSelected"));
    }
  }

  @Test
  public void testRecursive() {
    PropertyPathHelper h = new PropertyPathHelper(
            "Name,Alias", // $select
            "EntityTypes",
            "SubTypes/Namespace,SubTypes/Name", // $selectR
            "SubTypes/0,Properties/1");

    // Path: "" type: Schema
    assertTrue(h.getCurrentDepth() == 1);
    assertTrue(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());

    assertTrue(h.isSelected("Name"));
    assertTrue(h.isSelected("Alias"));
    assertFalse(h.isSelected("Namespace"));
    assertTrue(h.isExpanded("EntityTypes"));
    assertTrue(h.isExpanded("SubTypes"));
    assertTrue(h.isExpanded("Properties")); // this is level 1 so expanded
    assertFalse(h.isExpanded("OtherNavProp"));

    // Path: "EntityTypes"
    h.navigate("EntityTypes");
    assertTrue(h.getCurrentDepth() == 2);
    assertFalse(h.isSelectionLimited());
    assertFalse(h.isSelectionLimitedRecursive());
    assertTrue(h.isExpanded("SubTypes"));   // EntityTypes/SubTypes
    assertFalse(h.isExpanded("Properties")); // EntityTypes/Properties, Properties 1 level only!
    assertFalse(h.isExpanded("EntityTypes")); // EntityTypes/EntityTypes

    assertTrue(h.isSelected("Namespace"));  // EntityTypes/Namespace
    assertTrue(h.isSelected("Name"));      // EntityTypes/Name
    assertTrue(h.isSelected("AnotherProp")); // EntityTypes/AnotherProp

    for (int i = 1; i < 100; i++) {
      h.navigate("SubTypes");
      assertTrue(h.getCurrentDepth() == i + 2);
      assertFalse(h.isSelectionLimited());
      assertTrue(h.isSelectionLimitedRecursive());
      assertTrue(h.isExpanded("SubTypes"));
      assertFalse(h.isExpanded("Properties"));
      assertFalse(h.isExpanded("NotExpandedNavProp"));

      assertTrue(h.isSelected("Namespace"));
      assertTrue(h.isSelected("Name"));
      assertFalse(h.isSelected("NotSelected"));
    }
  }

  @Test
  public void testRecursiveOverride() {
    PropertyPathHelper h = new PropertyPathHelper(
            "Name,Alias,EntityTypes/SubTypes/Prop1", // $select
            "EntityTypes", // $expand
            "SubTypes/Namespace,SubTypes/Name", // $selectR
            "SubTypes/0,Properties/1");

    h.navigate("EntityTypes");
    h.navigate("SubTypes");
    assertTrue(h.isSelected("Prop1"));
    assertFalse(h.isSelected("Prop2"));
    // overridden
    assertTrue(h.isSelected("Namespace"));
    assertTrue(h.isSelected("Name"));
  }
  /* TODO, ExpressionParser doesn't handle wild cards */
}
