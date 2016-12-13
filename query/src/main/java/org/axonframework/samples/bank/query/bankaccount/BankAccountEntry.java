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

package org.axonframework.samples.bank.query.bankaccount;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class BankAccountEntry {

    @Id
    @GeneratedValue
    private long id;
    private String axonBankAccountId;
    private long balance;
    private long overdraftLimit;

    @SuppressWarnings("unused")
    public BankAccountEntry() {
    }

    public BankAccountEntry(String axonBankAccountId, long balance, long overdraftLimit) {
        this.axonBankAccountId = axonBankAccountId;
        this.balance = balance;
        this.overdraftLimit = overdraftLimit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAxonBankAccountId() {
        return axonBankAccountId;
    }

    public void setAxonBankAccountId(String axonBankAccountId) {
        this.axonBankAccountId = axonBankAccountId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }
}