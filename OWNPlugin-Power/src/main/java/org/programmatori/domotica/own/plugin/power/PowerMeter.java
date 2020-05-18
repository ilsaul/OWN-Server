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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.programmatori.domotica.own.sdk.utils.LogUtility;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class is the main part of the plug-in. This read all information about power consumption and recording in DB.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.0, 28/09/2014
 */
public class PowerMeter extends Thread implements PlugIn {
	private static final Log log = LogFactory.getLog(PowerMeter.class);

	private int restartEvery = 1000 * 60 * 10; // 10 min
	private EngineManager engine;

	public PowerMeter(EngineManager engine) {
		setName("Power Meter");
		this.engine = engine;

		restartEvery = Integer.parseInt(Config.getInstance().getNode("power.intervall"));
		String unit = Config.getInstance().getNode("power.intervall[@unit]");
		log.debug(restartEvery + " " + unit);

		if ("min".equals(unit)) {
			restartEvery = restartEvery * 60 * 1000;
		}
		if ("sec".equals(unit)) {
			restartEvery = restartEvery * 1000;
		}
		if ("hour".startsWith(unit)) {
			restartEvery = restartEvery * 60 * 60 * 1000;
		}

		DbUtil.getInstance().setDBName("Power");
		DbUtil.getInstance().setUser("power", "pw123");

		if (!DbUtil.getInstance().isValidDB("SELECT count(*) FROM WATT_READ")) {
			InputStream in = PowerMeter.class.getResourceAsStream("createDB.sql");
			if (in == null) {
				in = PowerMeter.class.getResourceAsStream("createDB.sql");
			}

			DbUtil.getInstance().createDB(in);
		}
	}

	@Override
	public void receiveMsg(SCSMsg msg) {
		log.debug("Power recived msg: "+ msg);
		if (msg.getWho().getMain() == 3) { //PowerUnit.MUST_WHO) {
			if (msg.getWhere().getMain() == 10) { //&& msg.isStatus()) {
				String value = msg.getValue().getSingleValue(1);
				addWatt(value);
			}
		} else {
			// ignore other message
		}

	}

	/**
	 * need to write non DB
	 * @param value
	 */
	private void addWatt(String value) {
		log.debug("Power: " + value + "W");
		Connection conn = DbUtil.getInstance().getConnection();


		try {
			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO WATT_READ VALUES(CURRENT_TIMESTAMP, " + value + ")";
			log.debug("SQL: " + sql);
			stmt.execute(sql);

			stmt.close();
			conn.commit();
			stmt = null;
		} catch (SQLException e) {
			log.error(LogUtility.getErrorTrace(e));
		} finally {
			DbUtil.closeConnection(conn);
		}


	}

	@Override
	public void run() {

		while (true) {
			try {
				log.trace("Pre sleep: " + restartEvery);
				sleep(restartEvery);
				log.trace("Post sleep");
			} catch (InterruptedException e) {
				log.error(LogUtility.getErrorTrace(e));
			}

			sendCommand();
		}
	}

	private void sendCommand() {
		try {
			SCSMsg msg = new SCSMsg("*#3*10*0##");

			engine.sendCommand(msg, this);
		} catch (MessageFormatException e) {
			log.error(LogUtility.getErrorTrace(e));
		}
	}

	public static void main(String[] args) {
		//URL url = ClassLoader.getSystemResource("CreateDB.sql");
		//url = ClassLoader.getSystemClassLoader().getResource("CreateDB.sql");
//		InputStream in = PowerMeter.class.getResourceAsStream("CreateDB.sql");
//		if (in == null) {
//			in = PowerMeter.class.getResourceAsStream("org/programmatori/domotica/own/plugin/power/CreateDB.sql");
//		}

		//System.out.println(in.toString());

		try {
			SCSMsg msg = new SCSMsg("*3*10*0*5*6*7*8##");
			//recive2Msg(msg);
		} catch (MessageFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

