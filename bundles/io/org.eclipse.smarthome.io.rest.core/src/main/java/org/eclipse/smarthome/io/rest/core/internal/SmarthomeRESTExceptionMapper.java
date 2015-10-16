package org.eclipse.smarthome.io.rest.core.internal;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Provider
public class SmarthomeRESTExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception>
{
	public static final String JSON_KEY_ERROR_MESSAGE 	= "message";
	public static final String JSON_KEY_ERROR 			= "error";
	
	// also dump stacktrace?
	private final static boolean WITH_STACKTRACE 		= true;

	private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	/**
	 * hide ctor a bit from public
	 */
	SmarthomeRESTExceptionMapper() 
	{
	}
	
	public Response makeErrorResponse( Response.Status status, String message )
	{
		return Response.status(status).build();
	}
	
	
	/**
	 * create JSON Response 
	 */
	public Response toResponse(Exception e)
	{

		JsonObject ret 	= new JsonObject();
		JsonObject err 	= new JsonObject();
		ret.add( JSON_KEY_ERROR, err );

		err.addProperty( JSON_KEY_ERROR_MESSAGE, e.getMessage() );

		//
		// JSONify the Exception
		//
		JsonObject exc	= new JsonObject();
		err.add( "exception", exc );
		{
			exc.addProperty( "class", 				e.getClass().getName() );
			exc.addProperty( "message", 			e.getMessage() );
			exc.addProperty( "localized-message", 	e.getLocalizedMessage() );
			exc.addProperty( "cause", 				null!=e.getCause() ? e.getCause().getClass().getName() : null );
			
			if( WITH_STACKTRACE )
				exc.add(	 "stacktrace", 			GSON.toJsonTree( e.getStackTrace() ) );
		}       	
		
    	return Response.serverError().header("Content-Type", MediaType.APPLICATION_JSON).entity( GSON.toJson( ret ) ).build();
	}
}
