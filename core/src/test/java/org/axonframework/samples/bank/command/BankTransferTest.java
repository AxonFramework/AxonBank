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

import org.axonframework.samples.bank.api.banktransfer.BankTransferCompletedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCreatedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferFailedEvent;
import org.axonframework.samples.bank.api.banktransfer.CreateBankTransferCommand;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferCompletedCommand;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferFailedCommand;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.*;

public class BankTransferTest {

    private FixtureConfiguration<BankTransfer> fixture;

    @Before
    public void setUp() throws Exception {
        fixture = new AggregateTestFixture<>(BankTransfer.class);
    }

    @Test
    public void testCreateBankTransfer() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";

        fixture.givenNoPriorActivity()
               .when(new CreateBankTransferCommand(bankTransferId, sourceBankAccountId, destinationBankAccountId, 20))
               .expectEvents(new BankTransferCreatedEvent(bankTransferId,
                                                          sourceBankAccountId,
                                                          destinationBankAccountId,
                                                          20));
    }

    @Test
    public void testMarkBankTransferCompleted() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";

        fixture.given(new BankTransferCreatedEvent(bankTransferId, sourceBankAccountId, destinationBankAccountId, 20))
               .when(new MarkBankTransferCompletedCommand(bankTransferId))
               .expectEvents(new BankTransferCompletedEvent(bankTransferId));
    }

    @Test
    public void testMarkBankTransferFailed() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";

        fixture.given(new BankTransferCreatedEvent(bankTransferId, sourceBankAccountId, destinationBankAccountId, 20))
               .when(new MarkBankTransferFailedCommand(bankTransferId))
               .expectEvents(new BankTransferFailedEvent(bankTransferId));
    }
}