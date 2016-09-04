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

package org.axonframework.samples.bank.command;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.samples.bank.api.bankaccount.BankAccountCreatedEvent;
import org.axonframework.samples.bank.api.bankaccount.CreateBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.CreditDestinationBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DebitSourceBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DepositMoneyCommand;
import org.axonframework.samples.bank.api.bankaccount.DestinationBankAccountCreditedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneyAddedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneyDepositedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneyOfFailedBankTransferReturnedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneySubtractedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneyWithdrawnEvent;
import org.axonframework.samples.bank.api.bankaccount.ReturnMoneyOfFailedBankTransferCommand;
import org.axonframework.samples.bank.api.bankaccount.SourceBankAccountDebitedEvent;
import org.axonframework.samples.bank.api.bankaccount.WithdrawMoneyCommand;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@AggregateRoot
public class BankAccount {

    @AggregateIdentifier
    private String id;
    private long overdraftLimit;
    private long balanceInCents;

    @SuppressWarnings("unused")
    private BankAccount() {
    }

    @CommandHandler
    public BankAccount(CreateBankAccountCommand command) {
        apply(new BankAccountCreatedEvent(command.getBankAccountId(), command.getOverdraftLimit()));
    }

    @CommandHandler
    public void handle(DepositMoneyCommand command) {
        apply(new MoneyDepositedEvent(command.getBankAccountId(), command.getAmountOfMoney()));
    }

    @CommandHandler
    public void handle(WithdrawMoneyCommand command) {
        if (command.getAmountOfMoney() <= balanceInCents + overdraftLimit) {
            apply(new MoneyWithdrawnEvent(command.getBankAccountId(), command.getAmountOfMoney()));
        }
    }

    @CommandHandler
    public boolean handle(DebitSourceBankAccountCommand command) {
        if (command.getAmount() <= balanceInCents + overdraftLimit) {
            apply(new SourceBankAccountDebitedEvent(command.getBankAccountId(),
                                                    command.getAmount(),
                                                    command.getBankTransferId()));
            return true;
        }
        return false;
    }

    @CommandHandler
    public void handle(CreditDestinationBankAccountCommand command) {
        apply(new DestinationBankAccountCreditedEvent(command.getBankAccountId(),
                                                      command.getAmount(),
                                                      command.getBankTransferId()));
    }

    @CommandHandler
    public void handle(ReturnMoneyOfFailedBankTransferCommand command) {
        apply(new MoneyOfFailedBankTransferReturnedEvent(command.getId(), command.getAmount()));
    }

    @EventHandler
    public void on(BankAccountCreatedEvent event) {
        this.id = event.getId();
        this.overdraftLimit = event.getOverdraftLimit();
        this.balanceInCents = 0;
    }

    @EventHandler
    public void on(MoneyAddedEvent event) {
        balanceInCents += event.getAmount();
    }

    @EventHandler
    public void on(MoneySubtractedEvent event) {
        balanceInCents -= event.getAmount();
    }
}