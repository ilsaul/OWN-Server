/*
 * OWN Server is
 * Copyright (C) 2010-2014 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
package org.programmatori.domotica.own.plugin.power;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.tools.RunScript;
import org.programmatori.domotica.own.sdk.utils.LogUtility;

/**
 * This class is utility for access to DataBase.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.0, 28/09/2014
 */
public class DbUtil {
	private static Log log = LogFactory.getLog(DbUtil.class);

	private static final String DRIVER = "org.h2.Driver";
	private static final String PROTOCOL = "jdbc:h2:";
	private static final String URL_DB = "db/";
	private static final String URL_CRYPT = ";CIPHER=AES";

	private static DbUtil instance = null;
	private String dbName;
	private String user;
	private String userPw;
	private String cryptPw;

	private DbUtil() {
		cryptPw = "";
	}

	public static DbUtil getInstance() {
		if (instance == null) {
			synchronized (DbUtil.class) {
				if (instance == null) {
					instance = new DbUtil();
				}
			}
		}

		return instance;
	}

	// ---------------- Setting for open db -----------------------------

	/**
	 * Setting the DataBase Name
	 *
	 * @param dbName Database name
	 */
	public void setDBName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * Setting for connect to the database
	 * @param user Username
	 * @param pw password
	 */
	public void setUser(String user, String pw) {
		this.user = user;
		this.userPw = pw;
	}

	/**
	 * set encryption string for encrypt database
	 * @param crypt string
	 */
	public void setCrypt(String crypt) {
		this.cryptPw = crypt;
	}

	// ------------------------ Access to database ----------------------

	public Connection getConnection() {
		log.trace("Start getConnection");
		Connection conn = null;

		try {
			Class.forName(DRIVER).newInstance();

			Properties prop = new Properties();
	        prop.setProperty("user", user);
	        prop.put("password", userPw);

			String url = PROTOCOL + URL_DB + dbName;
			if (cryptPw.trim().length() > 0) {
				prop.put("password", cryptPw + " " + userPw);
				url = url + URL_CRYPT;
			}
//			else {
//				url += ";";
//			}

			conn = DriverManager.getConnection(url, prop);
			conn.setAutoCommit(false);

			log.debug("Connection estabilished");
		} catch (Exception e) {
			log.error(LogUtility.getErrorTrace(e));
		}

		log.trace("End getConnection");
		return conn;
	}

	public static void closeConnection(Connection conn) {
		log.trace("Start closeConnection");

		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
			log.debug("Connection closed");
		} catch (Exception e) {
			log.error(LogUtility.getErrorTrace(e));
		}

		log.trace("End closeConnection");
	}

	public boolean isValidDB(String sqlCheckDB) {
		log.trace("Start checkDB");
		boolean isDBOk = false;
		Connection conn = null;

		try {
			conn = getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlCheckDB);

			if (rs != null) {
				isDBOk = true;
			}
		} catch (SQLException e) {
			log.error(LogUtility.getErrorTrace(e));
			log.info("Possibile db non esistente");
		} catch (NullPointerException e) {
				log.error(LogUtility.getErrorTrace(e));
				log.info("Possibile db non esistente");
		} finally {
			closeConnection(conn);
		}

		log.trace("End checkDB");
		return isDBOk;
	}

	public void createDB(InputStream in) {
		log.trace("Start createDB");

		Connection conn = getConnection();
		StringWriter sw = new StringWriter();
		try {
			InputStreamReader reader = new InputStreamReader(in);
			//FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(reader);

			if (br.ready()) {
				String line = br.readLine();
				while (line != null) {
					sw.append(line);
					line = br.readLine();
				}
			}

		} catch (Exception e) {
			log.error(LogUtility.getErrorTrace(e));
		}

		String sql = sw.toString();
		try {
			Statement stmt = conn.createStatement();

			int minIndex = 0;
			int maxIndex = 0;

			do {
				maxIndex = sql.indexOf(';', minIndex);

				if (maxIndex > -1) {
					String tempSql = sql.substring(minIndex, maxIndex);
					Charset c = Charset.forName("UTF-8");
					String s = new String(tempSql.getBytes(c), c);
					log.info(s);

					minIndex = maxIndex + 1;
					try {
						stmt.execute(s);
					} catch (SQLException e) {
						log.info("Error Code: " + e.getErrorCode());
						if (e.getMessage().startsWith("'DROP TABLE'"))
							continue;
						throw e;
					}
				}
			} while (maxIndex > -1);
			if (minIndex < sql.length() - 1) {
				String tempSql = sql.substring(minIndex, sql.length());
				log.info(tempSql);
				stmt.execute(tempSql);
			}

			conn.commit();
		} catch (Exception e) {
			log.error(LogUtility.getErrorTrace(e));

			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) { /* Stub */
			}
		} finally {
			closeConnection(conn);
		}

		log.trace("End createDB");
	}

	public void createDB(String scriptName) {

		try {
			FileInputStream f = new FileInputStream(scriptName);
			createDB(f);
		} catch (FileNotFoundException e) {
			LogUtility.getErrorTrace(e);
		}



	}

	public static int checkBatchStatus(int[] executeBatch) {
		int status = 1;

		for (int i = 0; i < executeBatch.length; i++) {
			if (executeBatch[i] == Statement.EXECUTE_FAILED) {
				status = i;
				break;
			}
		}

		return status;
	}

	/**
	 * Initialize a database from a SQL script file.
	 */
	public void execScriptDb(String scriptName) throws Exception {
		Connection conn = getConnection();

		File f = new File(scriptName);
		FileReader in = new FileReader(f);

		//InputStream in = getClass().getResourceAsStream(scriptName);
		if (f.exists()) {
			log.warn("File not found");
		} else {
			RunScript.execute(conn, in);
			closeConnection(conn);
		}
	}

	/**
	 * Test
	 */
	public static void main(String[] args) {
		DbUtil.getInstance().setDBName("ProvaDB");
		DbUtil.getInstance().setUser("admin", "admin");
		//DbUtil.getInstance().setCrypt("provaCrypt");

		Connection conn = DbUtil.getInstance().getConnection();

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS TEST");
			stmt.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR)");

			PreparedStatement prep = conn.prepareStatement("INSERT INTO TEST VALUES(?, 'Test' || SPACE(100))");
			long time;
			time = System.currentTimeMillis();

			int len = 1000;
			int i = 0;
			for (; i < len; i++) {
				long now = System.currentTimeMillis();
				if (now > time + 10) {
					time = now;
					System.out.println("Inserting " + (100L * i / len) + "%");
				}

				prep.setInt(1, i);
				prep.execute();
			}
			System.out.println("Inserting " + (100L * i / len) + "%");

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		DbUtil.closeConnection(conn);

	}
}