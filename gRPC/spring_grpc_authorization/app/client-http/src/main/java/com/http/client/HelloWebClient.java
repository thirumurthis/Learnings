package com.http.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;


@HttpExchange("http://service-http.local:9091")
public interface HelloWebClient {

    @GetExchange("/")
    String getPongData();
}
