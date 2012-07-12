package org.odata4j.consumer;

import java.io.Reader;
import java.util.List;

import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatType;
import org.odata4j.format.SingleLink;
import org.odata4j.format.xml.AtomCollectionInfo;

public interface ODataClient {

  FormatType getFormatType();

  EdmDataServices getMetadata(ODataClientRequest request) throws ODataServerException, ODataClientException;

  Iterable<AtomCollectionInfo> getCollections(ODataClientRequest request) throws ODataServerException, ODataClientException;

  Iterable<SingleLink> getLinks(ODataClientRequest request) throws ODataServerException, ODataClientException;

  Response getEntity(ODataClientRequest request) throws ODataServerException, ODataClientException;

  Response getEntities(ODataClientRequest request) throws ODataServerException, ODataClientException;

  Response callFunction(ODataClientRequest request) throws ODataServerException, ODataClientException;

  Response createEntity(ODataClientRequest request) throws ODataServerException, ODataClientException;

  void updateEntity(ODataClientRequest request) throws ODataServerException, ODataClientException;

  void deleteEntity(ODataClientRequest request) throws ODataServerException, ODataClientException;

  void deleteLink(ODataClientRequest request) throws ODataServerException, ODataClientException;

  void createLink(ODataClientRequest request) throws ODataServerException, ODataClientException;

  void updateLink(ODataClientRequest request) throws ODataServerException, ODataClientException;

  Entry createRequestEntry(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> props, List<OLink> links);

  String requestBody(FormatType formatType, ODataClientRequest request);

  Reader getFeedReader(Response response);
}
