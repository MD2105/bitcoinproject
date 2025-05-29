package com.example.bitcoin_price;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BitcoinPriceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BitcoinPriceApplication.class, args);
		System.out.println("Bitcoin Price Application is running!");
	}

}
