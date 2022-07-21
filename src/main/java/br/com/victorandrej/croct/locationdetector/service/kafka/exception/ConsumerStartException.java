package br.com.victorandrej.croct.locationdetector.service.kafka.exception;

public class ConsumerStartException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConsumerStartException(String string) {
		super(string);
	}

}
