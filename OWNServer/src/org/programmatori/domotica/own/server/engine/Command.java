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
package org.programmatori.domotica.own.server.engine;

import java.io.Serializable;
import java.util.*;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.Sender;

/**
 * Manage the association between message recived to the bus and the answare received.
 * 
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since OWNServer v0.1.0
 * @version 1.0.0 16/10/2010
 */
public class Command implements Comparable<Command>, Serializable {
	private static final long serialVersionUID = 3865810383162847583L;

	private static String patternTime = "%1$tH:%1$tM:%1$tS:%1$tL";

	private Sender client;
	private Date timeCreate;
	private Date timeSend;
	private Date timeAnswer;
	private SCSMsg msgSend;
	private List<SCSMsg> msgReceive;
	private SCSMsg status;

	public Command(Sender client, SCSMsg msg) {
		this.client = client;
		msgReceive = new ArrayList<SCSMsg>();

		if (client == null) {
			msgReceive.add(msg);
			timeAnswer = Calendar.getInstance().getTime();
		} else {
			msgSend = msg;
			timeCreate = Calendar.getInstance().getTime();
		}

		status = null;
	}

	public Sender getClient() {
		return client;
	}
	public SCSMsg getSendMsg() {
		return msgSend;
	}
	public List<SCSMsg> getReceiveMsg() {
		return msgReceive;
	}
	public SCSMsg getStatus() {
		return status;
	}
	public void setStatus(SCSMsg status) {
		this.status = status;
	}

	public Date getTimeSend() {
		return timeSend;
	}

	public void setTimeSend(Date timeSend) {
		this.timeSend = timeSend;
	}

	public Date getTimeCreate() {
		return timeCreate;
	}

	public Date getTimeAnswer() {
		return timeAnswer;
	}

	public void setReceived(Command command) {
		timeAnswer = command.getTimeAnswer();
		msgReceive.addAll(command.getReceiveMsg());

		// Settaggio Stato
		if (status == null) {
			status = command.getStatus();
		}
	}

	@Override
	public int compareTo(Command o) {
		int priority = 0;

		if (getTimeAnswer() == null || o.getTimeAnswer() == null) {
			if (getTimeAnswer() == null) priority = 1;
			if (o.getTimeAnswer() == null) priority = -1;
		} else {
			priority = getTimeAnswer().compareTo(o.getTimeAnswer());
		}

		if (priority == 0) {
			priority = getTimeSend().compareTo(o.getTimeSend());
		}

		return priority;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		//Format f = new SimpleDateFormat("H:m:s:S");

		if (msgSend != null) {
			sb.append("Msg Send: ");
			sb.append(msgSend.toString());

			if (timeCreate != null) {
				sb.append(" Create: ");
				//sb.append(f.format(timeCreate));
				sb.append(String.format(patternTime, timeCreate));
			}

			if (timeSend != null) {
				sb.append(" Sended: ");
				//sb.append(f.format(timeSend));
				sb.append(String.format(patternTime, timeSend));
			}
		}

		if (msgReceive.size() > 0) {
			sb.append(" Msg Receive: ");
			sb.append(msgReceive.toString());

			sb.append(" Receive: ");
			//sb.append(f.format(timeAnswer));
			sb.append(String.format(patternTime, timeAnswer));
		}

		return sb.toString().trim();
	}

}
