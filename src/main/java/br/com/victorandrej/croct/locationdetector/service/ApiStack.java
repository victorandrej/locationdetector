package br.com.victorandrej.croct.locationdetector.service;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
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

	public <T> T call(String url, Class<T> typeclass) throws IOException, ApiStackConsumptionException, ApiStackUnknownException {
		StringBuilder sb = new StringBuilder();
		sb.append(apiUrl);
		sb.append(url);
		sb.append("?access_key=");
		sb.append(acessKey);

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet(sb.toString());
		
		HttpResponse response = client.execute(get);
		StatusLine statusLine = response.getStatusLine();
		
		HttpEntity entity = response.getEntity();
		Optional<Header> contentType = Optional.ofNullable(entity.getContentType());
		String content = EntityUtils.toString(entity);
		
		ObjectMapper jsonMapper = new ObjectMapper();
		
		if (!(statusLine.getStatusCode() == HttpStatus.SC_OK && contentType.isPresent()
				&& contentType.get().getValue().equals(ContentType.APPLICATION_JSON.getMimeType()))) {
			throw new IOException(content);
		}

		try {
			return jsonMapper.readValue(content, typeclass);
		} catch (JsonMappingException ex) {
			try {
				throw new ApiStackConsumptionException(jsonMapper.readValue(content, ApiStackError.class));
			} catch (JsonMappingException err) {
				//neste ponto a api retorna um json diferente do erro em documentacao, exemplo em teste
				throw new ApiStackUnknownException(jsonMapper.readValue(content, ApiStackUnknownError.class));
			}
		}
	}

}
