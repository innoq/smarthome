/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

/**
 * This exception is thrown by the {@link ItemRegistry} if an item could
 * not be found.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ItemNotFoundException extends ItemLookupException {

    public ItemNotFoundException(String name) {
        super("Item '" + name + "' could not be found in the item registry");
    }

    private static final long serialVersionUID = -3720784568250902711L;

}
