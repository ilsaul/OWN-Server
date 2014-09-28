OWNPlugin-Power
================
This plugin is for save and Analyze power consumption.

INSTALL
-------

For install Power plugin need to insert it in the configuration section of plugins

```
<plugin>
  <plugin id="1">org.programmatori.domotica.own.plugin.power.PowerMeter</plugin>
<plugin>
```

the attribute id of the tag plugin is only a counter, then if you have an other plugin then you set it to 2 and so on.

You must add a section similar to this where ever you want in configuration file for configurate the plugin
```
<power>
	<intervall unit="sec">10</intervall>
</power>
```
