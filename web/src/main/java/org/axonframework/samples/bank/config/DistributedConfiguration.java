package org.axonframework.samples.bank.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("distributed")
@EnableDiscoveryClient
@Configuration
public class DistributedConfiguration {

}
