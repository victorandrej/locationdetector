package br.com.victorandrej.croct.locationdetector.service;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.victorandrej.croct.locationdetector.service.apistack.error.record.ApiStackError;
import br.com.victorandrej.croct.locationdetector.service.apistack.error.record.ApiStackUnknownError;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackConsumptionException;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackUnknownException;
import br.com.victorandrej.croct.locationdetector.service.interfaces.ThrowableFunction;

public class ApiStack {
	private String apiUrl = "api.ipstack.com/";
	private String acessKey;

	public ApiStack(String acessKey, boolean useHttps) {
		this.apiUrl = (useHttps ? "https://" : "http://") + apiUrl;
		this.acessKey = acessKey;
	}

	@SuppressWarnings("unchecked")
	public <T> T call(String url, Class<T> typeclass)
			throws IOException, ApiStackConsumptionException, ApiStackUnknownException {
		StringBuilder sb = new StringBuilder();
		sb.append(apiUrl);
		sb.append(url);
		sb.append("?access_key=");
		sb.append(acessKey);

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet(sb.toString());
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		
		String content = EntityUtils.toString(entity);
		
		ObjectMapper jsonMapper = new ObjectMapper();

		Object result = tryHidder(() -> new ApiStackConsumptionException(jsonMapper.readValue(content, ApiStackError.class)));
		
		if (result instanceof ApiStackConsumptionException)
			throw (ApiStackConsumptionException) result;
		
		// a api pode retornar um json diferente do erro em documentacao, mas com estado
		// ok, exemplo em teste
		result = tryHidder(() -> new ApiStackUnknownException(jsonMapper.readValue(content, ApiStackUnknownError.class)));
		
		if (result instanceof ApiStackUnknownException)
			throw (ApiStackUnknownException) result;

		result = tryHidder(() -> jsonMapper.readValue(content, typeclass));
		
		if (result instanceof JsonMappingException)
			throw new IOException(content);

		return (T) result;

	}

	private Object tryHidder(ThrowableFunction f) {
		try {
			return f.execute();
		} catch (Throwable e) {
			return e;
		}
	}

}
