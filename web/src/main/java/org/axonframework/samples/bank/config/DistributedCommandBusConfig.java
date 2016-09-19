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
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.distributed.jgroups.JGroupsConnector;
import org.axonframework.common.jpa.ContainerManagedEntityManagerProvider;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventhandling.saga.repository.SagaStore;
import org.axonframework.eventhandling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.axonframework.spring.commandhandling.distributed.jgroups.JGroupsConnectorFactoryBean;
import org.axonframework.spring.config.TransactionManagerFactoryBean;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.PropertyVetoException;
import java.util.Arrays;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Configuration
@EntityScan({"org.axonframework.samples.bank.query",
        "org.axonframework.eventsourcing.eventstore.jpa",
        "org.axonframework.eventhandling.saga.repository.jpa"})
@Profile("distributed-command-bus")
public class DistributedCommandBusConfig {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public Serializer serializer() {
        return new XStreamSerializer();
    }

    @Bean
    public JGroupsConnectorFactoryBean jGroupsConnectorFactoryBean() throws Exception {
        SimpleCommandBus localSegment = new SimpleCommandBus();
        localSegment.setDispatchInterceptors(Arrays.asList(new BeanValidationInterceptor<>()));
        localSegment.setTransactionManager(springTransactionManager());

        JGroupsConnectorFactoryBean jGroupsConnectorFactoryBean = new JGroupsConnectorFactoryBean();
        jGroupsConnectorFactoryBean.setLocalSegment(localSegment);

        return jGroupsConnectorFactoryBean;
    }

    @Bean
    public CommandBus distributedCommandBus() throws Exception {
        JGroupsConnector jGroupsConnector = jGroupsConnectorFactoryBean().getObject();
        return new DistributedCommandBus(jGroupsConnector, jGroupsConnector);
    }

    @Bean
    public EventStorageEngine eventStorageEngine() throws Exception {
        return new JpaEventStorageEngine(entityManagerProvider(), springTransactionManager());
    }

    @Bean
    public EntityManagerProvider entityManagerProvider() {
        return new ContainerManagedEntityManagerProvider();
    }

    @Bean
    public SpringTransactionManager springTransactionManager() throws Exception {
        return new SpringTransactionManager(transactionManager());
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws PropertyVetoException {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public TransactionManagerFactoryBean transactionManagerFactoryBean() throws PropertyVetoException {
        TransactionManagerFactoryBean factoryBean = new TransactionManagerFactoryBean();
        factoryBean.setTransactionManager(transactionManager());

        return factoryBean;
    }

    @Bean
    public SagaStore<Object> jpaSagaStore() {
        return new JpaSagaStore(entityManagerProvider());
    }
}
