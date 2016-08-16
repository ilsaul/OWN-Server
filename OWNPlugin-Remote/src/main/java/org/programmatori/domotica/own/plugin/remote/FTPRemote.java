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
	private static final Logger LOGGER = LoggerFactory.getLogger(FTPRemote.class);

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
						LOGGER.info("connect to server {}", server);

						try {
							// Recupero il file da remoto con i comandi da attivare
							File f = new File(localFileName);
							String path = f.getAbsolutePath(); // extractPath(localFileName);
							String fileName = f.getName(); // extractFileName(localFileName);
							FileOutputStream os = new FileOutputStream(path + "/temp" + fileName);
							FTPUtility.getFile(ftp, remoteFileName, os);
							LOGGER.debug("Retrive file from remote");

							// Scrivo il file con lo stato aggiornato
							FileInputStream is = new FileInputStream(f);
							FTPUtility.putFile(ftp, remoteFileName, is);
							LOGGER.debug("Write file to remote");

						} catch (FileNotFoundException e) {
							e.printStackTrace();
							LOGGER.error("Retrive file from remote", e);
						}

						FTPUtility.disconnect(ftp);
						LOGGER.info("disconect from server {}", server);
					} else {
						LOGGER.error("Data connection to the server {} are wrong", server);
					}
				} else {
					LOGGER.warn("Protocol {} unknown", protocol);
				}

				LOGGER.debug("Sleep for {} msec", interval);
				sleep(interval);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}