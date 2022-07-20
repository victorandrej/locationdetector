package br.com.victorandrej.croct.locationdetector.service.apistack.exception;

import br.com.victorandrej.croct.locationdetector.service.apistack.error.record.ApiStackError;

public class ApiStackConsumptionException extends Exception {

	private static final long serialVersionUID = 1L;
	private ApiStackError error;

	public ApiStackConsumptionException(ApiStackError error) {
		this.error = error;
	}



	public ApiStackError getError() {
		return this.error;
	}
}
