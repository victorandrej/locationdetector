package br.com.victorandrej.croct.locationdetector.service.apistack.exception;

import br.com.victorandrej.croct.locationdetector.service.apistack.error.record.ApiStackUnknownError;

public class ApiStackUnknownException extends Exception {

	private static final long serialVersionUID = 1L;
	private ApiStackUnknownError error;

	public ApiStackUnknownException(ApiStackUnknownError error) {
		this.error = error;
	}

	public ApiStackUnknownError getError() {
		return this.error;
	}

}
