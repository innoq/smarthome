/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;
import org.eclipse.smarthome.model.sitemap.*;
import org.omg.PortableInterceptor.INACTIVE;

/**
 * This is a data transfer object that is used to serialize widgets.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Chris Jackson
 *
 */
public class WidgetDTO {

    public String widgetId;
    public String type;
    public String name;

    public String label;		// rendered label ready for display 
    public String labelformat;	// label format used for label rendering
    public String icon;
    public String labelcolor;
    public String valuecolor;

    // widget-specific attributes
    public ArrayList<MappingDTO> mappings = new ArrayList<MappingDTO>();
    public Boolean switchSupport;
    public Integer sendFrequency;
    public String separator;
    public Integer refresh;
    public Integer height;
    public BigDecimal minValue;
    public BigDecimal maxValue;
    public BigDecimal step;
    public String url;
    public String service;
    public String period;

    public String itemname;			// explicit name of the linked item, same as item.name, used mainly for PUTs, where the item DTO might not be present
    public EnrichedItemDTO item;
    
    // in case the widget is not a Frame, it's children get rendered as an own page
    public PageDTO linkedPage;

    // only for Frames, other LinkableWidgets use linkedPage to contain the subwidgets
    public final ArrayList<WidgetDTO> widgets = new ArrayList<WidgetDTO>();

    public final ArrayList<ColorArrayDTO> labelcolors = new ArrayList<>();
    public final ArrayList<ColorArrayDTO> valuecolors = new ArrayList<>();
    public final ArrayList<VisibilityRuleDTO> visibilityrules = new ArrayList<>();

    
    public enum Visibility {
    	INVISIBLE,
    	INACTIVE,
    	ACTIVE
    }
        
    public Visibility visibility;
    
    
    public WidgetDTO() {
    }

    
    /**
     * create a Widget model object from the attributes
     * @return
     */
    Widget create() {
    	
    	//
    	// dispatch linkable widgets
    	//
    	if( SitemapPackage.Literals.FRAME.getName().equals(type) ) {
    		Frame w = setupWidget( SitemapFactory.eINSTANCE.createFrame() );
    		setupLinkableWidget(w);
    		return w;
    	}
    	else if( SitemapPackage.Literals.TEXT.getName().equals(type) ) {
    		Text w = setupWidget( SitemapFactory.eINSTANCE.createText() );
    		setupLinkableWidget(w);
    		return w;
    	}
    	else if( SitemapPackage.Literals.GROUP.getName().equals(type) ) {
    		Group w = setupWidget( SitemapFactory.eINSTANCE.createGroup() );
    		setupLinkableWidget(w);
    		return w;
    	}
    	else if( SitemapPackage.Literals.IMAGE.getName().equals(type) ) {
    		Image w = setupWidget( SitemapFactory.eINSTANCE.createImage() );
    		setupLinkableWidget(w);
    		w.setUrl(url);
    		w.setRefresh(refresh);
    		return w;
    	}
    	//
    	// dispatch nonlinkable widgets
    	//
    	else if( SitemapPackage.Literals.CHART.getName().equals(type) ) {
    		Chart w = setupWidget( SitemapFactory.eINSTANCE.createChart() );
    		w.setRefresh(refresh);
    		w.setPeriod(period);
    		w.setService(service);
    		return w;
    	}
    	else if( SitemapPackage.Literals.COLORPICKER.getName().equals(type) ) {
    		Colorpicker w = setupWidget( SitemapFactory.eINSTANCE.createColorpicker() );
    		// ?? Colorpicker not supported in SitemapResource.createWidgetBean()
    		w.setFrequency(sendFrequency);
    		return w;
    	}
    	else if( SitemapPackage.Literals.LIST.getName().equals(type) ) {
    		List w = setupWidget( SitemapFactory.eINSTANCE.createList() );
    		w.setSeparator(separator);
    		return w;
    	}
    	else if( SitemapPackage.Literals.MAPVIEW.getName().equals(type) ) {
    		Mapview w = setupWidget( SitemapFactory.eINSTANCE.createMapview() );
    		w.setHeight(height);
    		return w;
    	}
    	else if( SitemapPackage.Literals.SELECTION.getName().equals(type) ) {
    		Selection w = setupWidget( SitemapFactory.eINSTANCE.createSelection() );
    		for( MappingDTO mDTO : mappings ) {
    			w.getMappings().add( mDTO.create() );
    		}
    		return w;
    	}
    	else if( SitemapPackage.Literals.SWITCH.getName().equals(type) ) {
    		Switch w = setupWidget( SitemapFactory.eINSTANCE.createSwitch() );
    		for( MappingDTO mDTO : mappings ) {
    			w.getMappings().add( mDTO.create() );
    		}
    		return w;
    	}
    	else if( SitemapPackage.Literals.SETPOINT.getName().equals(type) ) {
    		Setpoint w = setupWidget( SitemapFactory.eINSTANCE.createSetpoint() );
    		w.setMinValue(minValue);
    		w.setMaxValue(maxValue);
    		w.setStep(step);
    		return w;
    	}
    	else if( SitemapPackage.Literals.SLIDER.getName().equals(type) ) {
    		Slider w = setupWidget( SitemapFactory.eINSTANCE.createSlider() );
    		w.setFrequency(sendFrequency);
    		w.setSwitchEnabled(switchSupport);
    		return w;
    	}
    	else if( SitemapPackage.Literals.VIDEO.getName().equals(type) ) {
    		Video w = setupWidget( SitemapFactory.eINSTANCE.createVideo() );
    		w.setUrl(url);
    		return w;
    	}
    	else if( SitemapPackage.Literals.WEBVIEW.getName().equals(type) ) {
    		Webview w = setupWidget( SitemapFactory.eINSTANCE.createWebview() );
    		w.setHeight(height);
    		w.setUrl(url);
    		return w;
    	}
    	
    	// fallback if no specific type could be identified
    	return setupWidget( SitemapFactory.eINSTANCE.createWidget() );
    }

    
    /**
     * basic configuration of a widget: icon, label, itemname, labelcolor, valuecolor
     * @param w
     * @return
     */
    private <T extends Widget> T setupWidget( T w ) {
    	
    	//
    	// set basic properties
    	//
    	w.setIcon(icon);
    	w.setLabel(labelformat);
    	w.setItem(itemname); // remember item name from the explicit attribute bc. this.item might be missing

    	//
    	// configure label and value colors
    	//
    	for( ColorArrayDTO caDTO : labelcolors ) {
			w.getLabelColor().add( caDTO.create() );    		
    	}
    	for( ColorArrayDTO caDTO : valuecolors ) {
			w.getValueColor().add( caDTO.create() );    		
    	}

    	//
    	// configure visibility rules
    	//
    	for( VisibilityRuleDTO vrDTO : visibilityrules ) {
    		w.getVisibility().add( vrDTO.create() );
    	}
    	
    	return w;
    }
    
    
    /**
     * configure children of a LinkableWidget
     * @param w
     * @return
     */
    private <T extends LinkableWidget> T setupLinkableWidget( T w ) {
    	
    	//
    	// There might be linkedPage in case of w not being a Frame. 
    	// This linkedPage also contains the sub-widgets rendered for w on GET. These subwidgets might
    	// be dynamically enumerated from the members of a GroupItem in case no explicit children are 
    	// configured in the Widget itself (see ItemUIRegistryImpl#getChildren and ItemUIRegistryImpl#getDynamicGroupChildren).
    	//
    	// The children of the linkedPage are ignored here. In case subwidgets have to be created, the caller
    	// must place them in the widgets array.
    	// In case the subwidgets shall be dynamically enumerated, make sure widgets array is empty.
    	//
    	
    	//
    	// fill list of children
    	//
    	for( WidgetDTO wdto : widgets ) {
    		w.getChildren().add( wdto.create() );
    	}
    	
    	return w;
    }

}
