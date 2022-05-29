package com.mudivili.securitysslclient;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class ConsumingRestApplication {

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${trust-store}")
    private String trustStoreFileName;

    @Value("${trust-store-password}")
    private String trustStorePassword;

    private static final Logger log = LoggerFactory.getLogger(ConsumingRestApplication.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Resource trustStore = resourceLoader.getResource("classpath:" + trustStoreFileName);

        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(trustStore.getFile(), trustStorePassword.toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();

        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {
            Health health = restTemplate.getForObject(
                    "https://tw-laptop.mudivili.com:8443/actuator/health", Health.class);
            log.info(health.toString());
        };
    }
}