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
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.callbacks.VoidCallback;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.SagaLifecycle;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.samples.bank.api.bankaccount.CreditDestinationBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DebitSourceBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DestinationBankAccountCreditedEvent;
import org.axonframework.samples.bank.api.bankaccount.ReturnMoneyOfFailedBankTransferCommand;
import org.axonframework.samples.bank.api.bankaccount.SourceBankAccountDebitedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCreatedEvent;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferCompletedCommand;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferFailedCommand;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

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
        commandBus.dispatch(GenericCommandMessage.asCommandMessage(command),
                            new CommandCallback<DebitSourceBankAccountCommand, Boolean>() {
                                @Override
                                public void onSuccess(CommandMessage<? extends DebitSourceBankAccountCommand> commandMessage,
                                                      Boolean result) {
                                    if (result.equals(Boolean.FALSE)) {
                                        MarkBankTransferFailedCommand markFailedCommand = new MarkBankTransferFailedCommand(
                                                event.getBankTransferId());
                                        commandBus.dispatch(GenericCommandMessage.asCommandMessage(markFailedCommand));

                                        SagaLifecycle.end();
                                    }
                                }

                                @Override
                                public void onFailure(CommandMessage<? extends DebitSourceBankAccountCommand> commandMessage,
                                                      Throwable cause) {
                                    if (AggregateNotFoundException.class.isAssignableFrom(cause.getClass())) {
                                        MarkBankTransferFailedCommand markFailedCommand = new MarkBankTransferFailedCommand(
                                                event.getBankTransferId());
                                        commandBus.dispatch(GenericCommandMessage.asCommandMessage(markFailedCommand));

                                        SagaLifecycle.end();
                                    } else {
                                        throw new RuntimeException(cause);
                                    }
                                }
                            });
    }

    @SagaEventHandler(associationProperty = "bankTransferId")
    public void on(SourceBankAccountDebitedEvent event) {
        CreditDestinationBankAccountCommand command = new CreditDestinationBankAccountCommand(destinationBankAccountId,
                                                                                              event.getBankTransferId(),
                                                                                              event.getAmount());
        commandBus.dispatch(GenericCommandMessage.asCommandMessage(command),
                            new VoidCallback<CreditDestinationBankAccountCommand>() {
                                @Override
                                protected void onSuccess(CommandMessage<? extends CreditDestinationBankAccountCommand> commandMessage) {

                                }

                                @Override
                                public void onFailure(CommandMessage<? extends CreditDestinationBankAccountCommand> commandMessage,
                                                      Throwable cause) {
                                    if (AggregateNotFoundException.class.isAssignableFrom(cause.getClass())) {
                                        ReturnMoneyOfFailedBankTransferCommand returnMoneyCommand = new ReturnMoneyOfFailedBankTransferCommand(
                                                event.getBankTransferId(),
                                                event.getAmount());
                                        commandBus.dispatch(GenericCommandMessage.asCommandMessage(returnMoneyCommand));

                                        MarkBankTransferFailedCommand markFailedCommand = new MarkBankTransferFailedCommand(
                                                event.getBankTransferId());
                                        commandBus.dispatch(GenericCommandMessage.asCommandMessage(markFailedCommand));

                                        SagaLifecycle.end();
                                    } else {
                                        throw new RuntimeException(cause);
                                    }
                                }
                            });
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "bankTransferId")
    public void on(DestinationBankAccountCreditedEvent event) {
        MarkBankTransferCompletedCommand command = new MarkBankTransferCompletedCommand(event.getBankTransferId());
        commandBus.dispatch(GenericCommandMessage.asCommandMessage(command));
    }
}