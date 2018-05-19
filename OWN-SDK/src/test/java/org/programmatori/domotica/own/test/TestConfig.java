package org.programmatori.domotica.own.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.programmatori.domotica.own.sdk.config.AbstractConfig;
import org.programmatori.domotica.own.sdk.config.Config;

public class TestConfig {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetHomeDirectory() {
		String path = AbstractConfig.getHomeDirectory();

		ClassLoader loader = Test.class.getClassLoader();
		String classDot = TestConfig.class.getCanonicalName();
		String className = classDot.replaceAll("\\.", File.separator) + ".class";
		File actualPath = new File(loader.getResource(className).getPath()); // "org/programmatori/domotica/own/test/TestConfig.class").getPath());
		actualPath = actualPath.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();

		// Remove bin for Eclipse
		if (actualPath.getName().toString().equals("bin")) {
			actualPath = actualPath.getParentFile();
		}
		// Remove gradle path (/build/classes/test)
		if (actualPath.getName().toString().equals("test")
				&& actualPath.getParentFile().getName().toString().equals("java")
				&& actualPath.getParentFile().getParentFile().getName().toString().equals("classes")
				&& actualPath.getParentFile().getParentFile().getParentFile().getName().toString().equals("build")) {
			actualPath = actualPath.getParentFile().getParentFile().getParentFile().getParentFile();
		}

		// On linux the space is %20 and need to be replace
		String acPath = actualPath.getAbsolutePath();
		acPath = acPath.replaceAll("%20", " ");

		assertEquals( "Wrong Path", acPath, path);
	}

	@Test
	public void testStartConfig() {
		Config config = Config.getInstance();

		assertNotNull("Deve esistere la configurazione", config);
	}

}
