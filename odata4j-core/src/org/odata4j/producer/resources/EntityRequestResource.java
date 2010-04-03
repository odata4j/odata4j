package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OProperty;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.xml.AtomFeedWriter;

import com.sun.jersey.api.core.HttpContext;

@Path("{entitySetName}{id: (\\(.*\\))}")
public class EntityRequestResource extends BaseResource {

	private static final Logger log = Logger.getLogger(EntityRequestResource.class.getName());
	
	@PUT
	public Response updateEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName, 
			@PathParam("id") String id){
		
		log.info(String.format("updateEntity(%s,%s)",entitySetName,id));
		
		List<OProperty<?>> properties = this.getRequestEntityProperties(context.getRequest());
		
		Object idObject = idObject(id);
		
		producer.updateEntity(entitySetName, idObject, properties);
		
		return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION,ODataConstants.DATA_SERVICE_VERSION).build();
	}
	
	@POST
	public Response mergeEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName, 
			@PathParam("id") String id){
		
		if (!"MERGE".equals(context.getRequest().getHeaderValue(ODataConstants.Headers.X_HTTP_METHOD)))
			throw new RuntimeException("Expected a tunnelled MERGE");
			
		log.info(String.format("mergeEntity(%s,%s)",entitySetName,id));
		
		List<OProperty<?>> properties = this.getRequestEntityProperties(context.getRequest());
		
		Object idObject = idObject(id);
		
		producer.mergeEntity(entitySetName, idObject, properties);
		
		return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION,ODataConstants.DATA_SERVICE_VERSION).build();
	}
	
	
	@DELETE
	public Response deleteEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName, 
			@PathParam("id") String id){
		
		log.info(String.format("getEntity(%s,%s)",entitySetName,id));
		
		Object idObject = idObject(id);
		
		producer.deleteEntity(entitySetName, idObject);
		
		return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION,ODataConstants.DATA_SERVICE_VERSION).build();
	}
	
	@GET
	@Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET)
	public Response getEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName, 
			@PathParam("id") String id){
		
		log.info(String.format("getEntity(%s,%s)",entitySetName,id));
		
		Object idObject =idObject(id);
			
		EntityResponse response = producer.getEntity(entitySetName,idObject);
		
		if (response==null)
			return Response.status(Status.NOT_FOUND).build();
		
		String baseUri = context.getUriInfo().getBaseUri().toString();
		StringWriter sw = new StringWriter();
		AtomFeedWriter.generateResponseEntry(baseUri,response,sw);
		String entity = sw.toString();
		
		return Response.ok(entity,ODataConstants.APPLICATION_ATOM_XML_CHARSET).header(ODataConstants.Headers.DATA_SERVICE_VERSION,ODataConstants.DATA_SERVICE_VERSION).build();
		
	}
	
	
	
	
	
	
	
	
	
	private Object idObject(String id){
		String cleanid = null;
		if (id != null && id.length()>0){
			if (id.startsWith("(") && id.endsWith(")")){
				cleanid = id.substring(1,id.length()-1);
				//log.info("cleanid!: " + cleanid);
			}
		}
		if (cleanid==null)
			throw new RuntimeException("unable to parse id");
		
		Object idObject;
		if (cleanid.startsWith("'") && cleanid.endsWith("'")){
			idObject = cleanid.substring(1,cleanid.length()-1);
		} else {
			idObject = Integer.parseInt(cleanid);
		}
		return idObject;
	}
	

}
