'use strict';

angular.module('axonBank')
    .controller('BankAccountsCtrl', function ($scope, $uibModal, BankAccountService) {
        function updateBankAccounts(bankAccounts) {
            $scope.bankAccounts = bankAccounts;
        }

        $scope.create = function () {
            $uibModal.open({
                controller: 'CreateBankAccountModalCtrl',
                templateUrl: '/app/modals/createBankAccountModal.html',
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body'
            });
        };

        $scope.deposit = function (id) {
            $uibModal.open({
                controller: 'DepositMoneyModalCtrl',
                templateUrl: '/app/modals/depositMoneyModal.html',
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                resolve: {
                    bankAccountId: function () {
                        return id;
                    }
                }
            });
        };

        $scope.withdraw = function (id) {
            $uibModal.open({
                controller: 'WithdrawMoneyModalCtrl',
                templateUrl: '/app/modals/withdrawMoneyModal.html',
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                resolve: {
                    bankAccountId: function () {
                        return id;
                    }
                }
            });
        };

        $scope.transfer = function (id) {
            $uibModal.open({
                controller: 'TransferMoneyModalCtrl',
                templateUrl: '/app/modals/transferMoneyModal.html',
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                resolve: {
                    bankAccountId: function () {
                        return id;
                    },
                    bankAccounts: function () {
                        return $scope.bankAccounts;
                    }
                }
            });
        };

        $scope.bankTransfers = function (id) {
            $uibModal.open({
                controller: 'BankTransfersModalCtrl',
                templateUrl: '/app/modals/bankTransfersModal.html',
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                resolve: {
                    bankAccountId: function () {
                        return id;
                    },
                    bankTransfers: function () {
                        return BankAccountService.loadBankTransfers(id);
                    }
                }
            });
        };

        BankAccountService.connect()
            .then(function () {
                BankAccountService.loadBankAccounts()
                    .then(updateBankAccounts);

                BankAccountService.subscribeToBankAccountUpdates()
                    .then(function () {
                        // do nothing
                    }, function () {
                        // do nothing
                    }, updateBankAccounts)
            });
    })
    .controller('CreateBankAccountModalCtrl', function ($uibModalInstance, $scope, BankAccountService) {
        $scope.bankAccount = {};

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
        $scope.submit = function () {
            BankAccountService.createBankAccount($scope.bankAccount);
            $uibModalInstance.close();
        };
    })
    .controller('DepositMoneyModalCtrl', function ($uibModalInstance, $scope, BankAccountService, bankAccountId) {
        $scope.deposit = {
            bankAccountId: bankAccountId
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
        $scope.submit = function () {
            BankAccountService.deposit($scope.deposit);
            $uibModalInstance.close();
        };
    })
    .controller('WithdrawMoneyModalCtrl', function ($uibModalInstance, $scope, BankAccountService, bankAccountId) {
        $scope.withdrawal = {
            bankAccountId: bankAccountId
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
        $scope.submit = function () {
            BankAccountService.withdraw($scope.withdrawal);
            $uibModalInstance.close();
        };
    })
    .controller('TransferMoneyModalCtrl',
        function ($uibModalInstance, $scope, BankAccountService, bankAccountId, bankAccounts) {
            $scope.bankAccounts = bankAccounts;
            $scope.bankTransfer = {
                sourceBankAccountId: bankAccountId
            };

            $scope.cancel = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                BankAccountService.transfer($scope.bankTransfer);
                $uibModalInstance.close();
            };
        })
    .controller('BankTransfersModalCtrl',
        function ($uibModalInstance, $scope, BankAccountService, bankAccountId, bankTransfers) {
            $scope.bankAccountId = bankAccountId;
            $scope.bankTransfers = bankTransfers;

            $scope.close = function () {
                $uibModalInstance.close();
            };
        });