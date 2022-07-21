package br.com.victorandrej.croct.locationdetector.service.kafka;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import br.com.victorandrej.croct.locationdetector.service.kafka.enums.ConsumerStatus;
import br.com.victorandrej.croct.locationdetector.service.kafka.exception.KafConsumerDeadException;
import br.com.victorandrej.croct.locationdetector.service.kafka.exception.StopConsumerException;

public class KafConsumer<K, V> implements Closeable, Runnable {
	private KafkaConsumer<K, V> kafConsumer;
	private Consumer<V> consumer;
	private ConsumerStatus status;

	public KafConsumer(Properties properties, Collection<String> topics, Consumer<V> consumer) {
		this.kafConsumer = new KafkaConsumer<K, V>(properties);
		this.kafConsumer.subscribe(topics);
		this.consumer = consumer;
		this.status = ConsumerStatus.CREATED;
	}

	/**
	 * Para o consumer, esse metodo e ThreadSafe
	 */
	public void stop() {
		this.status = ConsumerStatus.STOPPING;
	}

	public ConsumerStatus getStatus() {
		return this.status;
	}

	/***
	 * Fecha o consumer, o mesmo que chamar {@link #stop() Stop}
	 */
	@Override
	public void close() throws IOException {
		this.releaseResources();
	}

	private void releaseResources() {
		if (this.status.equals(ConsumerStatus.STOPPED))
			this.kafConsumer.close();
	}

	@Override
	public void run() {
		if (this.status.equals(ConsumerStatus.DEAD))
			throw new KafConsumerDeadException("Consumer esta morto");

		this.status = ConsumerStatus.RUNNING;

		try {
			loop();
		} catch (StopConsumerException e) {
			this.status = ConsumerStatus.STOPPED;
			this.releaseResources();
			this.status = ConsumerStatus.DEAD;
		}
	}

	private void hasStopRequest() throws StopConsumerException {
		if (this.status.equals(ConsumerStatus.STOPPING))
			throw new StopConsumerException();

	}

	private void loop() throws StopConsumerException {
		while (true) {
			var records = this.kafConsumer.poll(Duration.ofMillis(100));

			hasStopRequest();

			for (var record : records) {
				hasStopRequest();
				consumer.accept(record.value());
			}
		}
	}

}
