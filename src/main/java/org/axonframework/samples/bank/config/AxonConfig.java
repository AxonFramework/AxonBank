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

import org.axonframework.commandhandling.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.AnnotationCommandHandlerAdapter;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SimpleEventHandlerInvoker;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.saga.AnnotatedSagaManager;
import org.axonframework.eventhandling.saga.ResourceInjector;
import org.axonframework.eventhandling.saga.SagaRepository;
import org.axonframework.eventhandling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.eventhandling.saga.repository.SagaStore;
import org.axonframework.eventhandling.saga.repository.inmemory.InMemorySagaStore;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.samples.bank.command.BankAccount;
import org.axonframework.samples.bank.command.BankAccountCommandHandler;
import org.axonframework.samples.bank.command.BankTransfer;
import org.axonframework.samples.bank.command.BankTransferManagementSaga;
import org.axonframework.samples.bank.query.bankaccount.BankAccountEventListener;
import org.axonframework.samples.bank.query.banktransfer.BankTransferEventListener;
import org.axonframework.spring.saga.SpringResourceInjector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Autowired
    private BankAccountEventListener bankAccountEventListener;
    @Autowired
    private BankTransferEventListener bankTransferEventListener;

    @Bean
    public CommandBus commandBus() {
        return new SimpleCommandBus();
    }

    @Bean
    public EventStore eventStore() {
        return new EmbeddedEventStore(eventStorageEngine());
    }

    @Bean
    public EventStorageEngine eventStorageEngine() {
        return new InMemoryEventStorageEngine();
    }

    @Bean
    public BankAccountCommandHandler bankAccountCommandHandler() {
        return new BankAccountCommandHandler(bankAccountEventSourcingRepository(), eventStore());
    }

    @Bean
    public AnnotationCommandHandlerAdapter annotationBankAccountCommandHandler() {
        AnnotationCommandHandlerAdapter annotationCommandHandlerAdapter = new AnnotationCommandHandlerAdapter(
                bankAccountCommandHandler());
        annotationCommandHandlerAdapter.subscribe(commandBus());

        return annotationCommandHandlerAdapter;
    }

    @Bean
    public AggregateAnnotationCommandHandler<BankTransfer> bankTransferCommandHandler() {
        AggregateAnnotationCommandHandler<BankTransfer> commandHandler = new AggregateAnnotationCommandHandler<>(
                BankTransfer.class,
                bankTransferEventSourcingRepository());
        commandHandler.subscribe(commandBus());

        return commandHandler;
    }

    @Bean
    public Repository<BankAccount> bankAccountEventSourcingRepository() {
        return new EventSourcingRepository<>(BankAccount.class, eventStore());
    }

    @Bean
    public Repository<BankTransfer> bankTransferEventSourcingRepository() {
        return new EventSourcingRepository<>(BankTransfer.class, eventStore());
    }

    @Bean
    public SagaRepository<BankTransferManagementSaga> sagaRepository() {
        return new AnnotatedSagaRepository<>(BankTransferManagementSaga.class,
                                             sagaStore(),
                                             resourceInjector());
    }

    @Bean
    public ResourceInjector resourceInjector() {
        return new SpringResourceInjector();
    }

    @Bean
    public AnnotatedSagaManager<BankTransferManagementSaga> annotatedSagaManager() {
        return new AnnotatedSagaManager<>(BankTransferManagementSaga.class, sagaRepository());
    }

    @Bean
    public EventProcessor sagaEventProcessor() {
        SubscribingEventProcessor sagaEventProcessor = new SubscribingEventProcessor("sagaEventProcessor",
                                                                                     annotatedSagaManager(),
                                                                                     eventStore());
        sagaEventProcessor.start();

        return sagaEventProcessor;
    }

    @Bean
    public SagaStore<Object> sagaStore() {
        return new InMemorySagaStore();
    }

    @Bean
    public EventProcessor eventProcessor() {
        EventProcessor eventProcessor = new SubscribingEventProcessor("eventProcessor",
                                                                      new SimpleEventHandlerInvoker(
                                                                              bankAccountEventListener,
                                                                              bankTransferEventListener),
                                                                      eventStore());
        eventProcessor.start();

        return eventProcessor;
    }
}