package com.abz.library.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.abz.library.borrowing.BorrowingProperties;
import com.abz.library.reservation.ReservationProperties;

@Configuration 
@EnableConfigurationProperties ({BorrowingProperties.class, ReservationProperties.class})
public class EnablePropertiesConfig {

}
