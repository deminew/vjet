/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.javatojs.tests.data.datatype;

public enum CoinComplexEnum {
	PENNY(1, "Penny", false), 
	NICKEL(5, "Nickel", false), 
	DIME(10, "Dime", false), 
	QUARTER(25, "Quarter", true);
	
	private final int m_value;
	private final String m_display;
	private final boolean m_canUseForLaundry;
	
	CoinComplexEnum(int value, String display, boolean canUseForLaundry) { 
		m_value = value; 
		m_display = display;
		m_canUseForLaundry = canUseForLaundry;
	}
	
	public int value() { return m_value; }

	public String display() {
		return m_display;
	}

	public boolean canUseForLaundry() {
		return m_canUseForLaundry;
	}

}