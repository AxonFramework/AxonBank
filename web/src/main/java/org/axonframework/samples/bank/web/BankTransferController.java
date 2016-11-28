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
import org.axonframework.samples.bank.web.dto.BankTransferDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Controller
public class BankTransferController {

    private CommandBus commandBus;
    private BankTransferRepository bankTransferRepository;

    @Autowired
    public BankTransferController(CommandBus commandBus,
                                  BankTransferRepository bankTransferRepository) {
        this.commandBus = commandBus;
        this.bankTransferRepository = bankTransferRepository;
    }

    @SubscribeMapping("/bank-accounts/{bankAccountId}/bank-transfers")
    public Iterable<BankTransferEntry> bankTransfers(@DestinationVariable String bankAccountId) {
        return bankTransferRepository.findBySourceBankAccountIdOrDestinationBankAccountId(bankAccountId, bankAccountId);
    }

    @MessageMapping("/bank-transfers/{id}")
    public BankTransferEntry get(@DestinationVariable String id) {
        return bankTransferRepository.findOne(id);
    }

    @MessageMapping("/bank-transfers/create")
    public void create(BankTransferDto bankTransferDto) {
        String bankTransferId = UUID.randomUUID().toString();
        CreateBankTransferCommand command = new CreateBankTransferCommand(bankTransferId,
                                                                          bankTransferDto.getSourceBankAccountId(),
                                                                          bankTransferDto.getDestinationBankAccountId(),
                                                                          bankTransferDto.getAmount());
        commandBus.dispatch(GenericCommandMessage.asCommandMessage(command));
    }
}
