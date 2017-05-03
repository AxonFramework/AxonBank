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

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.samples.bank.api.banktransfer.CreateBankTransferCommand;
import org.axonframework.samples.bank.query.banktransfer.BankTransferEntry;
import org.axonframework.samples.bank.query.banktransfer.BankTransferRepository;
import org.axonframework.samples.bank.web.dto.BankTransferDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@Slf4j
public class BankTransferController {

    private final CommandGateway commandGateway;
    private final BankTransferRepository bankTransferRepository;

    public BankTransferController(CommandGateway commandGateway, BankTransferRepository bankTransferRepository) {
        this.commandGateway = commandGateway;
        this.bankTransferRepository = bankTransferRepository;
    }

    @SubscribeMapping("/bank-accounts/{bankAccountId}/bank-transfers")
    public Iterable<BankTransferEntry> bankTransfers(@DestinationVariable String bankAccountId) {
        log.info("Retrieve bank transfers for bank account with id {}", bankAccountId);
        return bankTransferRepository.findBySourceBankAccountIdOrDestinationBankAccountId(bankAccountId, bankAccountId);
    }

    @MessageMapping("/bank-transfers/{id}")
    public BankTransferEntry get(@DestinationVariable String id) {
        log.info("Retrieve bank transfer with id {}", id);
        return bankTransferRepository.findOne(id);
    }

    @MessageMapping("/bank-transfers/create")
    public void create(BankTransferDto bankTransferDto) {
        log.info("Create bank transfer with payload {}", bankTransferDto);

        String bankTransferId = UUID.randomUUID().toString();
        CreateBankTransferCommand command = new CreateBankTransferCommand(bankTransferId,
                                                                          bankTransferDto.getSourceBankAccountId(),
                                                                          bankTransferDto.getDestinationBankAccountId(),
                                                                          bankTransferDto.getAmount());

        commandGateway.send(command);
    }
}
