/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.core.internal.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for things and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 * @author Jörg Plewe - refactoring, error handling
 */
@Path(ThingResource.PATH_THINGS)
@Produces(MediaType.APPLICATION_JSON)
public class ThingResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(ThingResource.class);

    /** The URI path to this resource */
    public static final String PATH_THINGS = "things";
    
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ItemFactory itemFactory;
    private ItemRegistry itemRegistry;
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;
    private ManagedItemProvider managedItemProvider;
    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;

    @Context
    private UriInfo uriInfo;

    /**
     * create a new Thing
     * @param thingBean
     * @return Response holding the newly created Thing or error information
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(ThingDTO thingBean) {
        ThingUID thingUIDObject = new ThingUID(thingBean.UID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        // turn the ThingDTO's configuration into a Configuration
        Configuration configuration = getConfiguration(thingBean);

    	Status status;
    	Thing thing = thingRegistry.get(thingUIDObject);

    	//
    	// does the Thing already exist?
    	//
    	if( null == thing )
    	{   
	        // if not, create new Thing
	        thing = managedThingProvider.createThing(thingUIDObject.getThingTypeUID(), thingUIDObject, bridgeUID, configuration);
	        status = Status.CREATED;
    	}
    	else
    	{
    		// if so, report a conflict
    		status = Status.CONFLICT;
    	}

    	return getThingResponse(status, thing, "Thing " + thingUIDObject.toString() + " already exists!" );
    }

    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {

        Collection<Thing> things = thingRegistry.getAll();
        Set<EnrichedThingDTO> thingBeans = convertToListBean(things);

        return Response.ok(thingBeans).build();
    }

    @GET
    @Path("/{thingUID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByUID(@PathParam("thingUID") String thingUID) 
    {
        Thing thing = thingRegistry.get((new ThingUID(thingUID)));

        //
        // return Thing data if it does exist
        //
        if (thing != null) 
        {
        	return getThingResponse(Status.OK, thing, null);
        } 
        else 
        {
        	return getThingNotFoundResponse(thingUID);
        }
    }

    
    /**
     * link a Channel of a Thing to an Item
     * @param thingUID
     * @param channelId
     * @param itemName
     * @return Response with status/error information
     */
    @POST
    @Path("/{thingUID}/channels/{channelId}/link")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response link(@PathParam("thingUID") String thingUID, @PathParam("channelId") String channelId,
            String itemName) {

        Thing thing = thingRegistry.get(new ThingUID(thingUID));
        if (thing == null) {
            logger.warn("Received HTTP POST request at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        Channel channel = findChannel(channelId, thing);
        if (channel == null) {
            logger.info("Received HTTP POST request at '{}' for the unknown channel '{}' of the thing '{}'",
                    uriInfo.getPath(), channel, thingUID);
        	String message = "Channel " + channelId + " for Thing " + thingUID + " does not exist!";
        	return JSONResponse.createResponse( Status.NOT_FOUND, null, message);
        }

        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException ex) {
            GenericItem item = itemFactory.createItem(channel.getAcceptedItemType(), itemName);
            managedItemProvider.add(item);
        }

        ChannelUID channelUID = new ChannelUID(new ThingUID(thingUID), channelId);

        unlinkChannelIfAlreadyLinked(channelUID);

        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));

        return Response.ok().build();
    }

    
    /**
     * Delete a Thing, if possible.
     * Thing deletion might be impossible if the Thing is not managed, will return CONFLICT.
     * Thing deletion might happen delayed, will return ACCEPTED. 
     * @param thingUID
     * @param force
     * @return Response with status/error information
     */
    @DELETE
    @Path("/{thingUID}")
    public Response remove(@PathParam("thingUID") String thingUID,
            @DefaultValue("false") @QueryParam("force") boolean force) {

    	ThingUID thingUIDObject = new ThingUID(thingUID); 
    	
    	//
    	// check whether thing exists and throw 404 if not 
    	//
        Thing thing = thingRegistry.get(thingUIDObject);
        if (thing == null) {
            logger.info("Received HTTP DELETE request for update at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }    	

        //
        // ask whether the Thing exists as a managed thing, so it can get updated, 409 otherwise
        //
        Thing managed = managedThingProvider.get(thingUIDObject);
        if ( null == managed ) {
            logger.info("Received HTTP DELETE request for update at '{}' for an unmanaged thing '{}'.", uriInfo.getPath(), thingUID);
        	return getThingResponse(Status.CONFLICT, thing, "Cannot delete Thing " + thingUID + ". Maybe it is not managed.");
        }

        
        //
        // only move on if Thing is known to be managed, so it can get updated
        //
        
        if( force ) {
            if( null == thingRegistry.forceRemove(thingUIDObject) )
            {
            	return getThingResponse( Status.INTERNAL_SERVER_ERROR, thing, "Cannot delete Thing " + thingUID + " for unknown reasons.");
            }
        } else {
            if( null != thingRegistry.remove(thingUIDObject) )
            {
            	return getThingResponse( Status.ACCEPTED, thing, null );
            }
        }

        return Response.ok().build();
    }

    
    /**
     * Unlink a Channel of a Thing from an Item.
     * @param thingUID
     * @param channelId
     * @param itemName
     * @return Response with status/error information
     */
    @DELETE
    @Path("/{thingUID}/channels/{channelId}/link")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response unlink(@PathParam("thingUID") String thingUID, @PathParam("channelId") String channelId,
            String itemName) {

        ChannelUID channelUID = new ChannelUID(new ThingUID(thingUID), channelId);

        if (itemChannelLinkRegistry.isLinked(itemName, channelUID)) {
            managedItemChannelLinkProvider.remove(new ItemChannelLink(itemName, channelUID).getID());
        }

        return Response.ok().build();
    }

    
    /**
     * Update Thing.
     * @param thingUID
     * @param thingBean
     * @return Response with the updated Thing or error information
     * @throws IOException
     */
    @PUT
    @Path("/{thingUID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("thingUID") String thingUID, ThingDTO thingBean) throws IOException {

        ThingUID thingUIDObject = new ThingUID(thingUID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        //
        // ask whether the Thing exists at all, 404 otherwise
        //
        Thing thing = thingRegistry.get(thingUIDObject);
        if ( null == thing ) {
            logger.info("Received HTTP PUT request for update at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        
        //
        // ask whether the Thing exists as a managed thing, so it can get updated, 409 otherwise
        //
        Thing managed = managedThingProvider.get(thingUIDObject);
        if ( null == managed ) {
            logger.info("Received HTTP PUT request for update at '{}' for an unmanaged thing '{}'.", uriInfo.getPath(), thingUID);
        	return getThingResponse(Status.CONFLICT, thing, "Cannot update Thing " + thingUID + ". Maybe it is not managed.");
        }
        
        //
        // only process if Thing is known to be managed, so it can get updated
        //
        thing.setBridgeUID(bridgeUID);
        updateConfiguration(thing, getConfiguration(thingBean));

        //
        // update, returns null in case Thing cannot be found
        //
        Thing oldthing = managedThingProvider.update(thing);
        if( null == oldthing )
        {
            return getThingNotFoundResponse(thingUID);
        }

        // everything went well
        return getThingResponse(Status.OK, thing, null);
    }

    
    /**
     * Updates Thing configuration.
     * @param thingUID
     * @param configurationParameters
     * @return Response with the updated Thing or error information
     * @throws IOException
     */
    @PUT
    @Path("/{thingUID}/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConfiguration(@PathParam("thingUID") String thingUID,
            Map<String, Object> configurationParameters) throws IOException {

    	ThingUID thingUIDObject = new ThingUID(thingUID);
    	
        //
        // ask whether the Thing exists at all, 404 otherwise
        //
        Thing thing = thingRegistry.get(thingUIDObject);
        if ( null == thing ) {
            logger.info("Received HTTP PUT request for update configuration at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        //
        // ask whether the Thing exists as a managed thing, so it can get updated, 409 otherwise
        //
        Thing managed = managedThingProvider.get(thingUIDObject);
        if ( null == managed ) {
            logger.info("Received HTTP PUT request for update configuration at '{}' for an unmanaged thing '{}'.", uriInfo.getPath(), thingUID);
        	return getThingResponse(Status.CONFLICT, thing, "Cannot update Thing " + thingUID + ". Maybe it is not managed.");
        }
        
        //
        // only move on if Thing is known to be managed, so it can get updated
        //        
        thingRegistry.updateConfiguration(thingUIDObject, convertDoublesToBigDecimal(configurationParameters));

        return getThingResponse(Status.OK, thing, null);
    }


    
    /**
     * helper: Response to be sent to client if a Thing cannot be found
     * @param thingUID
     * @return Response configured for NOT_FOUND
     */
    private static Response getThingNotFoundResponse( String thingUID )
    {
    	String message = "Thing " + thingUID + " does not exist!";
    	return JSONResponse.createResponse( Status.NOT_FOUND, null, message);
    }
    

    /**
     * helper: create a Response holding a Thing and/or error information.
     * @param status
     * @param thing
     * @param errormessage
     * @return Response
     */
    private Response getThingResponse( Status status, Thing thing, String errormessage )
    {
    	Object entity = null!=thing ? EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri()) : null;
    	return JSONResponse.createResponse( status, entity, errormessage );
    }
    
    
    
    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void setItemFactory(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void setManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = managedItemChannelLinkProvider;
    }

    protected void setManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = managedItemProvider;
    }

    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void unsetItemFactory(ItemFactory itemFactory) {
        this.itemFactory = null;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void unsetManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = null;
    }

    protected void unsetManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = null;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    private Set<EnrichedThingDTO> convertToListBean(Collection<Thing> things) {
        Set<EnrichedThingDTO> thingBeans = new LinkedHashSet<>();
        for (Thing thing : things) {
            EnrichedThingDTO thingBean = EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri());
            thingBeans.add(thingBean);
        }
        return thingBeans;
    }

    private Channel findChannel(String channelId, Thing thing) {
        for (Channel channel : thing.getChannels()) {
            if (channel.getUID().getId().equals(channelId)) {
                return channel;
            }
        }
        return null;
    }

    private void unlinkChannelIfAlreadyLinked(ChannelUID channelUID) {
        Collection<ItemChannelLink> links = managedItemChannelLinkProvider.getAll();
        for (ItemChannelLink link : links) {
            if (link.getUID().equals(channelUID)) {
                logger.debug(
                        "Channel '{}' is already linked to item '{}' and will be unlinked before it will be linked to the new item.",
                        channelUID, link.getItemName());
                managedItemChannelLinkProvider.remove(link.getID());
            }
        }
    }

    public static Configuration getConfiguration(ThingDTO thingBean) {
        Configuration configuration = new Configuration();

        Map<String, Object> convertDoublesToBigDecimal = convertDoublesToBigDecimal(thingBean.configuration);
        configuration.setProperties(convertDoublesToBigDecimal);

        return configuration;
    }

    private static Map<String, Object> convertDoublesToBigDecimal(Map<String, Object> configuration) {
        Map<String, Object> convertedConfiguration = new HashMap<String, Object>(configuration.size());
        for (Entry<String, Object> parameter : configuration.entrySet()) {
            String name = parameter.getKey();
            Object value = parameter.getValue();
            convertedConfiguration.put(name, value instanceof Double ? new BigDecimal((Double) value) : value);
        }
        return convertedConfiguration;
    }

    public static void updateConfiguration(Thing thing, Configuration configuration) {
        for (String parameterName : configuration.keySet()) {
            thing.getConfiguration().put(parameterName, configuration.get(parameterName));
        }
    }

}
