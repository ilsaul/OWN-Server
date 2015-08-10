OWNPlugin-Map
================
The plugin creates a map of the building by querying the bus

INSTALL
-------

For install Map plugin you need to insert, in the server configuration, the plugin tag

```
<plugin>
  <plugin id="1">org.programmatori.domotica.own.plugin.map.Map</plugin>
<plugin>
```

the id of the tag plugin is only a counter, then if you have an other plugin then you set id = 2 and so on.

You must add a section similar to this where ever you want in configuration file for configurate the plugin
```
<map>
	<path>conf</path>
	<pause>
		<start>5000</start>
		<unit>5000</unit>
	</pause>
	<intervall unit="min">10</intervall>
	<file version="2.0">homeState.xml</file>
</map>
<areas>
	<area id="1">Bathroom</area>
	<area id="2">Living Room</area>
	<area id="3">Main Bedroom</area>
</areas>
```
