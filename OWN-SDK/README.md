OWN-SDK
=======

This is the API library. If you want to develop something like a plugin for the server you must include the SDK.

### PlugIn
PlugIn is a piece of the Server and implement some functionallity of it. PlugIn receive all messages that arrive on the bus and can send message too.
For create a PlugIn you must implement the interface org.programmatori.domotica.own.sdk.server.engine.PlugIn.

### Engine
Engine Is use for manage a Bus. It can else be for different bus, the important is the dialog that have with the Server.
For create a Engine you must implement the interface org.programmatori.domotica.own.sdk.server.engine.core.Engine.

