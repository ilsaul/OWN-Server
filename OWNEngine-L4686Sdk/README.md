OWN Engine L4686SDK
===================

This is Engine replacement of Emulator for the OWN Server. This Engine can really connect to the real Bus SCS.

INSTALL
-------

You must add this line where ever you want in the server configuration file set which bus need to use.

```
<bus>org.programmatori.domotica.own.engine.l4686sdk.L4686Sdk</bus>
```

You need to provide the USB or COM port where is conected the component L4686SDK.

```
<l4686sdk>COM7</l4686sdk>
```