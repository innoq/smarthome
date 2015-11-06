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
