/*
 * OWN Server is
 * Copyright (C) 2010-2015 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
package org.programmatori.domotica.own.test;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.iot.own.server.utils.OpenWebNetProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SCSClient {
	public static final String MODE_COMMAND = "1";
	public static final String MODE_MONITOR = "2";

	private String host;
	private int port;
	private String mode;
	//private boolean close;

	private Socket socket;
	private InputStream is;
	private OutputStream os;

	private BlockingQueue<String> received;

	private RederSocket reader;

	public SCSClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;

		received = new LinkedBlockingQueue<String>();

		//setName("SCS Client");

		mode = null;
		//close = true;
	}

	public void connect(String newMode) {
		this.mode = newMode;

		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			socket = new Socket(address, port);
			is = socket.getInputStream();
			os = socket.getOutputStream();
			//close = false;

			reader = new RederSocket(is, received);
			Thread t = new Thread(reader);
			t.setName("SCS Client");
			t.start();

		} catch (Exception e) {
			e.printStackTrace();
		}

		String welcome = take();
		if (!OpenWebNetProtocol.MSG_WELCOME.toString().equals(welcome)) {
			throw new RuntimeException("Connection Failed Server Not Present");
		}

		if (mode != null) {
			String ret = "";
			if (MODE_COMMAND.equals(mode)) {
				ret = send(ServerMsg.MSG_MODE_COMMAND.getMsgString());
			} else if (MODE_MONITOR.equals(mode)) {
				ret = send(ServerMsg.MSG_MODE_MONITOR.getMsgString());
			}

			if (!ServerMsg.MSG_ACK.getMsgString().equals(ret)) {
				throw new RuntimeException("Authentication Failed");
			}
		}

	}

	public String take() {
		try {
			return received.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Deprecated
	public void checkTimeOut() throws IOException {
		int inCh = 0;
		while (inCh > -1) {
			inCh = is.read();
		}

		if (inCh == -1) throw new IOException("Server Close Connection");
	}

	public String send(String msg) {
		//SCSMsg msg = new SCSMsg(sMsg);
		String ret = "";

		try {
			os.write(msg.getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(Config.getInstance().getSendTimeout());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		while (received.size() > 0) {
			ret += take();

		}

		return ret;
	}

	public boolean isClose() {
		return reader.isClose();
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
