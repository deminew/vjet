/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.mod.wst.jsdt.internal.oaametadata;

public class Method extends VersionableElement {
	public String scope;
	public String visibility;
	public String name;
	
	public boolean isContructor;
	
	public Exception [] exceptions;
	public Parameter [] parameters;
	public ReturnsData returns;
	public boolean isStatic() {
		return IOAAMetaDataConstants.USAGE_STATIC.equals(scope);
	}
}
