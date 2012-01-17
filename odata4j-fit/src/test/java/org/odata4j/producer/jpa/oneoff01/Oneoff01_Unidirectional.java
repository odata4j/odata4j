package org.odata4j.producer.jpa.oneoff01;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.producer.jpa.oneoff.AbstractOneoffTestBase;

public class Oneoff01_Unidirectional extends AbstractOneoffTestBase {

  @Test
  public void createOnetoManyUniDirectional() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);

    OEntity comment = consumer
        .createEntity("Comment")
        .properties(OProperties.string("Description", "C1" + now))
        .get();

    OEntity ticket = consumer.createEntity("Ticket")
        .properties(OProperties.string("Description", "T" + now))
        .inline("Comments", comment)
        .execute();

    Assert.assertNotNull(ticket);
    ORelatedEntitiesLink link = ticket.getLink("Comments", ORelatedEntitiesLink.class);
    Assert.assertEquals(1, consumer.getEntities(link).execute().count());

  }

  @Test
  public void mergeOnetoManyUniDirectional() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);

    OEntity comment = consumer
        .createEntity("Comment")
        .properties(OProperties.string("Description", "C1" + now))
        .get();

    OEntity ticket = consumer.createEntity("Ticket")
        .properties(OProperties.string("Description", "T1" + now))
        .inline("Comments", comment)
        .execute();

    Assert.assertNotNull(ticket);

    consumer.mergeEntity(ticket)
        .properties(OProperties.string("Description", "T2" + now))
        .execute();

    Enumerable<OEntity> ticketEnum = consumer.getEntities("Ticket").filter("Description eq 'T2" + now + "'").execute();
    Assert.assertNotNull(ticketEnum);
    Assert.assertEquals(1, ticketEnum.count());

    OEntity ticket1 = ticketEnum.elementAt(0);
    ORelatedEntitiesLink link = ticket1.getLink("Comments", ORelatedEntitiesLink.class);
    Assert.assertEquals(1, consumer.getEntities(link).execute().count());

  }

  @Test
  public void mergeOnetoManyUniDirectional2() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);

    OEntity comment = consumer
        .createEntity("Comment")
        .properties(OProperties.string("Description", "C1" + now))
        .get();

    OEntity ticket = consumer.createEntity("Ticket")
        .properties(OProperties.string("Description", "T1" + now))
        .inline("Comments", comment)
        .execute();

    Assert.assertNotNull(ticket);

    ORelatedEntitiesLink link = ticket.getLink("Comments", ORelatedEntitiesLink.class);
    comment = consumer.getEntities(link).execute().first();

    consumer.mergeEntity(comment)
        .properties(OProperties.string("Description", "C2" + now))
        .execute();

    link = ticket.getLink("Comments", ORelatedEntitiesLink.class);
    comment = consumer.getEntities(link).execute().first();

    Assert.assertEquals("C2" + now, comment.getProperty("Description").getValue());

  }

  @Test
  public void deleteOnetoManyUniDirectional() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);

    OEntity comment = consumer
        .createEntity("Comment")
        .properties(OProperties.string("Description", "C1" + now))
        .get();

    OEntity ticket = consumer.createEntity("Ticket")
        .properties(OProperties.string("Description", "T1" + now))
        .inline("Comments", comment)
        .execute();

    Assert.assertNotNull(ticket);

    int beforeCommentCount = consumer.getEntities("Comment").execute().count();
    consumer.deleteEntity(ticket).execute();

    Enumerable<OEntity> ticketEnum = consumer.getEntities("Ticket").filter("Description eq 'T1" + now + "'").execute();
    Assert.assertEquals(0, ticketEnum.count());

    int afterCommentCount = consumer.getEntities("Comment").execute().count();
    Assert.assertTrue(beforeCommentCount > afterCommentCount ? true : false);
  }
}
