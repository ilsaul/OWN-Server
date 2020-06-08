package org.programmatori.domotica.own.sdk.utils;

import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtility {
	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtility.class);

	private ReflectionUtility() {
		// stubb
	}

	public static <T> T createClass(String fullyQualifiedName) {

		Class<?> c;
		try {
			c = ClassLoader.getSystemClassLoader().loadClass(fullyQualifiedName);
		} catch (ClassNotFoundException e) {
			logger.error("Class not found", e);
			return null;
		}

		Constructor<T> constructor;
		try {
			constructor = (Constructor<T>) c.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			logger.error("Constructor not found", e);
			return null;
		}

		T newObject;
		try {
			newObject = constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			logger.error("Instantiate Error", e);
			return null;
		}

		return newObject;
	}

	public static <T> T createClass(String fullyQualifiedName, Object param) {
		Class<?> c;
		try {
			c = ClassLoader.getSystemClassLoader().loadClass(fullyQualifiedName);
		} catch (ClassNotFoundException e) {
			logger.error("Class not found", e);
			return null;
		}

		Constructor<T> constructor;
		try {
			//@SuppressWarnings("unchecked")
			constructor = (Constructor<T>) c.getDeclaredConstructor(param.getClass());
		} catch (NoSuchMethodException e) {
			logger.error("Constructor not found", e);
			return null;
		}

		T newObject;
		try {
			newObject = constructor.newInstance(param);
		} catch (ReflectiveOperationException e) {
			logger.error("Instantiate Error", e);
			return null;
		}

		return newObject;
	}
}
