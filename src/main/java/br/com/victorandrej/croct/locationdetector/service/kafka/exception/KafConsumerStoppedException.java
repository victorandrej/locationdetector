package br.com.victorandrej.croct.locationdetector.service.kafka.exception;

public class KafConsumerStoppedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public KafConsumerStoppedException(String string) {
		super(string);
	}

}
