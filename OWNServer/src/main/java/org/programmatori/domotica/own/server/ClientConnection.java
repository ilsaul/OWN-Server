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
package org.programmatori.domotica.own.server;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.Monitor;
import org.programmatori.domotica.own.sdk.server.engine.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import static org.programmatori.domotica.own.server.ConnectionStatus.CHECK_IP;

/**
 * Manager for a single client Connection.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 29/04/2012
 */
public class ClientConnection implements Runnable, Monitor, Sender {
	private static final Logger logger = LoggerFactory.getLogger(ClientConnection.class);

	private TcpIpServer server;
	private Socket clientSocket;
	private long id;

	private PrintWriter socketOut = null;
	private InputStream socketIn = null;

	private EngineManager engine;
	private int mode;
	private ConnectionStatus status;
	private StringBuilder commandBuffer;

	/**
	 * Access is restricted to a local package
	 */
	ClientConnection(Socket clientSocket, TcpIpServer server, EngineManager engine) {
		logger.trace("Client Start");
		this.server = server;
		this.clientSocket = clientSocket;
		this.engine = engine;
		mode = OpenWebNetProtocol.MODE_COMMAND;

		id = GeneratorID.get();
		logger.debug("Generate ID: {}", id);
		commandBuffer = new StringBuilder();

		status = ConnectionStatus.START;
	}

	@Override
	public void run() {
		int timeout = Config.getInstance().getWelcomeTimeout();
		try {
			clientSocket.setSoTimeout(timeout);
		} catch (SocketException e1) {
			logger.error("Error in timeout setting:", e1);
		}

		try {
			setup();
			processCommands();
		} catch (Exception e) {
			logger.error("Generic Error", e);
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				// Stub !!
			}
		}
	}

	private String readMessage() throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;

		//TODO: Make Better
		while (commandBuffer.indexOf(SCSMsg.MSG_ENDER) > -1) {
			length = socketIn.read(buffer);
			if (length != -1){
				result.write(buffer, 0, length);
				commandBuffer.append(result.toString(StandardCharsets.UTF_8.name()));
			}
		}

		int pos = commandBuffer.indexOf(SCSMsg.MSG_ENDER);
		String newCommand = commandBuffer.substring(0, pos+2);
		String tmp = commandBuffer.substring(pos+2);
		commandBuffer = new StringBuilder();
		commandBuffer.append(tmp);

		return newCommand;
	}

	private void setup() throws IOException {
		socketIn = clientSocket.getInputStream();
		socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
	}

	private void processCommands() {
		// Loop Message
		while (clientSocket.isConnected() && !Config.getInstance().isExit()) {
			SCSMsg response = null;

			switch (status) {
				case START:
					// Welcome
					response = OpenWebNetProtocol.MSG_WELCOME;
					logger.debug("Welcome msg: {}", response);
					status = ConnectionStatus.MODE;
					break;

				case MODE:
					try {
						SCSMsg msgSCS = new SCSMsg(readMessage());
						response = processStart(msgSCS);
						if (response.equals(SCSMsg.MSG_ACK)) {
							status = CHECK_IP;
						} else {
							status = ConnectionStatus.DISCONNECTED;
						}

					} catch (MessageFormatException | IOException e) {
						logger.error("Error Client in Mode setting", e);
						status = ConnectionStatus.DISCONNECTED;
						response = SCSMsg.MSG_NACK;
					}
					break;

				case CHECK_IP:
					if (checkValidIP(clientSocket.getInetAddress())) {
						status = ConnectionStatus.CONNECTED;
					} else {
						response = createPwAsk();
						status = ConnectionStatus.PASSWORD;
					}
					break;

				case PASSWORD:
//					try {
//						SCSMsg msgNo = new SCSMsg("∗98∗##"); // Open Command
//						SCSMsg msg1 = new SCSMsg("∗98∗1##"); // sha1 Authentication
//						SCSMsg msg2 = new SCSMsg("∗98∗2##"); // sha2 Authentication
//					} catch (MessageFormatException e) {
//						// Stub!!
//					}

					//TODO: Implement PASSWORD case - Bug.ID: #91
					status = ConnectionStatus.CONNECTED;
					break;

				case CONNECTED:
					try {
						String sMsg = readMessage();

						if (sMsg != null && mode == OpenWebNetProtocol.MODE_MONITOR) {
							logger.error("Attempt to send command in monitor mode");
							status = ConnectionStatus.DISCONNECTED;

						} else if (sMsg != null) {
							try {
								SCSMsg msg = new SCSMsg(sMsg);
								engine.sendCommand(msg, this);

							} catch (MessageFormatException e) {
								logger.error("Command format received invalid", e);
								status = ConnectionStatus.DISCONNECTED;
							}
						}
					} catch (IOException e) {
						logger.error("Error Client in Mode setting", e);
						status = ConnectionStatus.DISCONNECTED;
						response = SCSMsg.MSG_NACK;
					}
					break;

				case DISCONNECTED:
					if (mode == OpenWebNetProtocol.MODE_MONITOR) {
						engine.removeMonitor(this);
					}
					return;

				default:
					logger.error("Unknown Status");
					break;
			}

			if (response != null) {
				socketOut.print(response.toString());
				socketOut.flush();
				logger.debug("{} TX MSG: {}", getId(), response);
				Config.getInstance().getMessageLog().log(response, true, getId());
			}
		}

		logger.trace("Client End");
	}

	private SCSMsg processStart(SCSMsg msgSCS) {
		SCSMsg response = SCSMsg.MSG_ACK;

		if (msgSCS.equals(OpenWebNetProtocol.MSG_MODE_COMMAND)) {
			mode = OpenWebNetProtocol.MODE_COMMAND;
			logger.info("{} Mode: Command", getId());

		} else if (msgSCS.equals(OpenWebNetProtocol.MSG_MODE_MONITOR)) {
			mode = OpenWebNetProtocol.MODE_MONITOR;

			//Bug.ID: #3 - Monitor don't have time-out
			try {
				clientSocket.setSoTimeout(0);
			} catch (SocketException e) {
				logger.error("Error:" , e);
			}

			logger.info("{} Mode: Monitor", getId());
			engine.addMonitor(this);

		// This mode don't exist in BTicino Server
		} else if (msgSCS.equals(OpenWebNetProtocol.MSG_MODE_TEST)) {
			mode = OpenWebNetProtocol.MODE_TEST;

			// Mixed mode I disable timeout
			try {
				clientSocket.setSoTimeout(0);
			} catch (SocketException e) {
				logger.error("Error:" , e);
			}

			logger.info("{} Mode: Test", getId());
			engine.addMonitor(this);
		} else {
			response = SCSMsg.MSG_NACK;
		}

		return response;
	}

	private SCSMsg createPwAsk() {
		//FIXME: Add algorithm for password
		return null;
	}

	/**
	 * This is for limit who can access to the bus.<br>
	 * I haven't yet implement.
	 */
	private boolean checkValidIP(InetAddress ip) {
		//FIXME: Now i accept anyone
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientConnection other = (ClientConnection) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void receiveMsg(SCSMsg msg) {
		logger.debug("{} TX MSG: {}", getId(), msg);

		Config.getInstance().getMessageLog().log(msg, true, getId());

		socketOut.print(msg.toString());
		socketOut.flush();
	}

}
