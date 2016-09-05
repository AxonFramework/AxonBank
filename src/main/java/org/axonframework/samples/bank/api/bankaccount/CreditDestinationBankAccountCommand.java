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

package org.axonframework.samples.bank.api.bankaccount;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public class CreditDestinationBankAccountCommand {

    @TargetAggregateIdentifier
    private String bankAccountId;
    private String bankTransferId;
    private long amount;

    public CreditDestinationBankAccountCommand(String bankAccountId, String bankTransferId, long amount) {
        this.bankAccountId = bankAccountId;
        this.bankTransferId = bankTransferId;
        this.amount = amount;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getBankTransferId() {
        return bankTransferId;
    }

    public long getAmount() {
        return amount;
    }
}
