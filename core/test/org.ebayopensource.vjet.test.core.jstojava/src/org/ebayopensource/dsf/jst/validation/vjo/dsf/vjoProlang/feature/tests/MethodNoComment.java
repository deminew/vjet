/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/* 
 * $Id: MethodNoComment.java.java, Jun 21, 2009, 12:20:41 AM, liama. Exp$:
 * Copyright (c) 2006-2009 Ebay Technologies. All Rights Reserved.
 * This software program and documentation are copyrighted by Ebay
 * Technologies.
 */
package org.ebayopensource.dsf.jst.validation.vjo.dsf.vjoProlang.feature.tests;
import static com.ebay.junitnexgen.category.Category.Groups.FAST;
import static com.ebay.junitnexgen.category.Category.Groups.P3;
import static com.ebay.junitnexgen.category.Category.Groups.UNIT;

import java.util.List;

import org.ebayopensource.dsf.jsgen.shared.validation.vjo.VjoSemanticProblem;
import org.ebayopensource.dsf.jst.validation.vjo.VjoValidationBaseTester;
import org.junit.Before;
import org.junit.Test;

import com.ebay.junitnexgen.category.Category;
import com.ebay.junitnexgen.category.Description;
import com.ebay.junitnexgen.category.ModuleInfo;

/**
 * MethodNoComment.java
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
@Category( { P3, FAST, UNIT })
@ModuleInfo(value="DsfPrebuild",subModuleId="JsToJava")
public class MethodNoComment extends VjoValidationBaseTester {

    @Before
    public void setUp() {
        expectProblems.clear();
    }

    @Test
    @Category( { P3, FAST, UNIT })
    @Description("Test DSF project, To validate false positive ")
    public void testMethodNoComment() {
        List<VjoSemanticProblem> problems = getVjoSemanticProblem(
                "dsf.vjoProlang.feature.tests.", "MethodNoComment.js", this
                        .getClass());
        assertProblemEquals(expectProblems, problems);
    }
}
