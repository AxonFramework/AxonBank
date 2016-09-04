package org.axonframework.samples.bank.command;

import org.axonframework.samples.bank.api.banktransfer.BankTransferCompletedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferCreatedEvent;
import org.axonframework.samples.bank.api.banktransfer.BankTransferFailedEvent;
import org.axonframework.samples.bank.api.banktransfer.CreateBankTransferCommand;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferCompletedCommand;
import org.axonframework.samples.bank.api.banktransfer.MarkBankTransferFailedCommand;
import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.*;

public class BankTransferTest {

    private FixtureConfiguration<BankTransfer> fixture;

    @Before
    public void setUp() throws Exception {
        fixture = Fixtures.newGivenWhenThenFixture(BankTransfer.class);
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