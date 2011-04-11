package org.ebayopensource.vjet.eclipse.core;

public interface ClassFileConstants {

	int AccDefault = 0;
	/*
	 * Modifiers
	 */
	int AccPublic = 0x0001;
	int AccPrivate = 0x0002;
	int AccProtected = 0x0004;
	int AccStatic = 0x0008;
	int AccFinal = 0x0010;
	int AccVarargs = 0x0080;
	int AccInterface = 0x0200;
	int AccAbstract = 0x0400;
	int AccAnnotation = 0x2000;
	int AccEnum = 0x4000;
	int AccModule = 0x8000;
}