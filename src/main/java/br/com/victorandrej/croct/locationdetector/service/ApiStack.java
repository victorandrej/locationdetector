package br.com.victorandrej.croct.locationdetector.service;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.victorandrej.croct.locationdetector.service.apistack.error.record.ApiStackError;
import br.com.victorandrej.croct.locationdetector.service.apistack.error.record.ApiStackUnknownError;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackConsumptionException;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackUnknownException;

public class ApiStack {
	private String apiUrl = "api.ipstack.com/";
	private String acessKey;

	public ApiStack(String acessKey, boolean useHttps) {
		this.apiUrl = (useHttps ? "https://" : "http://") + apiUrl;
		this.acessKey = acessKey;
	}


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

		Optional<T> result = deserialize(typeclass, content);

		if (result.isPresent())
			return result.get();

		Optional<ApiStackError> stackError = deserialize(ApiStackError.class, content);

		if (stackError.isPresent())
			throw new ApiStackConsumptionException(stackError.get());

		// a api pode retornar um json diferente do erro em documentacao, mas com estado ok, exemplo em teste
		Optional<ApiStackUnknownError> unknowError = deserialize(ApiStackUnknownError.class, content);

		if (unknowError.isPresent())
			throw new ApiStackUnknownException(unknowError.get());

		throw new IOException(content);

	}

	private <T> Optional<T> deserialize(Class<T> classType, String content) {
		try {
			return Optional.of(new ObjectMapper().readValue(content, classType));
		} catch (Throwable e) {
			return Optional.ofNullable(null);
		}
	}

}
