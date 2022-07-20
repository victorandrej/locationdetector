package br.com.victorandrej.croct.locationdetector.service.apistack.error.record;




public record ApiStackError(String success, Error error) {

	public record Error(int code, String type, String info) {
	}
}
