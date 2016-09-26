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

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SimpleEventHandlerInvoker;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.samples.bank.query.bankaccount.BankAccountEventListener;
import org.axonframework.samples.bank.query.banktransfer.BankTransferEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryConfig {

    @Autowired
    private EventBus eventBus;

    @Autowired
    private BankAccountEventListener bankAccountEventListener;
    @Autowired
    private BankTransferEventListener bankTransferEventListener;

    @Bean
    public EventProcessor eventProcessor() {
        EventProcessor eventProcessor = new SubscribingEventProcessor("eventProcessor",
                                                                      new SimpleEventHandlerInvoker(
                                                                              bankAccountEventListener,
                                                                              bankTransferEventListener),
                                                                      eventBus);
        eventProcessor.start();

        return eventProcessor;
    }
}
