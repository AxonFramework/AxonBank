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

package org.axonframework.samples.bank.web;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.samples.bank.api.bankaccount.CreateBankAccountCommand;
import org.axonframework.samples.bank.query.bankaccount.BankAccountEntry;
import org.axonframework.samples.bank.query.bankaccount.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/bank-accounts")
public class BankAccountController {

    private CommandBus commandBus;
    private BankAccountRepository bankAccountRepository;

    @Autowired
    public BankAccountController(CommandBus commandBus,
                                 BankAccountRepository bankAccountRepository) {
        this.commandBus = commandBus;
        this.bankAccountRepository = bankAccountRepository;
    }

    @GetMapping
    public Iterable<BankAccountEntry> all() {
        return bankAccountRepository.findAll();
    }

    @GetMapping("/{id}")
    public BankAccountEntry get(@PathVariable String id) {
        return bankAccountRepository.findOne(id);
    }

    @PostMapping
    public String create(@RequestParam(required = false, defaultValue = "0") Long overdraftLimit) {
        String id = UUID.randomUUID().toString();
        CreateBankAccountCommand command = new CreateBankAccountCommand(id, overdraftLimit);
        commandBus.dispatch(GenericCommandMessage.asCommandMessage(command));

        return id;
    }
}
