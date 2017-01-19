package org.axonframework.samples.bank.command;

import org.axonframework.samples.bank.api.bankaccount.CreditDestinationBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DebitSourceBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.DestinationBankAccountCreditedEvent;
import org.axonframework.samples.bank.api.bankaccount.DestinationBankAccountNotFoundEvent;
import org.axonframework.samples.bank.api.bankaccount.ReturnMoneyOfFailedBankTransferCommand;
import org.axonframework.samples.bank.api.bankaccount.SourceBankAccountDebitRejectedEvent;
import org.axonframework.samples.bank.api.bankaccount.SourceBankAccountDebitedEvent;
import org.axonframework.samples.bank.api.bankaccount.SourceBankAccountNotFoundEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCreatedEvent;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferCompletedCommand;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferFailedCommand;
import org.axonframework.test.saga.FixtureConfiguration;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.*;

public class BankTransferManagementSagaTest {

    private FixtureConfiguration testFixture;

    @Before
    public void setUp() throws Exception {
        testFixture = new SagaTestFixture<>(BankTransferManagementSaga.class);
    }

    @Test
    public void testBankTransferCreated() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";
        long amountOfMoneyToTransfer = 40;

        testFixture.givenNoPriorActivity()
                   .whenAggregate(bankTransferId).publishes(new BankTransferCreatedEvent(bankTransferId,
                                                                                         sourceBankAccountId,
                                                                                         destinationBankAccountId,
                                                                                         amountOfMoneyToTransfer))
                   .expectActiveSagas(1)
                   .expectDispatchedCommands(new DebitSourceBankAccountCommand(sourceBankAccountId,
                                                                               bankTransferId,
                                                                               amountOfMoneyToTransfer));
    }

    @Test
    public void testSourceBankAccountNotFound() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";
        long amountOfMoneyToTransfer = 40;

        testFixture.givenAggregate(bankTransferId).published(new BankTransferCreatedEvent(bankTransferId,
                                                                                          sourceBankAccountId,
                                                                                          destinationBankAccountId,
                                                                                          amountOfMoneyToTransfer))
                   .whenPublishingA(new SourceBankAccountNotFoundEvent(bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new MarkBankTransferFailedCommand(bankTransferId));
    }

    @Test
    public void testSourceBankAccountDebitRejected() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";
        long amountOfMoneyToTransfer = 40;

        testFixture.givenAggregate(bankTransferId).published(new BankTransferCreatedEvent(bankTransferId,
                                                                                          sourceBankAccountId,
                                                                                          destinationBankAccountId,
                                                                                          amountOfMoneyToTransfer))
                   .whenAggregate(sourceBankAccountId)
                   .publishes(new SourceBankAccountDebitRejectedEvent(bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new MarkBankTransferFailedCommand(bankTransferId));
    }

    @Test
    public void testSourceBankAccountDebited() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";
        long amountOfMoneyToTransfer = 40;

        testFixture.givenAggregate(bankTransferId).published(new BankTransferCreatedEvent(bankTransferId,
                                                                                          sourceBankAccountId,
                                                                                          destinationBankAccountId,
                                                                                          amountOfMoneyToTransfer))
                   .whenAggregate(sourceBankAccountId).publishes(new SourceBankAccountDebitedEvent(sourceBankAccountId,
                                                                                                   amountOfMoneyToTransfer,
                                                                                                   bankTransferId))
                   .expectActiveSagas(1)
                   .expectDispatchedCommands(new CreditDestinationBankAccountCommand(destinationBankAccountId,
                                                                                     bankTransferId,
                                                                                     amountOfMoneyToTransfer));
    }

    @Test
    public void testDestinationBankAccountNotFound() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";
        long amountOfMoneyToTransfer = 40;

        testFixture.givenAggregate(bankTransferId).published(new BankTransferCreatedEvent(bankTransferId,
                                                                                          sourceBankAccountId,
                                                                                          destinationBankAccountId,
                                                                                          amountOfMoneyToTransfer))
                   .andThenAggregate(sourceBankAccountId).published(new SourceBankAccountDebitedEvent(
                sourceBankAccountId, amountOfMoneyToTransfer, bankTransferId))
                   .whenPublishingA(new DestinationBankAccountNotFoundEvent(bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new ReturnMoneyOfFailedBankTransferCommand(sourceBankAccountId,
                                                                                        amountOfMoneyToTransfer),
                                             new MarkBankTransferFailedCommand(bankTransferId));
    }

    @Test
    public void testDestinationBankAccountCredited() throws Exception {
        String bankTransferId = "bankTransferId";
        String sourceBankAccountId = "sourceBankAccountId";
        String destinationBankAccountId = "destinationBankAccountId";
        long amountOfMoneyToTransfer = 40;

        testFixture.givenAggregate(bankTransferId).published(new BankTransferCreatedEvent(bankTransferId,
                                                                                          sourceBankAccountId,
                                                                                          destinationBankAccountId,
                                                                                          amountOfMoneyToTransfer))
                   .andThenAggregate(sourceBankAccountId).published(new SourceBankAccountDebitedEvent(
                sourceBankAccountId,
                amountOfMoneyToTransfer,
                bankTransferId))
                   .whenAggregate(destinationBankAccountId).publishes(new DestinationBankAccountCreditedEvent(
                destinationBankAccountId,
                amountOfMoneyToTransfer,
                bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new MarkBankTransferCompletedCommand(bankTransferId));
    }
}
