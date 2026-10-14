package com.abz.library.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.abz.library.borrowing.BorrowingProperties;

@Configuration 
@EnableConfigurationProperties (BorrowingProperties.class)
public class EnablePropertiesConfig {

}
