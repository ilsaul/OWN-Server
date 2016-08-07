package org.programmatori.domotica.own.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

public class RederSocket implements Runnable {

	private InputStream is;
	private BlockingQueue<String> received;
	private boolean close;

	public RederSocket(InputStream is, BlockingQueue<String> received) {
		this.is = is;
		this.received = received;

		close = false;
	}

	@Override
	public void run() {
		int inCh = 0;
		String ret = "";
		while ((inCh > -1)) {
			if (is != null) {
				try {
					//System.out.println("Start read...");
					inCh = is.read();
					//System.out.println("end read...");
				} catch (IOException e) {
					inCh = -1;
				}

				if (inCh != -1) ret += (char) inCh;

				if (ret.endsWith("##")) {
					received.add(ret);
					ret = "";
				}
			}
		}

		if (inCh == -1) close = true;
	}

	public Boolean isClose() {
		return close;

	}
}
