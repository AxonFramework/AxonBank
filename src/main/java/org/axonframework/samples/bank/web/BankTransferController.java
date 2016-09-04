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
import org.axonframework.samples.bank.api.banktransfer.CreateBankTransferCommand;
import org.axonframework.samples.bank.query.banktransfer.BankTransferEntry;
import org.axonframework.samples.bank.query.banktransfer.BankTransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/bank-transfers")
public class BankTransferController {

    private CommandBus commandBus;
    private BankTransferRepository bankTransferRepository;

    @Autowired
    public BankTransferController(CommandBus commandBus,
                                  BankTransferRepository bankTransferRepository) {
        this.commandBus = commandBus;
        this.bankTransferRepository = bankTransferRepository;
    }

    @GetMapping
    public Iterable<BankTransferEntry> all() {
        return bankTransferRepository.findAll();
    }

    @GetMapping("/{id}")
    public BankTransferEntry get(@PathVariable String id) {
        return bankTransferRepository.findOne(id);
    }

    @PostMapping
    public void create(@RequestParam("source") String sourceBankAccountId,
                       @RequestParam("destination") String destinationBankAccountId,
                       @RequestParam("amount") long amount) {
        CreateBankTransferCommand command = new CreateBankTransferCommand(UUID.randomUUID().toString(),
                                                                          sourceBankAccountId,
                                                                          destinationBankAccountId,
                                                                          amount);
        commandBus.dispatch(GenericCommandMessage.asCommandMessage(command));
    }
}
