package br.com.victorandrej.locationdetector;

import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.victorandrej.croct.locationdetector.record.Location;
import br.com.victorandrej.croct.locationdetector.service.ApiStack;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackConsumptionException;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackUnknownException;

class ApiStackTest {
	
	ApiStack apiStack;

	@BeforeEach
	void iniciarApiStack() {
		this.apiStack = new ApiStack(ApiToken.TOKEN, false);
	}

	@Test
	void ipInexistenteTest() {
		Assert.assertThrows(ApiStackConsumptionException.class, () -> {
			apiStack.call("awdodwa.com.br", Location.class);
		});
	}
	@Test
	void erroDesconhecidoTest() {
		Assert.assertThrows(ApiStackUnknownException.class, () -> {
			apiStack.call("http://www.google.com.br", Location.class);
		});
	}
	
	@Test
	void ipExistente() throws IOException, ApiStackConsumptionException, ApiStackUnknownException {
	  apiStack.call("www.google.com.br", Location.class);
	}
		
}
