OWNServer
==========

This java application is a Server for receive connection from clients that want to
send and/or receive information from a SCS Bus. The SCS Bus is a Bus make by BTicino where is connected home automatin equipement like light and roller shutter.

INSTALL
-------
Create a folder conf with inside the file config.xml. The first tag must be configuration. The whole configuration goes into the configuration tag. Insert the following configuration:

&lt;server&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;port&gt;20000&lt;/port&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;maxConnections&gt;50&lt;/maxConnections&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;timeoutWelcome&gt;30000&lt;/timeoutWelcome&gt; &lt;!-- Milliseconds --&gt;<br>
&nbsp;&nbsp;&nbsp;&nbsp;&lt;timeoutSend&gt;4000&lt;/timeoutSend&gt; &lt;!-- Milliseconds --&gt;<br>
&lt;/server&gt;

Insert the following line for the default engine:
```
<bus>org.programmatori.domotica.own.engine.emulator.Emulator</bus>
```
for other engines search OWNEngine prefix in project name.
