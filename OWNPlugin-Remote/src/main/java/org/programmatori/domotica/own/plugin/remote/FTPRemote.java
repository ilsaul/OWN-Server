/*
 * Copyright (C) 2010-2016 Moreno Cattaneo <moreno.cattaneo@gmail.com>
 *
 * This file is part of OWN Server.
 *
 * OWN Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 * OWN Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with OWN Server.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.programmatori.domotica.own.plugin.remote;

import java.io.*;

import org.apache.commons.net.ftp.FTPClient;
import org.programmatori.domotica.own.sdk.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy and Retrieve a remote file
 *
 * @author Moreno Cattaneo
 */
public class FTPRemote extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(FTPRemote.class);

	private String server;
	private String user;
	private String pw;
	private String remoteFileName;
	private String localFileName;
	private String protocol;
	private long interval;

	public FTPRemote() {
		setDaemon(true);
		// Read from ini
		server = Config.getInstance().getNode("remoter.host");
		user = Config.getInstance().getNode("remoter.user");
		pw = Config.getInstance().getNode("remoter.pw");
		remoteFileName = Config.getInstance().getNode("remoter.remoteFile");
		localFileName = Config.getInstance().getNode("remoter.localFile");
		protocol = Config.getInstance().getNode("remoter.protocol");

		String sInterval = Config.getInstance().getNode("remoter.interval");
		interval = Long.parseLong(sInterval);

		Config.getInstance().addThread(this);
	}

	@Override
	public void run() {
		try {
			while (!Config.getInstance().isExit()) {
				if (protocol.equals(FTPUtility.PROTOCOL)) {
					FTPClient ftp = FTPUtility.connect(server, user, pw);
					if (ftp != null) {
						logger.info("Connesso al server {}", server);

						try {
							// Recupero il file da remoto con i comandi da attivare
							File f = new File(localFileName);
							String path = f.getAbsolutePath(); // extractPath(localFileName);
							String fileName = f.getName(); // extractFileName(localFileName);
							FileOutputStream os = new FileOutputStream(path + "/temp" + fileName);
							FTPUtility.getFile(ftp, remoteFileName, os);
							logger.debug("File recuperato da remoto");

							// Scrivo il file con lo stato aggiornato
							FileInputStream is = new FileInputStream(f);
							FTPUtility.putFile(ftp, remoteFileName, is);
							logger.debug("File scritto in remoto");

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						FTPUtility.disconnect(ftp);
						logger.info("disconnesso dal server {}", server);
					} else {
						logger.error("Dati di connessione al server {} sono errati", server);
					}
				} else {
					logger.warn("Protocollo {} non riconosciuto", protocol);
				}

				logger.debug("Dormo per {} msec", interval);
				sleep(interval);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}