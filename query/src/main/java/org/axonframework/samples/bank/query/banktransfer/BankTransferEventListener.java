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

package org.axonframework.samples.bank.query.banktransfer;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCompletedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCreatedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BankTransferEventListener {

    private BankTransferRepository repository;

    @Autowired
    public BankTransferEventListener(BankTransferRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(BankTransferCreatedEvent event) {
        repository.save(new BankTransferEntry(event.getBankTransferId(),
                                              event.getSourceBankAccountId(),
                                              event.getDestinationBankAccountId(),
                                              event.getAmount()));
    }

    @EventHandler
    public void on(BankTransferFailedEvent event) {
        BankTransferEntry bankTransferEntry = repository.findOne(event.getBankTransferId());
        bankTransferEntry.setStatus(BankTransferEntry.Status.FAILED);

        repository.save(bankTransferEntry);
    }

    @EventHandler
    public void on(BankTransferCompletedEvent event) {
        BankTransferEntry bankTransferEntry = repository.findOne(event.getBankTransferId());
        bankTransferEntry.setStatus(BankTransferEntry.Status.COMPLETED);

        repository.save(bankTransferEntry);
    }


}
