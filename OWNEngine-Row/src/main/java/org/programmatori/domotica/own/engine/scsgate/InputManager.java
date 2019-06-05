package org.programmatori.domotica.own.engine.scsgate;

import org.joou.UByte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InputManager {
	private static final Logger logger = LoggerFactory.getLogger(InputManager.class);

	private BlockingQueue<UByte> charsQueue = new LinkedBlockingQueue<>();

	/**
	 * Remove the value
	 * @return
	 */
	public UByte take() {
		try {
			return charsQueue.take();
		} catch (InterruptedException e) {
			logger.error("Interruption of the waiting of the value", e);
			return null;
		}
	}

	/**
	 * Watch the value but don't remove.
	 * @return
	 */
	public UByte peek() {
		return charsQueue.peek();
	}

	public UByte[] take(int length) {
		UByte values[] = new UByte[length];
		for (int i = 0; i < length; i++) {
			values[i] = take();
		}

		return values;
	}

	public char takeChar() {
		return (char) take().byteValue();
	}

	public int count() {
		return charsQueue.size();
	}

	public void add(UByte b) {
		charsQueue.add(b);
	}

	public String takeString(int length) {
		String value = "";

		for (int i = 0; i < length; i++) {
			UByte b = take();
			if (b == null) return null;

			char ch = (char) b.byteValue();
			value += ch;
		}

		logger.debug("get from Queue: {}", value);
		return value;
	}
}
