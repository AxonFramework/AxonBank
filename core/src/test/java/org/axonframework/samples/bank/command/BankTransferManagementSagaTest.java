package org.axonframework.samples.bank.command;

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
                   .expectDispatchedCommands(new BankTransferSourceDebitCommand(sourceBankAccountId,
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
                   .whenPublishingA(new BankTransferSourceNotFoundEvent(bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new BankTransferMarkFailedCommand(bankTransferId));
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
                   .publishes(new BankTransferSourceDebitRejectedEvent(bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new BankTransferMarkFailedCommand(bankTransferId));
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
                   .whenAggregate(sourceBankAccountId).publishes(new BankTransferSourceDebitedEvent(sourceBankAccountId,
                                                                                                   amountOfMoneyToTransfer,
                                                                                                   bankTransferId))
                   .expectActiveSagas(1)
                   .expectDispatchedCommands(new BankTransferDestinationCreditCommand(destinationBankAccountId,
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
                   .andThenAggregate(sourceBankAccountId).published(new BankTransferSourceDebitedEvent(
                sourceBankAccountId, amountOfMoneyToTransfer, bankTransferId))
                   .whenPublishingA(new BankTransferDestinationNotFoundEvent(bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new BankTransferSourceReturnMoneyCommand(sourceBankAccountId,
                                                                                        amountOfMoneyToTransfer),
                                             new BankTransferMarkFailedCommand(bankTransferId));
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
                   .andThenAggregate(sourceBankAccountId).published(new BankTransferSourceDebitedEvent(
                sourceBankAccountId,
                amountOfMoneyToTransfer,
                bankTransferId))
                   .whenAggregate(destinationBankAccountId).publishes(new BankTransferDestinationCreditedEvent(
                destinationBankAccountId,
                amountOfMoneyToTransfer,
                bankTransferId))
                   .expectActiveSagas(0)
                   .expectDispatchedCommands(new BankTransferMarkCompletedCommand(bankTransferId));
    }
}
