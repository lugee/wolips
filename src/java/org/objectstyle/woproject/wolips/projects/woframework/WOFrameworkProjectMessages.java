/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.objectstyle.woproject.wolips.projects.woframework;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class WOFrameworkProjectMessages {

	private static final String RESOURCE_BUNDLE= WOFrameworkProjectMessages.class.getName();
	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private WOFrameworkProjectMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(getString(key), new Object[] { arg });
	}


	/**
	 * Gets a string from the resource bundle and formats it with arguments
	 */	
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}


}
