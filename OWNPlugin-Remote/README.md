OWNPlugin-Remote
================
This plugin is for transfer to a web server throw ftp a file and take back a file from the server.

INSTALL
-------

For install System plugin need to insert in the configuration in the plugin tag

`<plugin>`
`  <plugin id="1">org.programmatori.domotica.own.plugin.remote.FTPRemote</plugin>`
`<plugin>`

the id of the tag plugin is only a counter, then if u have an other plugin that plugin is 2.

You must add this section where ever you want in configuration file for configurate the plugin
`<remoter>`
`  <protocol>ftp</protocol>`
`  <host>ilsaul.2web.net</host>`
`  <remoteFile>/homeState.xml</remoteFile>`
`  <user>user</user>`
`  <password>password</password>`
`  `
`  <intervall unit="min">10</intervall>`
`  <localFile>conf/homeState.xml</localFile>`
`</remoter>`