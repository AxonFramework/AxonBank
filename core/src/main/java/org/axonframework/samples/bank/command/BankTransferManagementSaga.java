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

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.samples.bank.api.bankaccount.BankTransferDestinationCreditCommand;
import org.axonframework.samples.bank.api.bankaccount.BankTransferSourceDebitCommand;
import org.axonframework.samples.bank.api.bankaccount.BankTransferDestinationCreditedEvent;
import org.axonframework.samples.bank.api.bankaccount.BankTransferDestinationNotFoundEvent;
import org.axonframework.samples.bank.api.bankaccount.BankTransferSourceReturnMoneyCommand;
import org.axonframework.samples.bank.api.bankaccount.BankTransferSourceDebitRejectedEvent;
import org.axonframework.samples.bank.api.bankaccount.BankTransferSourceDebitedEvent;
import org.axonframework.samples.bank.api.bankaccount.BankTransferSourceNotFoundEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCreatedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferMarkCompletedCommand;
import org.axonframework.samples.bank.api.banktransfer.BankTransferMarkFailedCommand;
import org.axonframework.spring.stereotype.Saga;

import javax.inject.Inject;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@Saga
public class BankTransferManagementSaga {

    private transient CommandBus commandBus;

    @Inject
    public void setCommandBus(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    private String sourceBankAccountId;
    private String destinationBankAccountId;
    private long amount;

    @StartSaga
    @SagaEventHandler(associationProperty = "bankTransferId")
    public void on(BankTransferCreatedEvent event) {
        this.sourceBankAccountId = event.getSourceBankAccountId();
        this.destinationBankAccountId = event.getDestinationBankAccountId();
        this.amount = event.getAmount();

        BankTransferSourceDebitCommand command = new BankTransferSourceDebitCommand(event.getSourceBankAccountId(),
                                                                                  event.getBankTransferId(),
                                                                                  event.getAmount());
        commandBus.dispatch(asCommandMessage(command));
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    @EndSaga
    public void on(BankTransferSourceNotFoundEvent event) {
        BankTransferMarkFailedCommand markFailedCommand = new BankTransferMarkFailedCommand(event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(markFailedCommand));
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    @EndSaga
    public void on(BankTransferSourceDebitRejectedEvent event) {
        BankTransferMarkFailedCommand markFailedCommand = new BankTransferMarkFailedCommand(event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(markFailedCommand));
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    public void on(BankTransferSourceDebitedEvent event) {
        BankTransferDestinationCreditCommand command = new BankTransferDestinationCreditCommand(destinationBankAccountId,
                                                                                              event.getBankTransferId(),
                                                                                              event.getAmount());
        commandBus.dispatch(asCommandMessage(command));
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    @EndSaga
    public void on(BankTransferDestinationNotFoundEvent event) {
        BankTransferSourceReturnMoneyCommand returnMoneyCommand = new BankTransferSourceReturnMoneyCommand(
                sourceBankAccountId,
                amount);
        commandBus.dispatch(asCommandMessage(returnMoneyCommand));

        BankTransferMarkFailedCommand markFailedCommand = new BankTransferMarkFailedCommand(
                event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(markFailedCommand));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "bankTransferId")
    public void on(BankTransferDestinationCreditedEvent event) {
        BankTransferMarkCompletedCommand command = new BankTransferMarkCompletedCommand(event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(command));
    }
}