package com.ivankrn.transport_tracker_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class TransportTrackerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportTrackerBotApplication.class, args);
	}

}
