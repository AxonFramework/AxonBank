package org.axonframework.samples.bank.command;

import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.messaging.interceptors.JSR303ViolationException;
import org.axonframework.samples.bank.api.bankaccount.BankAccountCreatedEvent;
import org.axonframework.samples.bank.api.bankaccount.CreateBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DepositMoneyCommand;
import org.axonframework.samples.bank.api.bankaccount.MoneyDepositedEvent;
import org.axonframework.samples.bank.api.bankaccount.MoneyWithdrawnEvent;
import org.axonframework.samples.bank.api.bankaccount.WithdrawMoneyCommand;
import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.*;

import java.util.UUID;

public class BankAccountCommandHandlerTest {

    private FixtureConfiguration<BankAccount> testFixture;

    @Before
    public void setUp() throws Exception {
        testFixture = Fixtures.newGivenWhenThenFixture(BankAccount.class);

        testFixture.registerAnnotatedCommandHandler(new BankAccountCommandHandler(testFixture.getRepository(),
                                                                                  testFixture.getEventBus()));
        testFixture.registerCommandDispatchInterceptor(new BeanValidationInterceptor<>());
    }

    @Test(expected = JSR303ViolationException.class)
    public void testCreateBankAccount_RejectNegativeOverdraft() throws Exception {
        testFixture.givenNoPriorActivity()
                   .when(new CreateBankAccountCommand(UUID.randomUUID().toString(), -1000));
    }

    @Test
    public void testCreateBankAccount() throws Exception {
        String id = "bankAccountId";

        testFixture.givenNoPriorActivity()
                   .when(new CreateBankAccountCommand(id, 0))
                   .expectEvents(new BankAccountCreatedEvent(id, 0));
    }

    @Test
    public void testDepositMoney() throws Exception {
        String id = "bankAccountId";

        testFixture.given(new BankAccountCreatedEvent(id, 0))
                   .when(new DepositMoneyCommand(id, 1000))
                   .expectEvents(new MoneyDepositedEvent(id, 1000));
    }

    @Test
    public void testWithdrawMoney() throws Exception {
        String id = "bankAccountId";

        testFixture.given(new BankAccountCreatedEvent(id, 0), new MoneyDepositedEvent(id, 50))
                   .when(new WithdrawMoneyCommand(id, 50))
                   .expectEvents(new MoneyWithdrawnEvent(id, 50));
    }

    @Test
    public void testWithdrawMoney_RejectWithdrawal() throws Exception {
        String id = "bankAccountId";

        testFixture.given(new BankAccountCreatedEvent(id, 0), new MoneyDepositedEvent(id, 50))
                   .when(new WithdrawMoneyCommand(id, 51))
                   .expectEvents();
    }
}