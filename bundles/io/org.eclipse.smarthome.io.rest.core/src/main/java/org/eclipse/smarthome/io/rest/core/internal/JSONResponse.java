package org.eclipse.smarthome.io.rest.core.internal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Provider
public class JSONResponse
{
	public static final String JSON_KEY_ERROR_MESSAGE 	= "message";
	public static final String JSON_KEY_ERROR 			= "error";
	public static final String JSON_KEY_HTTPCODE 		= "http-code";

	// also dump stacktrace?
	private final static boolean WITH_STACKTRACE 		= true;

	final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	/**
	 * hide ctor a bit from public
	 */
	JSONResponse() 
	{
	}
	
	/**
	 * basic configuration of a ResponseBuilder
	 * @param status
	 * @return
	 */
	private static ResponseBuilder response(Response.Status status)
	{
		return Response.status(status).header("Content-Type", MediaType.APPLICATION_JSON);
	}

	
	private static JsonObject createErrorJson(String message, Response.Status status, Exception ex)
	{
		JsonObject ret 	= new JsonObject();
		JsonObject err 	= new JsonObject();
		ret.add( JSON_KEY_ERROR, err );

		err.addProperty( JSON_KEY_ERROR_MESSAGE, message );
		
		//
		// in case we have a http status code, report it
		//
		if( null != status )
		{
			err.addProperty(JSON_KEY_HTTPCODE, status.getStatusCode());
		}
		
		if( null != ex )
		{
			//
			// JSONify the Exception
			//
			JsonObject exc	= new JsonObject();
			err.add( "exception", exc );
			{
				exc.addProperty( "class", 				ex.getClass().getName() );
				exc.addProperty( "message", 			ex.getMessage() );
				exc.addProperty( "localized-message", 	ex.getLocalizedMessage() );
				exc.addProperty( "cause", 				null!=ex.getCause() ? ex.getCause().getClass().getName() : null );
				
				if( WITH_STACKTRACE )
					exc.add(	 "stacktrace", 			GSON.toJsonTree( ex.getStackTrace() ) );
			}
		}
		
		return ret;
	}


	public Response createErrorResponse( Response.Status status, String message )
	{
		JsonObject ret = createErrorJson(message, status, null);
		return response(status).entity( GSON.toJson(ret) ).build();
	}
	
	
	@Provider
	static class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception>
	{
		/**
		 * create JSON Response 
		 */
		@Override
		public Response toResponse(Exception e)
		{
			Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
			
			//
			// in case the Exception is a WebApplicationException, it already carries a Status
			//
			if( e instanceof WebApplicationException ) 
			{
				status = (Response.Status)((WebApplicationException)e).getResponse().getStatusInfo();
			}	
			
			JsonObject ret = createErrorJson(e.getMessage(), status, e);
			return response(status).entity( GSON.toJson(ret) ).build();
		}
	}
}
