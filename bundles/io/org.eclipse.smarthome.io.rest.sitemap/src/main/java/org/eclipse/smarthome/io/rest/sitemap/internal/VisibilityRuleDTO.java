/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

import org.eclipse.smarthome.model.sitemap.SitemapFactory;
import org.eclipse.smarthome.model.sitemap.VisibilityRule;

/**
 * This is a data transfer object that is used to serialize VisibilityRule.
 */
public class VisibilityRuleDTO {

    public String sign;
    public String state;
    public String condition;
    public String itemname;

    public VisibilityRuleDTO( VisibilityRule vr ) {
    	sign 		= vr.getSign();
    	state 		= vr.getState();
    	condition 	= vr.getCondition();
    	itemname	= vr.getItem();
    }

    
	public VisibilityRule create() {
		
		VisibilityRule vr = SitemapFactory.eINSTANCE.createVisibilityRule();
		vr.setCondition(condition);
		vr.setItem(itemname);
		vr.setSign(sign);
		vr.setState(state);
		
		return vr;
	}

}
