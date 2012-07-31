package org.odata4j.consumer;

import java.io.Reader;
import java.util.List;

import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatType;
import org.odata4j.format.SingleLink;
import org.odata4j.format.xml.AtomCollectionInfo;

public interface ODataClient {

  FormatType getFormatType();

  EdmDataServices getMetadata(ODataClientRequest request) throws ODataProducerException;

  Iterable<AtomCollectionInfo> getCollections(ODataClientRequest request) throws ODataProducerException;

  Iterable<SingleLink> getLinks(ODataClientRequest request) throws ODataProducerException;

  Response getEntity(ODataClientRequest request) throws ODataProducerException;

  Response getEntities(ODataClientRequest request) throws ODataProducerException;

  Response callFunction(ODataClientRequest request) throws ODataProducerException;

  Response createEntity(ODataClientRequest request) throws ODataProducerException;

  void updateEntity(ODataClientRequest request) throws ODataProducerException;

  void deleteEntity(ODataClientRequest request) throws ODataProducerException;

  void deleteLink(ODataClientRequest request) throws ODataProducerException;

  void createLink(ODataClientRequest request) throws ODataProducerException;

  void updateLink(ODataClientRequest request) throws ODataProducerException;

  Entry createRequestEntry(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> props, List<OLink> links);

  String requestBody(FormatType formatType, ODataClientRequest request) throws ODataProducerException;

  Reader getFeedReader(Response response);
}
