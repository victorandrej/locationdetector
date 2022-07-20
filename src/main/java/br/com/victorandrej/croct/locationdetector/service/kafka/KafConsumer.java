package br.com.victorandrej.croct.locationdetector.service.kafka;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import br.com.victorandrej.croct.locationdetector.service.kafka.exception.KafConsumerStoppedException;

public class KafConsumer<K, V> implements Closeable, Runnable {
	private KafkaConsumer<K, V> kafConsumer;
	private Consumer<V> consumer;
	private volatile boolean stopped = false;

	public KafConsumer(Properties properties, Collection<String>topics,Consumer<V> consumer) {
		this.kafConsumer = new KafkaConsumer<K, V>(properties);
		this.kafConsumer.subscribe(topics);
		this.consumer = consumer;
	}

	/**
	 * Para o consumer, esse metodo e ThreadSafe
	 */
	public void stop() {
		if (this.stopped)
			this.kafConsumer.close();
		this.stopped = true;
	}
		
	/***
	 * Fecha o consumer, o mesmo que chamar {@link #stop() Stop}
	 */
	@Override
	public void close() throws IOException {
		this.stop();
	}

	@Override
	public void run() {
		if (this.stopped)
			throw new KafConsumerStoppedException("Consumer esta parado");

		while (true) {
			if (this.stopped) {
				this.stop();
				return;
			}

			var records = this.kafConsumer.poll(Duration.ofMillis(100));

			if (records.isEmpty())
				continue;

			for (var record : records)
				consumer.accept(record.value());
		}
	}
}
