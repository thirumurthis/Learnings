package com.spring.grpc.client;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebProtoConfig extends WebMvcConfigurationSupport {

    //Below configuration is used to convert the protobuf to json
    @Bean
    public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        ProtobufHttpMessageConverter protobufHttpMessageConverter = new ProtobufHttpMessageConverter();
        List<MediaType> converterList = new ArrayList<>();
        converterList.add(MediaType.APPLICATION_JSON);
        converterList.add(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=ISO-8859-1"));
        protobufHttpMessageConverter.setSupportedMediaTypes(converterList);
        return protobufHttpMessageConverter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, protobufHttpMessageConverter());
    }
}
