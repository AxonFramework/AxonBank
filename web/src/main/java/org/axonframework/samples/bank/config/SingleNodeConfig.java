/*
 * Copyright (c) 2016. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.samples.bank.config;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.eventhandling.saga.repository.SagaStore;
import org.axonframework.eventhandling.saga.repository.inmemory.InMemorySagaStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!distributed-command-bus")
public class SingleNodeConfig {

    @Bean
    public CommandBus simpleCommandBus() {
        SimpleCommandBus simpleCommandBus = new SimpleCommandBus();
        simpleCommandBus.registerDispatchInterceptor(new BeanValidationInterceptor<>());

        return simpleCommandBus;
    }

    // We're using Axon Framework's Spring Boot support therefore Axon Framework will create JpaEventStorageEngine and
    // JpaSagaStore beans if EntityManagerFactory is on the classpath. EntityManagerFactory is on the classpath, because
    // we're using JPA for the query side of Axon Bank. In order to use InMemoryEventStorageEngine and InMemorySagaStore
    // we need to create the beans ourselves.
    @Bean
    public EventStorageEngine eventStorageEngine() {
        return new InMemoryEventStorageEngine();
    }

    @Bean
    public SagaStore sagaStore() {
        return new InMemorySagaStore();
    }
}
