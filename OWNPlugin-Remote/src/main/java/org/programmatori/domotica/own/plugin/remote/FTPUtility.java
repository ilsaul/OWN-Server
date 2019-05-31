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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for send and retrive files.
 * <br>
 * Example:<br>
 * <code>
 * FTPClient ftp = FTPUtility.connect("192.168.3.97", "user", "test");<br>
 * if (ftp != null) {<br>
 *   FileInputStream file = new FileInputStream("prova.txt");<br>
 *   ftp.saveFile(ftp, "prova.txt", file);<br>
 *   FileOutputStream file=new FileOutputStream("/Users/Heaven/prova.uno");<br>
 *   ftp.readFile(ftp, "prova.txt", file);<br>
 *   FTPUtility.disconnect(ftp);<br>
 * }
 * </code>
 *
 * @author Marco Cazzaniga
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.2 (14/08/2016)
 */
public class FTPUtility {
	/** log for the class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FTPUtility.class);

	/** Protocol used from this utility. */
	public static final String PROTOCOL = "ftp";

	private FTPUtility() {
		throw new IllegalAccessError("Utility class");
	}

	/**
	 * Funzione che consente la connessione ad un Server FTP.
	 *
	 * @param ftpServer Server FTP
	 * @param username Nome utente per l'accesso
	 * @param password Password per l'accesso
	 * @return Un oggetto di tipo FTPClient contenente il Client per l'accesso
	 */
	public static FTPClient connect(String ftpServer, String username, String password) {

		final FTPClient ftp = new FTPClient();
		String replyString;
		try {
			ftp.connect(ftpServer);
			ftp.login(username, password);
			LOGGER.info("Connesso a {}.", ftpServer);

			replyString = ftp.getReplyString();
			LOGGER.debug(replyString);

			int reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				LOGGER.error("Il Server FTP ha rifiutato la connessione.");
				LOGGER.error(replyString);
				return null;
			}
		} catch (IOException e) {
			LOGGER.error("Connession problem", e);
		}

		return ftp;
	}

	/**
	 * Funzione che consente la disconnessione da un Server FTP.
	 *
	 * @param ftp Offetto di tipo FTPClient utilizzato per l'accesso
	 * @return >0 se tutto funziona correttamente; <0 in caso di errori
	 */
	public static int disconnect(FTPClient ftp) {
		if (ftp.isConnected()) {
			try {
				ftp.disconnect();
			} catch (IOException e) {
				LOGGER.error("Disconnection", e);
				return -1;
			}
		}
		LOGGER.info("Logout successful.");
		return 1;
	}

	/**
	 * Funzione che consente di scrivere un file sul sito FTP remoto.
	 *
	 * @param ftp Oggetto di tipo FTPClient da usare per il salvataggio del file
	 * @param name Nome del file da inserire sul server remoto
	 * @param file Flusso di tipo InputStream, da cui prelevare il file
	 * @return >=0 in caso tutto vada bene; <0 in caso di errori
	 */
	public static int putFile(FTPClient ftp, String name, InputStream file) {
		try {
			ftp.storeFile(name, file);
		} catch (IOException e) {
			LOGGER.error("Put File", e);
			return -1;
		}
		return 0;
	}

	/**
	 * Funzione che consente di leggere un file sul sito FTP remoto.
	 *
	 * @param ftp Oggetto di tipo FTPClient da usare per il recupero del file
	 * @param name Nome del file da recuperare dal server Remoto
	 * @param output Flusso di tipo OutputStream, su cui salvare il file
	 * @return >=0 in caso tutto vada bene; <0 in caso di errori
	 */
	public static int getFile(FTPClient ftp, String name, OutputStream output) {
		try {
			ftp.retrieveFile(name, output);
		} catch (IOException e) {
			LOGGER.error("Get File", e);
			return -1;
		}
		return 0;
	}

	/**
	 * Funzione che salva un file di testo XML su un server remoto via FTP.
	 *
	 * @param pathXML Nome e path del file XML da depositare in remoto
	 * @param ftpServer Server FTP su cui depositare il file
	 * @param username Nome utente per l'accesso ftp
	 * @param password Password per l'accesso ftp
	 * @return >0 se l'operazione si è conclusa correttamente; <0 in caso di
	 *         errore
	 */
	public int saveXML(String remoteName, String localName, String ftpServer, String username, String password) {

		FTPClient ftp = connect(ftpServer, username, password);
		FileInputStream file;

		try {
			file = new FileInputStream(localName);
			putFile(ftp, remoteName, file);
			disconnect(ftp);
		} catch (FileNotFoundException e) {
			LOGGER.error("File does not exist.", e);
			return -1;
		}
		return 1;
	}

	/**
	 * Funzione che legge un file di testo XML su un server remoto via FTP.
	 *
	 * @param remoteName Nome e path del file XML da leggere in remoto
	 * @param localName Nome e path del file XML da scrivere in locale
	 * @param ftpServer Server FTP su cui depositare il file
	 * @param username Nome utente per l'accesso ftp
	 * @param password Password per l'accesso ftp
	 * @return >0 se l'operazione si è conclusa correttamente; <0 in caso di errore
	 */
	public int readXML(String remoteName, String localName, String ftpServer, String username, String password) {

		FTPClient ftp = connect(ftpServer, username, password);
		FileOutputStream file;

		try {
			file = new FileOutputStream(localName);
			getFile(ftp, remoteName, file);
		} catch (FileNotFoundException e) {
			LOGGER.error("File does not exist.", e);
			return -1;
		}
		return 1;
	}
}
