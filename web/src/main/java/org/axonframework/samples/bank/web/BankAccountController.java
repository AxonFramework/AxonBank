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
import org.axonframework.commandhandling.callbacks.FutureCallback;
import org.axonframework.samples.bank.api.bankaccount.CreateBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DepositMoneyCommand;
import org.axonframework.samples.bank.api.bankaccount.WithdrawMoneyCommand;
import org.axonframework.samples.bank.query.bankaccount.BankAccountEntry;
import org.axonframework.samples.bank.query.bankaccount.BankAccountRepository;
import org.axonframework.samples.bank.web.dto.BankAccountDto;
import org.axonframework.samples.bank.web.dto.DepositDto;
import org.axonframework.samples.bank.web.dto.WithdrawalDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.io.net.http.model.Status;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@Controller
@MessageMapping("/bank-accounts")
public class BankAccountController {

    private CommandBus commandBus;
    private BankAccountRepository bankAccountRepository;

    @Autowired
    public BankAccountController(CommandBus commandBus,
                                 BankAccountRepository bankAccountRepository) {
        this.commandBus = commandBus;
        this.bankAccountRepository = bankAccountRepository;
    }

    @SubscribeMapping
    public Iterable<BankAccountEntry> all() {
        return bankAccountRepository.findAllByOrderByIdAsc();
    }

    @SubscribeMapping("/{id}")
    public BankAccountEntry get(@DestinationVariable String id) {
        return bankAccountRepository.findOne(id);
    }

    @MessageMapping("/create")
    public void create(BankAccountDto bankAccountDto) {
        String id = UUID.randomUUID().toString();
        CreateBankAccountCommand command = new CreateBankAccountCommand(id, bankAccountDto.getOverdraftLimit());
        commandBus.dispatch(asCommandMessage(command));
    }

    @MessageMapping("/withdraw")
    public void withdraw(WithdrawalDto depositDto) {
        WithdrawMoneyCommand command = new WithdrawMoneyCommand(depositDto.getBankAccountId(), depositDto.getAmount());
        commandBus.dispatch(asCommandMessage(command));
    }

    @MessageMapping("/deposit")
    public void deposit(DepositDto depositDto) {
        DepositMoneyCommand command = new DepositMoneyCommand(depositDto.getBankAccountId(), depositDto.getAmount());
        commandBus.dispatch(asCommandMessage(command));
    }

    @PostMapping("/update")
    public CompletableFuture<Response> handle(UpdateJobCategoryPayload payload) {

        FutureCallback<UpdateJobCategory, Void> cb = new FutureCallback<>();

        commandBus.dispatch(asCommandMessage(payload.asCommand()), cb);
        return cb.thenApply(r -> Response.ok().build())
                .exceptionally(e -> Response.status(Status.NOT_FOUND).build());
    }

    public static class Response {
        public static Response ok() {
            return new Response();
        }

        public Response build() {
            return this;
        }

        public static Response serverError() {
            return new Response();
        }

        public static Response status(Status notFound) {
            return new Response();
        }
    }

    public static class UpdateJobCategoryPayload {
        public Object asCommand() {
            return null;

        }
    }

    private class UpdateJobCategory {
    }
}
