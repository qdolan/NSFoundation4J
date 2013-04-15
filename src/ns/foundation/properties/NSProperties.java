/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package ns.foundation.properties;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import ns.foundation.NSForwardException;
import ns.foundation.NSLog.Logger;
import ns.foundation.NSPropertyListSerialization;
import ns.foundation.collections.NSArray;
import ns.foundation.collections.NSDictionary;
import ns.foundation.collections.NSMutableArray;
import ns.foundation.collections.NSMutableDictionary;
import ns.foundation.kvc.NSKeyValueCoding;
import ns.foundation.notifications.NSNotificationCenter;
import ns.foundation.utilities.NSValueUtilities;

/**
 * Collection of simple utility methods used to get and set properties
 * in the system properties. The only reason this class is needed is
 * because all of the methods in NSProperties have been deprecated.
 * This is a wee bit annoying. The usual method is to have a method
 * like <code>getBoolean</code> off of Boolean which would resolve
 * the System property as a Boolean object.
 * 
 * Properties can be set in all the following places:
 * <ul>
 * <li>Properties in a bundle Resources directory</li>
 * <li>Properties.dev in a bundle Resources directory</li>
 * <li>Properties.username in a bundle Resources directory </li>
 * <li>~/Library/WebObjects.properties file</li>
 * <li>in the eclipse launcher or on the command-line</li>
 * </ul>
 * 
 * TODO - If this would fallback to calling the System getProperty, we
 * could ask that Project Wonder frameworks only use this class.
 * 
 * @property er.extensions.ERXProperties.RetainDefaultsEnabled
 */
public class NSProperties extends Properties implements NSKeyValueCoding {
    private static final long serialVersionUID = -5209747642382576078L;

    /** default string */
    public static final String DefaultString = "Default";
    
    private static String UndefinedMarker = "-undefined-";
    /** logging support */
    public final static Logger log = Logger.getLogger(NSProperties.class);

    /** Internal cache of type converted values to avoid reconverting attributes that are asked for frequently */
    private static Map<Object, Object> _cache = Collections.synchronizedMap(new HashMap<Object, Object>());

    /**
     * Cover method for returning an NSArray for a
     * given system property.
     * @param s system property
     * @return array de-serialized from the string in
     *      the system properties
     */
    public static NSArray<?> arrayForKey(String s) {
        return arrayForKeyWithDefault(s, null);
    }

    /**
     * Cover method for returning an NSArray for a
     * given system property and set a default value if not given.
     * @param s system property
     * @param defaultValue default value
     * @return array de-serialized from the string in
     *      the system properties or default value
     */
    public static NSArray<?> arrayForKeyWithDefault(final String s, final NSArray<?> defaultValue) {
		NSArray<?> value;
		Object cachedValue = _cache.get(s);
		if (cachedValue == UndefinedMarker) {
			value = defaultValue;
		} else if (cachedValue instanceof NSArray) {
			value = (NSArray<?>) cachedValue;
		} else {
			value = NSValueUtilities.arrayValueWithDefault(System.getProperty(s), null);
			_cache.put(s, value == null ? (Object)UndefinedMarker : value);
			if (value == null) {
				value = defaultValue;
			}
		}
		return value;
    }
    
    /**
     * Cover method for returning a boolean for a
     * given system property.
     * @param s system property
     * @return boolean value of the string in the
     *      system properties.
     */    
    public static boolean booleanForKey(String s) {
        return booleanForKeyWithDefault(s, false);
    }

    /**
     * Cover method for returning a boolean for a
     * given system property or a default value. 
     * @param s system property
     * @param defaultValue default value
     * @return boolean value of the string in the
     *      system properties.
     */
    public static boolean booleanForKeyWithDefault(final String s, final boolean defaultValue) {
        boolean value;
		Object cachedValue = _cache.get(s);
		if (cachedValue == UndefinedMarker) {
			value = defaultValue;
		} else if (cachedValue instanceof Boolean) {
			value = ((Boolean) cachedValue).booleanValue();
		} else {
			Boolean objValue = NSValueUtilities.BooleanValueWithDefault(System.getProperty(s), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.booleanValue();
			}
		}
		return value;
    }
    
    /**
     * Cover method for returning an NSDictionary for a
     * given system property.
     * @param s system property
     * @return dictionary de-serialized from the string in
     *      the system properties
     */    
    public static NSDictionary<?, ?> dictionaryForKey(String s) {
        return dictionaryForKeyWithDefault(s, null);
    }

    /**
     * Cover method for returning an NSDictionary for a
     * given system property or the default value.
     * @param s system property
     * @param defaultValue default value
     * @return dictionary de-serialized from the string in
     *      the system properties
     */
    public static NSDictionary<?, ?> dictionaryForKeyWithDefault(final String s, final NSDictionary<? ,?> defaultValue) {
		NSDictionary<?, ?> value;
		Object cachedValue = _cache.get(s);
		if (cachedValue == UndefinedMarker) {
			value = defaultValue;
		} else if (cachedValue instanceof NSDictionary) {
			value = (NSDictionary<?, ?>) cachedValue;
		} else {
			value = NSValueUtilities.dictionaryValueWithDefault(System.getProperty(s), null);
			_cache.put(s, value == null ? (Object)UndefinedMarker : value);
			if (value == null) {
				value = defaultValue;
			}
		}
		return value;
    }

    /**
     * Cover method for returning an int for a
     * given system property.
     * @param s system property
     * @return int value of the system property or 0
     */
    public static int intForKey(String s) {
        return intForKeyWithDefault(s, 0);
    }

    /**
     * Cover method for returning a long for a
     * given system property.
     * @param s system property
     * @return long value of the system property or 0
     */
    public static long longForKey(String s) {
        return longForKeyWithDefault(s, 0);
    }

    /**
     * Cover method for returning a float for a
     * given system property.
     * @param s system property
     * @return float value of the system property or 0
     */
    public static float floatForKey(String s) {
        return floatForKeyWithDefault(s, 0);
    }

    /**
     * Cover method for returning a double for a
     * given system property.
     * @param s system property
     * @return double value of the system property or 0
     */
    public static double doubleForKey(String s) {
        return doubleForKeyWithDefault(s, 0);
    }

    /**
     * Cover method for returning a BigDecimal for a
     * given system property. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link NSValueUtilities}.
     * @param s system property
     * @return bigDecimal value of the string in the
     *      system properties.  Scale is controlled by the string, ie "4.400" will have a scale of 3.
     */
    public static BigDecimal bigDecimalForKey(String s) {
        return bigDecimalForKeyWithDefault(s,null);
    }

    /**
     * Cover method for returning a BigDecimal for a
     * given system property or a default value. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link NSValueUtilities}.
     * @param s system property
     * @param defaultValue default value
     * @return BigDecimal value of the string in the
     *      system properties. Scale is controlled by the string, ie "4.400" will have a scale of 3.
     */
    public static BigDecimal bigDecimalForKeyWithDefault(String s, BigDecimal defaultValue) {
        Object value = _cache.get(s);
        if (value == UndefinedMarker) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal)value;
        }
        
        String propertyValue = System.getProperty(s);
        final BigDecimal bigDecimal = NSValueUtilities.bigDecimalValueWithDefault(propertyValue, defaultValue);
        _cache.put(s, propertyValue == null ? (Object)UndefinedMarker : bigDecimal);
        return bigDecimal;
    }

    /**
     * Cover method for returning an int for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return int value of the system property or the default value
     */    
    public static int intForKeyWithDefault(final String s, final int defaultValue) {
		int value;
		Object cachedValue = _cache.get(s);
		if (cachedValue == UndefinedMarker) {
			value = defaultValue;
		} else if (cachedValue instanceof Integer) {
			value = ((Integer) cachedValue).intValue();
		} else {
			Integer objValue = NSValueUtilities.IntegerValueWithDefault(System.getProperty(s), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.intValue();
			}
		}
		return value;
    }

    /**
     * Cover method for returning a long for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return long value of the system property or the default value
     */    
    public static long longForKeyWithDefault(final String s, final long defaultValue) {
		long value;
		Object cachedValue = _cache.get(s);
		if (cachedValue == UndefinedMarker) {
			value = defaultValue;
		} else if (cachedValue instanceof Long) {
			value = ((Long) cachedValue).longValue();
		} else {
			Long objValue = NSValueUtilities.LongValueWithDefault(System.getProperty(s), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.longValue();
			}
		}
		return value;
    }

    /**
     * Cover method for returning a float for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return float value of the system property or the default value
     */    
    public static float floatForKeyWithDefault(final String s, final float defaultValue) {
		float value;
		Object cachedValue = _cache.get(s);
		if (cachedValue == UndefinedMarker) {
			value = defaultValue;
		} else if (cachedValue instanceof Float) {
			value = ((Float) cachedValue).floatValue();
		} else {
			Float objValue = NSValueUtilities.FloatValueWithDefault(System.getProperty(s), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.floatValue();
			}
		}
		return value;
    }

    /**
     * Cover method for returning a double for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return double value of the system property or the default value
     */    
    public static double doubleForKeyWithDefault(final String s, final double defaultValue) {
		double value;
		Object cachedValue = _cache.get(s);
		if (cachedValue == UndefinedMarker) {
			value = defaultValue;
		} else if (cachedValue instanceof Double) {
			value = ((Double) cachedValue).doubleValue();
		} else {
			Double objValue = NSValueUtilities.DoubleValueWithDefault(System.getProperty(s), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.doubleValue();
			}
		}
		return value;
    }
    
    /**
     * Returning an string for a given system 
     * property. This is a cover method of 
     * {@link java.lang.System#getProperty}
     * @param s system property
     * @return string value of the system propery or null
     */
    public static String stringForKey(String s) {
        return stringForKeyWithDefault(s, null);
    }

    /**
     * Returning an string for a given system
     * property. This is a cover method of
     * {@link java.lang.System#getProperty}
     * @param s system property
     * @return string value of the system propery or null
     */
    public static String stringForKeyWithDefault(final String s, final String defaultValue) {
        final String propertyValue = System.getProperty(s);
        final String stringValue = propertyValue == null ? defaultValue : propertyValue;
        return stringValue == UndefinedMarker ? null : stringValue;
    }

    /**
     * Returns an array of strings separated with the given separator string.
     * 
     * @param key the key to lookup
     * @param separator the separator (",")
     * @return the array of strings or NSArray.EmptyArray if not found
     */
    @SuppressWarnings("unchecked")
    public static NSArray<String> componentsSeparatedByString(String key, String separator) {
    	return NSProperties.componentsSeparatedByStringWithDefault(key, separator, (NSArray<String>)NSArray.EmptyArray);
    }

    /**
     * Returns an array of strings separated with the given separator string.
     * 
     * @param key the key to lookup
     * @param separator the separator (",")
     * @param defaultValue the default array to return if there is no value
     * @return the array of strings
     */
	public static NSArray<String> componentsSeparatedByStringWithDefault(String key, String separator, NSArray<String> defaultValue) {
    	NSArray<String> array;
    	String str = stringForKeyWithDefault(key, null);
    	if (str == null) {
    		array = defaultValue;
    	}
    	else {
    		array = (NSArray<String>)NSArray.componentsSeparatedByString(str, separator);
    	}
    	return array;
    }
    
    /**
     * Sets an array in the System properties for
     * a particular key.
     * @param array to be set in the System properties
     * @param key to be used to get the value
     */
    public static void setArrayForKey(NSArray<?> array, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(array), key);
    }

    /**
     * Sets a dictionary in the System properties for
     * a particular key.
     * @param dictionary to be set in the System properties
     * @param key to be used to get the value
     */    
    public static void setDictionaryForKey(NSDictionary<?, ?> dictionary, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(dictionary), key);
    }

    /**
     * Sets a string in the System properties for
     * another string.
     * @param string to be set in the System properties
     * @param key to be used to get the value
     */
    // DELETEME: Really not needed anymore -- MS: Why?  We need the cache clearing.
    public static void setStringForKey(String string, String key) {
        System.setProperty(key, string);
        _cache.remove(key);
    }
    
    public static void removeKey(String key) {
    	System.getProperties().remove(key);
    	_cache.remove(key);
    }
    
    /** 
     * Copies all properties from source to dest. 
     * 
     * @param source  properties copied from 
     * @param dest  properties copied to
     */
    public static void transferPropertiesFromSourceToDest(Properties source, Properties dest) {
        if (source != null) {
            dest.putAll(source);
            if (dest == System.getProperties()) {
                systemPropertiesChanged();
            }
        }
    }
    

    
    /**
     * Reads a Java properties file at the given path 
     * and returns a {@link java.util.Properties Properties} object 
     * as the result. If the file does not exist, returns 
     * an empty properties object. 
     * 
     * @param path  file path to the properties file
     * @return properties object with the values from the file
     *      specified.
     */
    // FIXME: This shouldn't eat the exception
    public static Properties propertiesFromPath(String path) {
    	NSProperties._Properties prop = new NSProperties._Properties();

        if (path == null  ||  path.length() == 0) {
            log.warn("Attempting to read property file for null file path");
            return prop;
        }

        File file = new File(path);
        if (! file.exists()  ||  ! file.isFile()  ||  ! file.canRead()) {
            log.warn("File " + path + " doesn't exist or can't be read.");
            return prop;
        }

        try {
        	prop.load(file);
            log.debug("Loaded configuration file at path: "+ path);
        } catch (IOException e) {
            log.error("Unable to initialize properties from file \"" + path + "\"", e);
        }
        return prop;
    }

    /**
     * Gets the properties for a given file.
     * 
     * @param file the properties file
     * @return properties from the given file
     * @throws java.io.IOException if the file is not found or cannot be read
     */
    public static Properties propertiesFromFile(File file) throws java.io.IOException {
        if (file == null)
            throw new IllegalStateException("Attempting to get properties for a null file!");
        NSProperties._Properties prop = new NSProperties._Properties();
        prop.load(file);
        return prop;
    }


    /**
     * Apply the current configuration to the supplied properties.
     * @param source
     * @param commandLine
     */
    public static Properties applyConfiguration(Properties source, Properties commandLine) {

    	Properties dest = source != null ? (Properties) source.clone() : new Properties();
    	if(commandLine != null) {
    		NSProperties.transferPropertiesFromSourceToDest(commandLine, dest);
    	}
		return dest;
    	
    }

    /**
     * Returns all of the properties in the system mapped to their evaluated values, sorted by key.
     * 
     * @param protectValues if true, keys with the word "password" in them will have their values removed 
     * @return all of the properties in the system mapped to their evaluated values, sorted by key
     */
    public static Map<String, String> allPropertiesMap(boolean protectValues) {
    	return propertiesMap(System.getProperties(), protectValues);
    }

    /**
     * Returns all of the properties in the system mapped to their evaluated values, sorted by key.
     * 
     * @param protectValues if true, keys with the word "password" in them will have their values removed 
     * @return all of the properties in the system mapped to their evaluated values, sorted by key
     */
    public static Map<String, String> propertiesMap(Properties properties, boolean protectValues) {
    	Map<String, String> props = new TreeMap<String, String>();
    	for (Enumeration<Object> e = properties.keys(); e.hasMoreElements();) {
    		String key = (String) e.nextElement();
    		if (protectValues && key.toLowerCase().contains("password")) {
    			props.put(key, "<deleted for log>");
    		}
    		else {
    			props.put(key, String.valueOf(properties.getProperty(key)));
    		}
    	}
    	return props;
    }
    
    /**
     * Returns a string suitable for logging.
     * @param properties
     */
    public static String logString(Properties properties) {
    	StringBuffer message = new StringBuffer();
        for (Map.Entry<String, String> entry : propertiesMap(properties, true).entrySet()) {
        	message.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
        }
        return message.toString();
    }
    
    public static class Property {
    	public String key, value;
    	public Property(String key, String value) {
    		this.key = key;
    		this.value = value;
    	}
    	public String toString() {
    		return key + " = " + value;
    	}
    }

    @SuppressWarnings("unchecked")
    public static NSArray<Property> allProperties() {
    	NSMutableArray<Property> props = new NSMutableArray<Property>();
    	for (Enumeration<Object> e = System.getProperties().keys(); e.hasMoreElements();) {
    		String key = (String) e.nextElement();
    		String object = "" + System.getProperty(key);
    		props.addObject(new Property(key, object));
    	}
    	return (NSArray<Property>) props.valueForKey("@sortAsc.key");
     }

    /**
     * Returns actual full path to the given file system path  
     * that could contain symbolic links. For example: 
     * /Resources will be converted to /Versions/A/Resources
     * when /Resources is a symbolic link.
     * 
     * @param path  path string to a resource that could 
     *               contain symbolic links
     * @return actual path to the resource
     */
    public static String getActualPath(String path) {
        String actualPath = null;
        File file = new File(path);
        try {
            actualPath = file.getCanonicalPath();
        } catch (Exception ex) {
            log.warn("The file at " + path + " does not seem to exist: " 
                + ex.getClass().getName() + ": " + ex.getMessage());
        }
        return actualPath;
    }
    
    public static void systemPropertiesChanged() {
        _cache.clear();
        NSNotificationCenter.defaultCenter().postNotification("PropertiesDidChange", null, null);
    }

    //	===========================================================================
    //	Instance Variable(s)
    //	---------------------------------------------------------------------------

    /** caches the application name that is appended to the key for lookup */
    protected String applicationNameForAppending;

    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------

    /**
     * Overriding the default getProperty method to first check:
     * key.&lt;ApplicationName> before checking for key. If nothing
     * is found then key.Default is checked.
     * @param key to check
     * @return property value
     */
    public String getProperty(String key) {
        String property = null;
        if (property == null) {
            property = super.getProperty(key);
            if (property == null) {
                property = super.getProperty(key + DefaultString);
            }
        }
        return property;
    }

    /**
     * Returns the properties as a String in Property file format. Useful when you use them 
     * as custom value types, you would set this as the conversion method name.
     * @throws IOException
     */
    public Object toExternalForm() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        store(os, null);
        return new String(os.toByteArray());
    }
    
    /**
     * Load the properties from a String in Property file format. Useful when you use them 
     * as custom value types, you would set this as the factory method name.
     * @param string
     */
    public static NSProperties fromExternalForm(String string) {
        NSProperties result = new NSProperties();
        try {
			result.load(new ByteArrayInputStream(string.getBytes()));
		}
		catch (IOException e) {
			// AK: shouldn't ever happen...
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
        return result;
    }

    /**
     * KVC implementation.
     * @param anObject
     * @param aKey
     */
    public void takeValueForKey(Object anObject, String aKey) {
         setProperty(aKey, (anObject != null ? anObject.toString() : null));
    }

    /**
     * KVC implementation.
     *
     * @param aKey
     */
    public Object valueForKey(String aKey) {
         return getProperty(aKey);
    }

	/**
	 * Stores the mapping between operator keys and operators
	 */
	private static final NSMutableDictionary<String, NSProperties.Operator> operators = new NSMutableDictionary<String, NSProperties.Operator>();

	/**
	 * Registers a property operator for a particular key.
	 * 
	 * @param operator
	 *            the operator to register
	 * @param key
	 *            the key name of the operator
	 */
	public static void setOperatorForKey(NSProperties.Operator operator, String key) {
		NSProperties.operators.setObjectForKey(operator, key);
	}

	/**
	 * <p>
	 * Property operators work like array operators. In your properties, you can
	 * define keys like:
	 * </p>
	 * 
	 * <code>
	 * er.extensions.akey.@someOperatorKey.aparameter=somevalue
	 * </code>
	 * 
	 * <p>
	 * Which will be processed by the someOperatorKey operator. Because
	 * properties get handled very early in the startup process, you should
	 * register operators somewhere like a static block in your Application
	 * class. For instance, if you wanted to register the forInstance operator,
	 * you might put the following your Application class:
	 * </p>
	 * 
	 * <code>
	 * static {
	 *   ERXProperties.setOperatorForKey(new ERXProperties.InRangeOperator(100), ERXProperties.InRangeOperator.ForInstanceKey);
	 * }
	 * </code>
	 * 
	 * <p>
	 * It's important to note that property operators evaluate at load time, not
	 * access time, so the compute function should not depend on any runtime
	 * state to execute. Additionally, access to other properties inside the
	 * compute method should be very carefully considered because it's possible
	 * that the operators are evaluated before all of the properties in the
	 * system are loaded.
	 * </p>
	 * 
	 * @author mschrag
	 */
	public static interface Operator {
		/**
		 * Performs some computation on the key, value, and parameters and
		 * returns a dictionary of new properties. If this method returns null,
		 * the original key and value will be used. If any other dictionary is
		 * returned, the properties in the dictionary will be copied into the
		 * destination properties.
		 * 
		 * @param key
		 *            the key ("er.extensions.akey" in
		 *            "er.extensions.akey.@someOperatorKey.aparameter=somevalue")
		 * @param value
		 *            ("somevalue" in
		 *            "er.extensions.akey.@someOperatorKey.aparameter=somevalue")
		 * @param parameters
		 *            ("aparameter" in
		 *            "er.extensions.akey.@someOperatorKey.aparameter=somevalue")
		 * @return a dictionary of properties (or null to use the original key
		 *         and value)
		 */
		public NSDictionary<String, String> compute(String key, String value, String parameters);
	}

	/**
	 * For each property in originalProperties, process the keys and values with
	 * the registered property operators and stores the converted value into
	 * destinationProperties.
	 * 
	 * @param originalProperties
	 *            the properties to convert
	 * @param destinationProperties
	 *            the properties to copy into
	 */
	public static void evaluatePropertyOperators(Properties originalProperties, Properties destinationProperties) {
		NSArray<String> operatorKeys = NSProperties.operators.allKeys();
		for (Object keyObj : new TreeSet<Object>(originalProperties.keySet())) {
			String key = (String) keyObj;
			if (key != null && key.length() > 0) {
				String value = originalProperties.getProperty(key);
				if (operatorKeys.count() > 0 && key.indexOf(".@") != -1) {
					NSProperties.Operator operator = null;
					NSDictionary<String, String> computedProperties = null;
					for (String operatorKey : operatorKeys) {
						String operatorKeyWithAt = ".@" + operatorKey;
						if (key.endsWith(operatorKeyWithAt)) {
							operator = NSProperties.operators.objectForKey(operatorKey);
							computedProperties = operator.compute(key.substring(0, key.length() - operatorKeyWithAt.length()), value, null);
							break;
						}
						else {
							int keyIndex = key.indexOf(operatorKeyWithAt + ".");
							if (keyIndex != -1) {
								operator = NSProperties.operators.objectForKey(operatorKey);
								computedProperties = operator.compute(key.substring(0, keyIndex), value, key.substring(keyIndex + operatorKeyWithAt.length() + 1));
								break;
							}
						}
					}

					if (computedProperties == null) {
						destinationProperties.put(key, value);
					}
					else {
						originalProperties.remove(key);
						
						// If the key exists in the System properties' defaults with a different value, we must reinsert
						// the property so it doesn't get overwritten with the default value when we evaluate again.
						// This happens because ERXConfigurationManager processes the properties after a configuration
						// change in multiple passes and each calls this method.
						if (System.getProperty(key) != null && !System.getProperty(key).equals(value)) {
							originalProperties.put(key, value);
						}
						
						for (String computedKey : computedProperties.allKeys()) {
							destinationProperties.put(computedKey, computedProperties.objectForKey(computedKey));
						}
					}
				}
				else {
					destinationProperties.put(key, value);
				}
			}
		}
	}

	/**
	 * _Properties is a subclass of Properties that provides support for including other
	 * Properties files on the fly.  If you create a property named .includeProps, the value
	 * will be interpreted as a file to load.  If the path is absolute, it will just load it
	 * directly.  If it's relative, the path will be loaded relative to the current user's
	 * home directory.  Multiple .includeProps can be included in a Properties file and they
	 * will be loaded in the order they appear within the file.
	 *  
	 * @author mschrag
	 */
	public static class _Properties extends Properties {
        private static final long serialVersionUID = 6039896009430193423L;

        public static final String IncludePropsKey = ".includeProps";
		
		private Stack<File> _files = new Stack<File>();
		
		@Override
		public synchronized Object put(Object key, Object value) {
			if (_Properties.IncludePropsKey.equals(key)) {
				String propsFileName = (String)value;
                File propsFile = new File(propsFileName);
                if (!propsFile.isAbsolute()) {
                    // if we don't have any context for a relative (non-absolute) props file,
                    // we presume that it's relative to the user's home directory
    				File cwd = null;
    				if (_files.size() > 0) {
    					cwd = _files.peek();
    				}
    				else {
    					cwd = new File(System.getProperty("user.home"));
                	}
                    propsFile = new File(cwd, propsFileName);
                }

                // Detect mutually recursing props files by tracking what we've already loaded:
                String existingIncludeProps = this.getProperty(_Properties.IncludePropsKey);
                if (existingIncludeProps == null) {
                	existingIncludeProps = "";
                }
                if (existingIncludeProps.indexOf(propsFile.getPath()) > -1) {
                    log.error("_Properties.load(): recursive includeProps detected! " + propsFile + " in " + existingIncludeProps);
                    log.error("_Properties.load() cannot proceed - QUITTING!");
                    System.exit(1);
                }
                if (existingIncludeProps.length() > 0) {
                	existingIncludeProps += ", ";
                }
                existingIncludeProps += propsFile;
                super.put(_Properties.IncludePropsKey, existingIncludeProps);

                try {
                    log.info("_Properties.load(): Including props file: " + propsFile);
					this.load(propsFile);
				} catch (IOException e) {
					throw new RuntimeException("Failed to load the property file '" + value + "'.", e);
				}
				return null;
			}
			else {
				return super.put(key, value);
			}
		}

		public synchronized void load(File propsFile) throws IOException {
			_files.push(propsFile.getParentFile());
			try {
	            BufferedInputStream is = new BufferedInputStream(new FileInputStream(propsFile));
	            try {
	            	load(is);
	            }
	            finally {
	            	is.close();
	            }
			}
			finally {
				_files.pop();
			}
		}
	}

	public static void setCommandLineArguments(String[] argv) {
		
	}
}
