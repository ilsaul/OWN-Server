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
package org.programmatori.domotica.own.server.clients;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.domotica.own.sdk.msg.Where;
import org.programmatori.domotica.own.sdk.msg.Who;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.Monitor;
import org.programmatori.domotica.own.sdk.server.engine.Sender;
import org.programmatori.domotica.own.server.utils.ConnectionState;
import org.programmatori.domotica.own.server.utils.GeneratorID;
import org.programmatori.domotica.own.server.utils.OpenWebNetProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Manage single client Connection. It's execute like a thread.
 * Any client need to handshake some information.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 29/04/2012
 */
public class ClientConnection implements Runnable, Monitor, Sender {
	private static final Logger logger = LoggerFactory.getLogger(ClientConnection.class);

	private final Socket clientSocket;
	private final EngineManager engineManager;
	private final long id;

	private PrintWriter socketOut = null;
	private InputStream socketIn = null;

	private ClientSessionType mode;
	private ConnectionState status;
	private StringBuilder commandBuffer;

	/**
	 * Access restricted to a local package
	 */
	public ClientConnection(Socket clientSocket, EngineManager engineManager) {
		logger.trace("Client Start");

		this.clientSocket = clientSocket;
		this.engineManager = engineManager;
		mode = ClientSessionType.NONE; // initial set
		status = ConnectionState.START;

		id = GeneratorID.get();
		logger.info("Client {} connecting ({})", id, clientSocket.getInetAddress());

		commandBuffer = new StringBuilder();
	}

	/**
	 * Execute in a thread
	 */
	@Override
	public void run() {
		int timeout = Config.getInstance().getWelcomeTimeout();
		try {
			clientSocket.setSoTimeout(timeout);
		} catch (SocketException e1) {
			logger.error("Error in setting timeout", e1);
		}

		try {
			setup();
			handshake();
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

	/**
	 * Prepare input and output stream for client.
	 */
	private void setup() throws IOException {
		socketIn = clientSocket.getInputStream();
		socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
	}

	/**
	 * Check if client can connect to server and what it want to do.
	 */
	private void handshake() {
		// Loop Message
		while (clientSocket.isConnected() && !Config.getInstance().isExit()) {
			SCSMsg response = null;

			switch (status) {
				case START: // out
					response = commandStart();
					break;

				case MODE: // in
					response = commandMode();
					break;

				case PASSWORD: // out
					response = commandPw();
					break;

				case WAIT_IDENT: // In
					response = commandWaitIdent();
					break;

				case CONNECTED:
					response = commandConnected();
					break;

				case DISCONNECTED:
					if (mode == ClientSessionType.MODE_MONITOR) {
						engineManager.removeMonitor(this);
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
	}

	private void processCommands() {
		logger.trace("Client End");
	}

	/**
	 * This only send message to client for start handshake.
	 * It don't receive any msg.
	 * @return msg for client
	 */
	private SCSMsg commandStart() {
		// Welcome
		SCSMsg response = OpenWebNetProtocol.MSG_WELCOME;
		logger.debug("Welcome msg: {}", response);
		status = ConnectionState.MODE;

		return response;
	}

	/**
	 * The client inform the server what he want to do
	 *
	 * @return if server understed the command
	 */
	private SCSMsg commandMode() {
		SCSMsg response = null;
		try {
			String sMsg = readMessage();
			if (sMsg != null) {
				SCSMsg msgSCS = new SCSMsg(sMsg);
				logger.info("{} RX Msg {}", getId(), msgSCS);

				response = processMode(msgSCS);
				if (response.equals(ServerMsg.MSG_ACK.getMsg())) {
					status = ConnectionState.PASSWORD;
				} else {
					status = ConnectionState.DISCONNECTED;
				}
			} else {
				logger.debug("I continue to wait because message is not valid");
			}

		} catch (MessageFormatException | IOException e) {
			logger.error("Error Client in Mode setting", e);
			status = ConnectionState.DISCONNECTED;
			response = ServerMsg.MSG_NACK.getMsg();
		}

		return response;
	}

	private SCSMsg commandPw() {
		SCSMsg response = null;

		//TODO: Implement PASSWORD case - Bug.ID: #91
		try {
			String sMsg = readMessage();
			logger.debug("PW: {}", sMsg);

			status = ConnectionState.CONNECTED;
			return ServerMsg.MSG_ACK.getMsg();
		} catch (IOException e) {
			logger.error("Error Client in retrieve password", e);
		}

		//TODO: HMAC enable ?
		//TODO: No - Standard Authenticatin
		//TODO Yes - Send Alghorithm

		try {
//			SCSMsg msgNo = new SCSMsg("*98*##"); // Open Command
//			response = new SCSMsg("*98*1##"); // sha1 Authentication
//			SCSMsg msg2 = new SCSMsg("*98*2##"); // sha2 Authentication
		} catch (MessageFormatException e) {
			// Stub!!
		}


		//status = ConnectionState.WAIT_IDENT;
		status = ConnectionState.CONNECTED;

		return response;
	}

	private SCSMsg commandWaitIdent() {
		SCSMsg response = null;

		try {
			String sMsg = readMessage();

			response = new SCSMsg(new Who(true, "469712896"), new Where(false, ""), null, null, null);

		} catch (IOException | MessageFormatException e) {
			e.printStackTrace();
		}

		return response;
	}

	private SCSMsg commandConnected() {
		SCSMsg response = null;

		try {
			String sMsg = readMessage();
			logger.info("{} RX Msg {}", getId(), sMsg);

			if (sMsg != null && mode == ClientSessionType.MODE_MONITOR) {
				logger.error("Attempt to send command in monitor mode");
				status = ConnectionState.DISCONNECTED;

			} else if (sMsg != null) {
				try {
					SCSMsg msg = new SCSMsg(sMsg);
					engineManager.sendCommand(msg, this);

				} catch (MessageFormatException e) {
					logger.error("Command format received invalid", e);
					status = ConnectionState.DISCONNECTED;
				}
			}
		} catch (IOException e) {
			logger.error("Error Client in Mode setting", e);
			status = ConnectionState.DISCONNECTED;
			response = ServerMsg.MSG_NACK.getMsg();
		}

		return response;
	}

	private SCSMsg processMode(SCSMsg msgSCS) {
		SCSMsg response = ServerMsg.MSG_ACK.getMsg();

		if (msgSCS.equals(ServerMsg.MSG_MODE_COMMAND.getMsg())) {
			mode = ClientSessionType.MODE_COMMAND;
			logger.info("{} Mode: Command enable", getId());

		} else if (msgSCS.equals(ServerMsg.MSG_MODE_MONITOR.getMsg())) {
			mode = ClientSessionType.MODE_MONITOR;

			//Bug.ID: #3 - Monitor don't have time-out
			try {
				clientSocket.setSoTimeout(0);
			} catch (SocketException e) {
				logger.error("Error:", e);
			}

			logger.info("{} Mode: Monitor enable", getId());
			engineManager.addMonitor(this);

		// This mode doesn't exist in BTicino Server
		// TODO: Enable it in OWN Version only
		} else if (msgSCS.equals(ServerMsg.MSG_MODE_TEST.getMsg())) {
			mode = ClientSessionType.MODE_TEST;

			// A mixed mode: I disable timeout
			try {
				clientSocket.setSoTimeout(0);
			} catch (SocketException e) {
				logger.error("Error:", e);
			}

			logger.info("{} Mode: Test", getId());
			engineManager.addMonitor(this);

		} else {
			logger.error("Connection Mode not supported {}", msgSCS);
			response = ServerMsg.MSG_NACK.getMsg();
		}

		return response;
	}

	private SCSMsg createPwAsk() {
		//FIXME: Add algorithm for password
		return null;
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

	private String readMessage() throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;

		//TODO: Make Better
		do {
			length = socketIn.read(buffer);
			if (length != -1) {
				result.write(buffer, 0, length);
				commandBuffer.append(result.toString(StandardCharsets.UTF_8.name()));
			}
		} while (commandBuffer.indexOf(SCSMsg.MSG_ENDED) == -1);

		int pos = commandBuffer.indexOf(SCSMsg.MSG_ENDED);

		if (pos > -1) {
			String newCommand = commandBuffer.substring(0, pos + 2);
			String tmp = commandBuffer.substring(pos + 2);
			commandBuffer = new StringBuilder();
			commandBuffer.append(tmp);

			return newCommand;
		} else {
			logger.error("The message is not complete: {}", commandBuffer);
			return null;
		}
	}

}
