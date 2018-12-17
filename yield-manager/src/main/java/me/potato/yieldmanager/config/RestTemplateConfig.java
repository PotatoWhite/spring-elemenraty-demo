package me.potato.yieldmanager.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate() {

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setReadTimeout(5000);
		requestFactory.setConnectTimeout(3000);

		RestTemplate restTemplate = new RestTemplate(requestFactory);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

		converter.setSupportedMediaTypes(
				Arrays.asList(
						MediaType.APPLICATION_JSON,
						MediaType.APPLICATION_JSON_UTF8
				)
		);

		restTemplate.setMessageConverters(Arrays.asList(converter, new FormHttpMessageConverter()));
		return restTemplate;
	}
}
