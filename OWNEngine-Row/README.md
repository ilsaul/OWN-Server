OWN Engine SCSGate
==========

This is Engine replacement of Emulator for the OWN Server. This Engine can really connect to the real Bus SCS.
This engine can use only if you have SCSGate from GuidoPic. You don't ask me about sell or give information how 
to retrieve the Hardware. 

Connect SCSGate to Arduino base on the GuidoPic suggestion. You can find the software I use in Arduino in this module.
Connect Arduino to a serial of the PC. 

INSTALL
-------

You must add this line where ever you want in the server configuration file set which bus need to use.
```
<bus>org.programmatori.domotica.own.engine.scsgate.SCSGate</bus>
```

You need to provide information to the serial where Arduino is connected.

```
<scsgate>COM7</scsgate>
```