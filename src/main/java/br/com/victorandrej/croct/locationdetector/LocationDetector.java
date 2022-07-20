package br.com.victorandrej.croct.locationdetector;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import br.com.victorandrej.croct.locationdetector.enums.ResponseStatus;
import br.com.victorandrej.croct.locationdetector.error.record.ResponseError;
import br.com.victorandrej.croct.locationdetector.exception.ParameterException;
import br.com.victorandrej.croct.locationdetector.record.Location;
import br.com.victorandrej.croct.locationdetector.record.Request;
import br.com.victorandrej.croct.locationdetector.record.Response;
import br.com.victorandrej.croct.locationdetector.record.TopicResponse;
import br.com.victorandrej.croct.locationdetector.service.ApiStack;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackConsumptionException;
import br.com.victorandrej.croct.locationdetector.service.apistack.exception.ApiStackUnknownException;
import br.com.victorandrej.croct.locationdetector.service.kafka.KafConsumer;
import br.com.victorandrej.croct.locationdetector.util.PropertiesUtils;

class LocationDetector implements Consumer<Request> {

	public static void main(String[] args) throws Exception {
		Optional<String> acessKey = Optional.ofNullable(System.getProperty("acessKey"));
		Optional<String> server = Optional.ofNullable(System.getProperty("server"));
		Optional<String> topic = Optional.ofNullable(System.getProperty("topic"));
		String useHttps = System.getProperty("https", "true");
		String groupId = System.getProperty("groupId", "LOCATION_DETECTOR");
		int maxCacheableIps = Integer.parseInt(System.getProperty("ipCache", "1000"));
		int cacheMinuteTimeout = Integer.parseInt(System.getProperty("cacheTimeout", "30"));

		if (acessKey.isEmpty())
			throw new ParameterException("chave de acesso nao informada");
		if (server.isEmpty())
			throw new ParameterException("servidor nao informado");
		if (topic.isEmpty())
			throw new ParameterException("topico nao informado");

		LocationDetector detector = new LocationDetector(acessKey.get(), server.get(), topic.get(), useHttps, groupId,
				maxCacheableIps, cacheMinuteTimeout);

		Properties properties = PropertiesUtils.createConsumerProperties(server.get(), groupId);
		try (KafConsumer<String, Request> consumer = new KafConsumer<>(properties, Arrays.asList(topic.get()),detector)) {
			consumer.run();
		}

	}

	private String acessKey;
	private String topic;
	private String useHttps;
	private int cacheMinuteTimeout;
	private Cache<Object, Object> clientCache;
	private KafkaProducer<String, Object> producer;

	public LocationDetector(String acessKey, String server, String topic, String useHttps, String groupId,
			int maxCacheableIps, int cacheMinuteTimeout) {
		this.acessKey = acessKey;
		this.topic = topic;
		this.useHttps = useHttps;
		this.cacheMinuteTimeout = cacheMinuteTimeout;
		this.clientCache = CacheBuilder.newBuilder().maximumSize(maxCacheableIps).build();
		Properties prducerPropeties = PropertiesUtils.createProducerProperties(server);
		this.producer = new KafkaProducer<>(prducerPropeties);
	}

	@Override
	public void accept(Request request) {
		Optional<Response> inCacheRequest = Optional.ofNullable((Response) clientCache.getIfPresent(request));

		if (inCacheRequest.isPresent() && this.isValidCache(inCacheRequest.get())) {
			producer.send(new ProducerRecord<String, Object>(topic, request.userId(),
					new TopicResponse(ResponseStatus.OK, inCacheRequest.get())));
			return;
		} else if (inCacheRequest.isPresent()) {
			clientCache.invalidate(request);
		}

		ApiStack apiStack = new ApiStack(acessKey, Boolean.parseBoolean(useHttps));

		try {
			Response response = new Response(request.userId(), request.timeSamp(), request.userId(),
					apiStack.call(request.ip(), Location.class));

			clientCache.put(request, response);

			producer.send(new ProducerRecord<String, Object>(topic, request.userId(),
					new TopicResponse(ResponseStatus.OK, response))).get();

		} catch (Exception e) {
			catchError(e, producer, request.userId(), topic);
		}

	}

	private boolean isValidCache(Response cache) {
		LocalDateTime inCacheTime = Timestamp.from(Instant.ofEpochMilli(cache.timeStamp())).toLocalDateTime();
		LocalDateTime nowTime = LocalDateTime.now();
		
		return inCacheTime.plusMinutes(cacheMinuteTimeout).isAfter(nowTime);
	}

	private void catchError(Exception e, KafkaProducer<String, Object> producer, String userId, String topic) {

		if (e instanceof ApiStackConsumptionException) {
			producer.send(new ProducerRecord<String, Object>(topic, userId, new TopicResponse(ResponseStatus.ERROR,
					new ResponseError(((ApiStackConsumptionException) e).getError().error().info()))));
		} else if (e instanceof ApiStackUnknownException) {
			producer.send(new ProducerRecord<String, Object>(topic, userId, new TopicResponse(ResponseStatus.ERROR,
					new ResponseError(((ApiStackUnknownException) e).getError().detail()))));
		} else {
			producer.send(new ProducerRecord<String, Object>(topic, userId,
					new TopicResponse(ResponseStatus.ERROR, new ResponseError(e.getMessage()))));
		}
	}
}
