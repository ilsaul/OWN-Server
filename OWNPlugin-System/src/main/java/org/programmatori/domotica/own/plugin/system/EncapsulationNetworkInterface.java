package org.programmatori.domotica.own.plugin.system;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class EncapsulationNetworkInterface {

	private final NetworkInterface networkInterface;

	public EncapsulationNetworkInterface(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}

	@Override
	public String toString() {
		try {

			String address = "";
			Enumeration<InetAddress> iAddress = networkInterface.getInetAddresses();
			while (iAddress.hasMoreElements()) {
				InetAddress tempIp = iAddress.nextElement();

				if (tempIp instanceof Inet6Address) {
					address += " ipv6:" + tempIp.getHostAddress();
				} else if (tempIp instanceof Inet4Address) {
					address += " ipv4:" + tempIp.getHostAddress();
				}
			}

			return "networkInterface{" +
				"name:" + networkInterface.getDisplayName() +
				", Virt:" + networkInterface.isVirtual() +
				", Up:" + networkInterface.isUp() +
				", PuP:" + networkInterface.isPointToPoint() +
				", Lb:" + networkInterface.isLoopback() +
				", addr:" + address +
				'}';
		} catch (SocketException e) {
			return "";
		}
	}
}
