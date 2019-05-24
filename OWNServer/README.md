OWNServer
==========

This java application is a Server for receive connection from clients that want to
send and/or receive information from a SCS Bus. The SCS Bus is a Bus make by BTicino where is connected home automatin equipement like light and roller shutter.

INSTALL
-------

You must add these lines where ever you want in the server configuration file:

<server>
		<port>20000</port>
		<maxConnections>50</maxConnections>
		<timeoutWelcome>30000</timeoutWelcome> <!-- Milliseconds -->
		<timeoutSend>4000</timeoutSend> <!-- Milliseconds -->
	</server>

```
<bus>org.programmatori.domotica.own.server.engine.l4686sdk.L4686Sdk</bus>
```


I promise to finish this document us fast as I can.
