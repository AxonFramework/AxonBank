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

package org.axonframework.samples.bank.query.bankaccount;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.samples.bank.api.bankaccount.BankAccountCreatedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneyAddedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneySubtractedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BankAccountEventListener {

    private BankAccountRepository repository;

    @Autowired
    public void setRepository(BankAccountRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(BankAccountCreatedEvent event) {
        repository.save(new BankAccountEntry(event.getId(), 0, event.getOverdraftLimit()));
    }

    @EventHandler
    public void on(MoneyAddedEvent event) {
        BankAccountEntry bankAccountEntry = repository.findOne(event.getBankAccountId());
        bankAccountEntry.setBalance(bankAccountEntry.getBalance() + event.getAmount());

        repository.save(bankAccountEntry);
    }

    @EventHandler
    public void on(MoneySubtractedEvent event) {
        BankAccountEntry bankAccountEntry = repository.findOne(event.getBankAccountId());
        bankAccountEntry.setBalance(bankAccountEntry.getBalance() - event.getAmount());

        repository.save(bankAccountEntry);
    }

}
