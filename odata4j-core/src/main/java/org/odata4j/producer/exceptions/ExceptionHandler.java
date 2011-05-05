package org.odata4j.producer.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.producer.resources.EntityRequestResource;

public class ExceptionHandler {
	private static final Logger log = Logger.getLogger(EntityRequestResource.class.getName());
	
	public static Response Handle(Exception e) {
		log.log(Level.WARNING, e.getMessage());
		
		if(e instanceof NotAuthorizedException) {
			return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
		}
		else if(e instanceof BadRequestException) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		else if(e instanceof NotFoundException || e instanceof TypeNotPresentException) {
			return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();			
		}
		else if(e instanceof ForbiddenException) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
		}
		else if(e instanceof MethodNotAllowedException) {
			return Response.status(405).entity(e.getMessage()).build();
		}
		else if(e instanceof ServerErrorException) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		else if(e instanceof NotImplementedException) {
			return Response.status(501).entity(e.getMessage()).build();
		}
		
				
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
	}
}
