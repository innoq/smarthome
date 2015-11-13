/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.io.rest.sitemap.managed;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;

import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.io.rest.sitemap.internal.SitemapDTO;

public class ManagedSitemapProvider extends DefaultAbstractManagedProvider<SitemapDTO, String> implements SitemapProvider {

	public void createOrUpdate( SitemapDTO smDTO ) {
		
		// exists?
		if( null != super.get( getKey(smDTO) ) ) {
			
			// if so, update  
			super.update( smDTO );
		} else {
			
			// if not, create
			super.add( smDTO );
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// 
	// implement SitemapProvider
	//
	///////////////////////////////////////////////////////////////////////////////////////////////
		
	@Override
	public Sitemap getSitemap(String sitemapName) {
		SitemapDTO dto = super.get(sitemapName);

		if( null == dto ) return null;
		
		return dto.create();
	}

	@Override
	public Set<String> getSitemapNames() {
		Collection<SitemapDTO> all = super.getAll();
		
		Set<String> ret = new HashSet<>();
		for( SitemapDTO sm : all ) {
			ret.add(sm.name);
		}
		return ret;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// 
	// implement DefaultAbstractManagedProvider
	//
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getKey(SitemapDTO element) {
		return element.name;
	}

	@Override
	protected String getStorageName() {
        return Sitemap.class.getName();	}

	@Override
	protected String keyToString(String key) {
		return key;
	}

}
