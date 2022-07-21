package br.com.victorandrej.croct.locationdetector.service.kafka.exception;

public class KafConsumerDeadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public KafConsumerDeadException(String string) {
		super(string);
	}

}
