/*
 * OWN Server is 
 * Copyright (C) 2010-2012 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * @author Marco Cazzaniga
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
public class FTPUtility {
	private static final Log log = LogFactory.getLog(FTPUtility.class);
	
	public static final String PROTOCOL = "ftp";

	/**
	 * Funzione che consente la connessione ad un Server FTP
	 * 
	 * @param ftpServer Server FTP
	 * @param username Nome utente per l'accesso
	 * @param password Password per l'accesso
	 * @return Un oggetto di tipo FTPClient contenente il Client per l'accesso
	 */
	public static FTPClient connect(String ftpServer, String username, String password) {

		FTPClient ftp = new FTPClient();
		String replyString;
		try {
			ftp.connect(ftpServer);
			ftp.login(username, password);
			log.info("Connesso a " + ftpServer + ".");

			replyString = ftp.getReplyString();
			log.debug(replyString);

			int reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				log.error("Il Server FTP ha rifiutato la connessione.");
				log.error(replyString);
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ftp;
	}

	/**
	 * Funzione che consente la disconnessione da un Server FTP
	 * 
	 * @param ftp Offetto di tipo FTPClient utilizzato per l'accesso
	 * @return >0 se tutto funziona correttamente; <0 in caso di errori
	 */
	public static int disconnect(FTPClient ftp) {
		if (ftp.isConnected()) {
			try {
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		log.info("Disconnessione avvenuta con successo.");
		return 1;
	}

	/**
	 * Funzione che consente di scrivere un file sul sito FTP remoto
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
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/**
	 * Funzione che consente di leggere un file sul sito FTP remoto
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
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/**
	 * Funzione che salva un file di testo XML su un server remoto via FTP
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
			log.error("Il file non esiste.");
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

	/**
	 * Funzione che legge un file di testo XML su un server remoto via FTP
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
			log.error("Il file non esiste.");
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

//	public void run() {
//		remoteFileName = "config.xml";
//		Date now = new Date();
//		String localFileName = "config.xml";
//
//		// Legge da remoto e scrive in locale
//		if (this.readXML(remoteFileName, localFileName, server, user, pw) == 1)
//			log.info("Operazione eseguita con successo: letto file " + remoteFileName + " e scritto su disco come " + localFileName + ".");
//		else
//			log.error("Operazione non eseguita per errori.");
//
//		// Legge in locale e scrive su remoto
//		if (this.readXML(remoteFileName, localFileName, server, user, pw) == 1)
//			log.info("Operazione eseguita con successo: scritto file " + remoteFileName + "sul server remoto.");
//		else
//			log.error("Operazione non eseguita per errori.");
//	}

	public static void main(String[] args) {

		//FTPUtility remote = new FTPUtility();
		FTPClient ftp = FTPUtility.connect("192.168.3.97", "marco", "heaven");

		if (ftp != null) {

			// FileInputStream file;
			//
			// try {
			// file = new FileInputStream("/Users/Heaven/prova.txt");
			// remote.saveFile(ftp, "prova.txt", file);
			// } catch (FileNotFoundException e) {
			// System.out.println("Il file non esiste.");
			// e.printStackTrace();
			// }

			// FileOutputStream file;
			// try {
			// file=new FileOutputStream("/Users/Heaven/prova.uno");
			// remote.readFile(ftp, "prova.txt", file);
			// } catch (FileNotFoundException e) {
			// System.out.println("Il file non esiste.");
			// e.printStackTrace();
			// }
			FTPUtility.disconnect(ftp);
		}

	}

}
