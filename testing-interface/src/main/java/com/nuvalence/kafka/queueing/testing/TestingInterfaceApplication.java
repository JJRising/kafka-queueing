package com.nuvalence.kafka.queueing.testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TestingInterfaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestingInterfaceApplication.class, args);
	}

}
