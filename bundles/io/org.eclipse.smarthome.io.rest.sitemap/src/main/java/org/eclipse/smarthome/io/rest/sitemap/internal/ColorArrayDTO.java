/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

import org.eclipse.smarthome.model.sitemap.ColorArray;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;

/**
 * This is a data transfer object that is used to serialize ColorArray.
 */
public class ColorArrayDTO {

    public String arg;
    public String sign;
    public String state;
    public String condition;
    public String itemname;

    public ColorArrayDTO( ColorArray ca ) {
    	arg 		= ca.getArg();
    	sign 		= ca.getSign();
    	state 		= ca.getState();
    	condition 	= ca.getCondition();
    	itemname	= ca.getItem();
    }

    
	public ColorArray create() {
		
		ColorArray ca = SitemapFactory.eINSTANCE.createColorArray();
		ca.setArg(arg);
		ca.setSign(sign);
		ca.setState(state);
		ca.setCondition(condition);
		ca.setItem(itemname);
		
		return ca;
	}

}
