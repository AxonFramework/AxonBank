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
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.saga.repository.SagaStore;
import org.axonframework.eventhandling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.monitoring.NoOpMessageMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.PropertyVetoException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Configuration
@EntityScan({"org.axonframework.samples.bank.query",
        "org.axonframework.eventsourcing.eventstore.jpa",
        "org.axonframework.eventhandling.saga.repository.jpa"})
@Profile("distributed-command-bus")
public class DistributedCommandBusConfig {

    @Autowired
    private TransactionManager transactionManager;
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public CommandBus localSegment() {
        SimpleCommandBus localSegment = new SimpleCommandBus(transactionManager, NoOpMessageMonitor.INSTANCE);
        localSegment.registerDispatchInterceptor(new BeanValidationInterceptor<>());

        return localSegment;
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws PropertyVetoException {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public SagaStore<Object> jpaSagaStore(EntityManagerProvider entityManagerProvider) {
        return new JpaSagaStore(entityManagerProvider);
    }
}
