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
import org.axonframework.commandhandling.callbacks.LoggingCallback;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.samples.bank.api.bankaccount.*;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCreatedEvent;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferCompletedCommand;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferFailedCommand;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@Saga
public class BankTransferManagementSaga {

    private transient CommandBus commandBus;

    @Autowired
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

        DebitSourceBankAccountCommand command = new DebitSourceBankAccountCommand(event.getSourceBankAccountId(),
                                                                                  event.getBankTransferId(),
                                                                                  event.getAmount());
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    @EndSaga
    public void on(SourceBankAccountNotFoundEvent event) {
        MarkBankTransferFailedCommand markFailedCommand = new MarkBankTransferFailedCommand(event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(markFailedCommand), LoggingCallback.INSTANCE);
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    @EndSaga
    public void on(SourceBankAccountDebitRejectedEvent event) {
        MarkBankTransferFailedCommand markFailedCommand = new MarkBankTransferFailedCommand(event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(markFailedCommand), LoggingCallback.INSTANCE);
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    public void on(SourceBankAccountDebitedEvent event) {
        CreditDestinationBankAccountCommand command = new CreditDestinationBankAccountCommand(destinationBankAccountId,
                                                                                              event.getBankTransferId(),
                                                                                              event.getAmount());
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    @EndSaga
    public void on(DestinationBankAccountNotFoundEvent event) {
        ReturnMoneyOfFailedBankTransferCommand returnMoneyCommand = new ReturnMoneyOfFailedBankTransferCommand(
                sourceBankAccountId,
                amount);
        commandBus.dispatch(asCommandMessage(returnMoneyCommand), LoggingCallback.INSTANCE);

        MarkBankTransferFailedCommand markFailedCommand = new MarkBankTransferFailedCommand(
                event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(markFailedCommand), LoggingCallback.INSTANCE);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "bankTransferId")
    public void on(DestinationBankAccountCreditedEvent event) {
        MarkBankTransferCompletedCommand command = new MarkBankTransferCompletedCommand(event.getBankTransferId());
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);
    }
}
