package jp.ac.osaka_u.ist.sdl.naturalness;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

abstract public class QueueThread<T> extends Thread {

	private BlockingQueue<OutputPacket> outputQueue;

	public QueueThread() {
		outputQueue = new LinkedBlockingQueue<>();
	}

	protected void init() {

	}

	abstract protected void consume(T value);

	protected void close() {

	}

	public boolean offer(T value) {
		return outputQueue.offer(new OutputPacket(value));
	}

	public boolean finishQueue() {
		return outputQueue.offer(new OutputPacket());
	}

	@Override
	public void run() {
		init();
		try {
			while(true) {
				OutputPacket packet = outputQueue.take();
				if(packet.exit) {
					break;
				}
				consume(packet.value);
			}
		} catch(InterruptedException e) {} finally {
			close();
		}

	}

	private class OutputPacket {
		T value;
		boolean exit;

		OutputPacket() {
			exit = true;
		}

		OutputPacket(T value) {
			this.value = value;
		}
	}
}
