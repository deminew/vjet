/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.parser.comments;

import java.util.List;

import org.ebayopensource.dsf.jst.declaration.JstModifiers;
import org.ebayopensource.dsf.jstojava.parser.comments.JsType.ArgType;

public interface IJsCommentMeta {
	public static enum DIRECTION {
	        BACK, FORWARD
	};
	
	int getBeginOffset();
	
	int getEndOffset();

	String getName();
	
	DIRECTION getDirection();

	JsTypingMeta getTyping();
	
	List<ArgType> getArgs();
	
	JstModifiers getModifiers();
	
	boolean isCast();
	
	JsAnnotation getAnnotation();
	
	boolean isAnnotation();
	
	boolean isMethod();
	
	String getCommentSrc();
}
