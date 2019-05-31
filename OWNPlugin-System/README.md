OWNPlugin-System
================
This plugin is not really necessary for the server, but it complete the standard functionality.
This plugin replace the BTicino server answer like date, version, etc.

INSTALL
-------

For install System plugin you need to insert, in the server configuration, the plugin tag

```
<plugin>
  <plugin id="1">org.programmatori.domotica.own.plugin.system.System</plugin>
<plugin>
```

the id of the tag plugin is only a counter, then if you have an other plugin then you set id = 2 and so on.

You must add a section similar to this where ever you want in configuration file for configurate the plugin
```
<system>
	<!-- BTicino know model value:
	2) MHServer
	4) MH2000
	6) F452
	7) F452V
	11) MHServer2
	13) H4684

	99) OWNServer
	-->
	<model>99</model>

	<kernel>0.0.0</kernel>
	<firmware>0.0.0</firmware>
	<version>0.0.0</version>
</system>
```

If you set 99 in the `model` the tag `version` is not read by the server because it is used the software version.
