/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.active.event;

import org.ebayopensource.dsf.jsnative.events.EventTarget;


public interface IDomEventPublisher {
	
	boolean publish(String evtType, EventTarget src);
//	boolean publish(String evtType, EventTarget src, String value);

}