package br.com.victorandrej.locationdetector;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.victorandrej.croct.locationdetector.service.kafka.KafConsumer;
import br.com.victorandrej.croct.locationdetector.service.kafka.exception.KafConsumerStoppedException;

/**
 * 
 * @author victor
 *
 */
class KafConsumerTest {
	KafkaProducer<String, String> producer;

	Properties consumerPropeties;

	@BeforeEach
	void inicio() {
		Properties prop = new Properties();
		prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producer = new KafkaProducer<>(prop);

		consumerPropeties = new Properties();
		consumerPropeties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		consumerPropeties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerPropeties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				StringDeserializer.class.getName());
		consumerPropeties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());
	}

	@Test
	void pararApenasAposReceberTodasAsSolicitacoesTest() throws InterruptedException, ExecutionException {
		int quantidadeEsperada = 20;
		for (int i = 0; i < quantidadeEsperada; i++)
			producer.send(new ProducerRecord<String, String>("CONSUMER_TEST", "Chave", "Valor")).get();
		;

		Wrapper<Integer> contador = new Wrapper<Integer>();
		contador.value = 0;

		KafConsumer<String, String> kafConsumer = new KafConsumer<>(consumerPropeties, Arrays.asList("CONSUMER_TEST"),
				(r) -> {
					contador.value++;
				});

		Thread t = new Thread(kafConsumer);
		t.start();
		Thread.sleep(1000);
		kafConsumer.stop();
		t.join();
		
		Assert.assertEquals(quantidadeEsperada, contador.value.intValue());

	}

	@Test
	void naoIniciarAposParadoTest() {
		Assert.assertThrows(KafConsumerStoppedException.class, () -> {
			try (var kafConsumer = new KafConsumer<>(consumerPropeties, Arrays.asList("CONSUMER_TEST"), (r) -> {
			})) {
				kafConsumer.stop();
				kafConsumer.run();
			}
		});
	}

	class Wrapper<T> {
		public volatile T value;
	}

}
