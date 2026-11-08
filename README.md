# OWN Server

OWN Server is an extensible Java server for applications that communicate with
BTicino/Legrand MyHome devices over an SCS bus. It exposes the bus to client
applications and provides a modular runtime where bus implementations
(`Engine`) and additional features (`Plugin`) can be connected without
changing the core server.

The repository is a multi-project Gradle build. **`OWNServer` is the main
project**: it contains the executable server, while the other projects provide
the SDK, bus engines and plugins used by it.

## Architecture

```text
Client applications
        |
        v
    OWNServer  <---- OWNPlugin-*  (optional features)
        |
        +-------- OWNEngine-*    (one selected bus implementation)
        |
        +-------- OWN-SDK        (API shared by engines and plugins)
```

At runtime, the server reads its XML configuration, selects one bus engine and
loads the configured plugins. The same server can therefore be used with a
real SCS interface or with the emulator used during development and testing.

## Modules

| Module | Purpose |
| --- | --- |
| [`OWNServer`](OWNServer) | Main executable server, TCP client connections and runtime configuration. |
| [`OWN-SDK`](OWN-SDK) | Public API for implementing engines and plugins. |
| [`OWNEngine-Emulator`](OWNEngine-Emulator) | Virtual SCS bus for local development and tests. |
| [`OWNEngine-L4686Sdk`](OWNEngine-L4686Sdk) | Engine for the L4686SDK connected through USB/serial. |
| [`OWNEngine-SCSGate`](OWNEngine-SCSGate) | Engine for an SCSGate/Arduino serial bridge. |
| [`OWNPlugin-System`](OWNPlugin-System) | Standard system information and version responses. |
| [`OWNPlugin-Map`](OWNPlugin-Map) | Builds a map of the installation by querying the bus. |
| [`OWNPlugin-Power`](OWNPlugin-Power) | Collects and analyses power-consumption data. |
| [`OWNPlugin-Remote`](OWNPlugin-Remote) | Transfers state files to and from a remote FTP server. |
| [`OWNPlugin-HomeKit`](OWNPlugin-HomeKit) | HomeKit integration module. |

Each module has its own `README.md` when additional module-specific setup is
required.

## Requirements

- Java 8 or newer compatible with the project's Java 8 source level
- Gradle (the build uses the Gradle wrapper when one is available in the
  checkout)
- A supported SCS interface for a real installation; no hardware is required
  when using the emulator

## Build

From the repository root:

```bash
./gradlew build
```

On checkouts without the Gradle wrapper, use the installed `gradle` command:

```bash
gradle build
```

Useful tasks:

```bash
gradle eclipse   # Generate Eclipse project metadata
gradle distZip   # Create the OWNServer distribution archive
gradle test      # Run the test suites
```

The distribution contains the OWN Server executable together with the runtime
engines and plugins declared by `OWNServer`.

## Configuration

The server configuration is XML-based. At minimum, configure the TCP server
and select one engine. The following example uses the emulator:

```xml
<server>
  <port>20000</port>
  <maxConnections>50</maxConnections>
  <timeoutWelcome>30000</timeoutWelcome>
  <timeoutSend>4000</timeoutSend>
</server>

<bus>org.programmatori.domotica.own.engine.emulator.Emulator</bus>
```

Replace the `<bus>` value to use a real interface:

```xml
<!-- L4686SDK -->
<bus>org.programmatori.domotica.own.engine.l4686sdk.L4686Sdk</bus>
<l4686sdk>COM7</l4686sdk>

<!-- SCSGate -->
<bus>org.programmatori.domotica.own.engine.scsgate.SCSGate</bus>
<scsgate>COM7</scsgate>
```

Plugins are enabled by adding their fully qualified class name to the
configuration. For example:

```xml
<plugin>
  <plugin id="1">org.programmatori.domotica.own.plugin.system.System</plugin>
</plugin>
```

See the README in each plugin directory for its additional configuration
section and available options.

## Running

After building the project, create the distribution archive and unpack it:

```bash
gradle distZip
unzip OWNServer/build/distributions/OWNServer-*.zip
cd OWNServer-*/
bin/OWNServer
```

For development, the server can also be started through Gradle:

```bash
gradle :OWNServer:run
```

## Developing extensions

Use [`OWN-SDK`](OWN-SDK) when creating a new integration:

- implement `org.programmatori.domotica.own.sdk.server.engine.core.Engine`
  for a new bus implementation;
- implement
  `org.programmatori.domotica.own.sdk.server.engine.PlugIn` for a server
  extension.

Add the new module to `settings.gradle` and to the `OWNServer` runtime
dependencies, then register the engine or plugin class in the XML
configuration.

## License

OWN Server is distributed under the
[GNU Lesser General Public License](https://www.gnu.org/licenses/lgpl-3.0.html).

[![Build Status](https://travis-ci.org/ilsaul/OWN-Server.svg?branch=master)](https://travis-ci.org/ilsaul/OWN-Server)
![License](https://badgen.net/badge/license/LGPL/blue)
