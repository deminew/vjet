package org.eclipse.dltk.mod.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.dltk.mod.core.messages"; //$NON-NLS-1$
	public static String SourceParserUtil_errorRetrievingContent;
	public static String SourceParserUtil_failedToCreateSourceParser;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
