package org.programmatori.domotica.own.plugin.map;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;

public class MapTest {

	@Ignore
	@Test
	public void testMap() {
		Map map = new Map(null);

		map.run();

		//try {
			//map.createStatusFile("test.xml");
		//} catch (TransformerConfigurationException e) {
			fail("it must not throw exception");
		//}

	}

}
