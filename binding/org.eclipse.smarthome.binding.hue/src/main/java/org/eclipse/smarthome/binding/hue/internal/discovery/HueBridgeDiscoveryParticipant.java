/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;
import static org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration.*;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;


/**
 * The {@link HueBridgeDiscoveryParticipant} is responsible for discovering new and
 * removed hue bridges. It uses the central {@link UpnpDiscoveryService}.
 * 
 * @author Kai Kreuzer - Initial contribution
 * 
 */
public class HueBridgeDiscoveryParticipant implements UpnpDiscoveryParticipant {

	@Override
	public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
		return Collections.singleton(THING_TYPE_BRIDGE);
	}

	@Override
	public DiscoveryResult createResult(RemoteDevice device) {
		ThingUID uid = getThingUID(device);
		if(uid!=null) {
	        Map<String, Object> properties = new HashMap<>(2);
	        properties.put(IP_ADDRESS, device.getDetails().getBaseURL().getHost());
	        properties.put(SERIAL_NUMBER, device.getDetails().getSerialNumber());

	        DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel(device.getDetails().getFriendlyName())
					.build();
	        return result;
		} else {
			return null;
		}
	}

	@Override
	public ThingUID getThingUID(RemoteDevice device) {
		if(device.getDetails().getModelDetails().getModelName().startsWith("Philips hue bridge")) {
			return new ThingUID(THING_TYPE_BRIDGE, device.getDetails().getSerialNumber());
		} else {
			return null;
		}
	}

}
