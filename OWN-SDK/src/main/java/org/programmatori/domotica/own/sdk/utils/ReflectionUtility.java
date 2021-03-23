package org.programmatori.domotica.own.sdk.utils;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

public class ReflectionUtility {
	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtility.class);

	private ReflectionUtility() {
		// stubb
	}

	@Nullable
	private static Class<?> getClass(String fullyQualifiedName) {

		try {
			return ClassLoader.getSystemClassLoader().loadClass(fullyQualifiedName);

		} catch (ClassNotFoundException e) {
			logger.error("Class not found", e);
			return null;
		}
	}

	@Nullable
	private static <T> T instantiateObject(Constructor<T> constructor) {
		try {
			return constructor.newInstance();

		} catch (ReflectiveOperationException e) {
			logger.error("Instantiate Error", e);
			return null;
		}
	}

	public static <T> T createClass(String fullyQualifiedName) {

		Class<?> c = getClass(fullyQualifiedName);
		if (c == null) return null;

		Constructor<T> constructor;
		try {
			constructor = (Constructor<T>) c.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			logger.error("Constructor not found", e);
			return null;
		}

		return instantiateObject(constructor);
	}

	public static <T> T createClass(String fullyQualifiedName, Object param) {

		Class<?> c = getClass(fullyQualifiedName);
		if (c == null) return null;

		Constructor<T> constructor;
		try {
			//@SuppressWarnings("unchecked")
			constructor = (Constructor<T>) c.getDeclaredConstructor(param.getClass());
		} catch (NoSuchMethodException e) {
			logger.error("Constructor not found", e);
			return null;
		}

		return instantiateObject(constructor);
	}
}
