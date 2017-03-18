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

import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.messaging.interceptors.JSR303ViolationException;
import org.axonframework.samples.bank.api.bankaccount.BankAccountCreatedEvent;
import org.axonframework.samples.bank.api.bankaccount.BankAccountCreateCommand;
import org.axonframework.samples.bank.api.bankaccount.BankAccountMoneyDepositCommand;
import org.axonframework.samples.bank.api.bankaccount.BankAccountMoneyDepositedEvent;
import org.axonframework.samples.bank.api.bankaccount.BankAccountMoneyWithdrawnEvent;
import org.axonframework.samples.bank.api.bankaccount.BankAccountWithdrawMoneyCommand;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.*;

import java.util.UUID;

public class BankAccountCommandHandlerTest {

    private FixtureConfiguration<BankAccount> testFixture;

    @Before
    public void setUp() throws Exception {
        testFixture = new AggregateTestFixture<>(BankAccount.class);

        testFixture.registerAnnotatedCommandHandler(new BankAccountCommandHandler(testFixture.getRepository(),
                                                                                  testFixture.getEventBus()));
        testFixture.registerCommandDispatchInterceptor(new BeanValidationInterceptor<>());
    }

    @Test(expected = JSR303ViolationException.class)
    public void testCreateBankAccount_RejectNegativeOverdraft() throws Exception {
        testFixture.givenNoPriorActivity()
                   .when(new BankAccountCreateCommand(UUID.randomUUID().toString(), -1000));
    }

    @Test
    public void testCreateBankAccount() throws Exception {
        String id = "bankAccountId";

        testFixture.givenNoPriorActivity()
                   .when(new BankAccountCreateCommand(id, 0))
                   .expectEvents(new BankAccountCreatedEvent(id, 0));
    }

    @Test
    public void testDepositMoney() throws Exception {
        String id = "bankAccountId";

        testFixture.given(new BankAccountCreatedEvent(id, 0))
                   .when(new BankAccountMoneyDepositCommand(id, 1000))
                   .expectEvents(new BankAccountMoneyDepositedEvent(id, 1000));
    }

    @Test
    public void testWithdrawMoney() throws Exception {
        String id = "bankAccountId";

        testFixture.given(new BankAccountCreatedEvent(id, 0), new BankAccountMoneyDepositedEvent(id, 50))
                   .when(new BankAccountWithdrawMoneyCommand(id, 50))
                   .expectEvents(new BankAccountMoneyWithdrawnEvent(id, 50));
    }

    @Test
    public void testWithdrawMoney_RejectWithdrawal() throws Exception {
        String id = "bankAccountId";

        testFixture.given(new BankAccountCreatedEvent(id, 0), new BankAccountMoneyDepositedEvent(id, 50))
                   .when(new BankAccountWithdrawMoneyCommand(id, 51))
                   .expectEvents();
    }
}