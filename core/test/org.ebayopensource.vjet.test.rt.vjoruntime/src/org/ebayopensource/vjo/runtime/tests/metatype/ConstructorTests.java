/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjo.runtime.tests.metatype;
import static com.ebay.junitnexgen.category.Category.Groups.FF;
import static com.ebay.junitnexgen.category.Category.Groups.IE;
import static com.ebay.junitnexgen.category.Category.Groups.P1;
import static com.ebay.junitnexgen.category.Category.Groups.P2;

import org.junit.Test;

import org.ebayopensource.dsf.jsnative.anno.BrowserType;
import com.ebay.junitnexgen.category.Category;
import com.ebay.junitnexgen.category.Module;
import org.ebayopensource.vjo.runtime.tests.BaseTestClass;

public class ConstructorTests extends BaseTestClass {

	private static final String CONSTZRUCTORS_TEST_VJO = "org.ebayopensource.vjo.runtime.tests.metatype.jstests.ConstructorsTest";

	@Test
	@Category( {P1,IE })
	@Module("VjoRuntimeTests")
	public void testConstructorTests_MSIE() throws Exception {

		runJsTest(CONSTZRUCTORS_TEST_VJO, BrowserType.IE_8);
	}

	@Test
	@Category( {P2,FF })
	@Module("VjoRuntimeTests")
	public void testConstructorTests_FIREFOX() throws Exception {
		runJsTest(CONSTZRUCTORS_TEST_VJO, BrowserType.FIREFOX_3);
	}

//	@Test
//	@Category( {P2,OPERA })
//	public void constructorTests_OPERA() throws Exception {
//		runJsTest(CONSTZRUCTORS_TEST_VJO, BrowserType.OPERA_9);
//	}
//
//	@Test
//	@Category( {P2,SAFARI })
//	public void constructorTests_SAFARI() throws Exception {
//		runJsTest(CONSTZRUCTORS_TEST_VJO, BrowserType.SAFARI_3);
//	}

}
