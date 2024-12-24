//package com.eatbook.backoffice.global.config;
//
//import com.eatbook.backoffice.domain.episode.dto.FileUploadRequest;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//
//@Configuration
//@EnableKafka
//public class KafkaConsumerConfig {
//
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, FileUploadRequest> fileUploadListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, FileUploadRequest> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        factory.setConcurrency(3); // 동시성 설정 (예: 3개의 쓰레드)
//        factory.getContainerProperties().setPollTimeout(3000);
//        return factory;
//    }
//
//    @Bean
//    public org.springframework.kafka.core.ConsumerFactory<String, FileUploadRequest> consumerFactory() {
//        JsonDeserializer<FileUploadRequest> deserializer = new JsonDeserializer<>(FileUploadRequest.class);
//        deserializer.addTrustedPackages("*");
//
//        return new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(
//                consumerConfigs(),
//                new StringDeserializer(),
//                deserializer
//        );
//    }
//
//    @Bean
//    public java.util.Map<String, Object> consumerConfigs() {
//        java.util.Map<String, Object> props = new java.util.HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "file-upload-group");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        return props;
//    }
//}