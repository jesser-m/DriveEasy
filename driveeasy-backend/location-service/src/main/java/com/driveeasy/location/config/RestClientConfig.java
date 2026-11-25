package com.driveeasy.location.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient.Builder charge-equilibre : les URI http://nom-du-service sont resolues via Eureka.
 * Les customizers Spring Boot sont appliques pour conserver le tracage Zipkin (propagation du traceId).
 * JdkClientHttpRequestFactory est necessaire car HttpURLConnection ne supporte pas PATCH.
 */
@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder(ObjectProvider<RestClientCustomizer> customizers,
                                                     @Value("${driveeasy.services.timeout-ms:3000}") int timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        RestClient.Builder builder = RestClient.builder().requestFactory(factory);
        customizers.orderedStream().forEach(c -> c.customize(builder));
        return builder;
    }
}
