'use strict';

angular.module('axonBank')
    .service('BankAccountService', function ($stomp, $q) {

        var isConnected = false;

        return {
            connect: function () {
                return $q(function (resolve, reject) {
                    if (!isConnected) {
                        $stomp.connect('/websocket')
                            .then(function (frame) {
                                isConnected = true;
                                resolve();
                            })
                            .catch(function (reason) {
                                reject(reason);
                            });
                    }
                    else {
                        resolve();
                    }
                });
            },
            loadBankAccounts: function () {
                return $q(function (resolve, reject) {
                    $stomp.subscribe('/app/bank-accounts', function (data) {
                        resolve(data);
                    });
                });
            },
            loadBankTransfers: function (bankAccountId) {
                return $q(function (resolve, reject) {
                    $stomp.subscribe('/app/bank-accounts/' + bankAccountId + '/bank-transfers', function (data) {
                        resolve(data);
                    });
                });
            },
            subscribeToBankAccountUpdates: function () {
                var deferred = $q.defer();
                $stomp.subscribe('/topic/bank-accounts.updates', function (data) {
                    deferred.notify(data);
                });
                return deferred.promise;
            },

            createBankAccount: function (data) {
                $stomp.send('/app/bank-accounts/create', data);
            },
            deposit: function (data) {
                $stomp.send('/app/bank-accounts/deposit', data);
            },
            withdraw: function (data) {
                $stomp.send('/app/bank-accounts/withdraw', data);
            },
            transfer: function (data) {
                $stomp.send('/app/bank-transfers/create', data);
            }
        };
    });