'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('MyMachinesController', function($q, $scope, $uibModal, $interval, tc, inform){
		var that = this;
		var timeout = $q.defer();
    	$scope.$on('$destroy', function(){ timeout.resolve(); });

	    this.machines = [];

        function pollMachines(){
            tc.daaas().machines({timeout: timeout.promise, bypassInterceptors: true}).then(function(machines){
                that.machines = machines;
            });
        }
        var pollMachinesPromise = $interval(pollMachines, 1000);
        timeout.promise.then(function(){ $interval.cancel(pollMachinesPromise); });
        pollMachines();
	    

	    this.view = function(machine){
	    	that.machine = machine;
	    	$uibModal.open({
                templateUrl:  tc.daaas().pluginUrl() + 'views/machine.html',
                controller: 'MachineController as machineController',
                size : 'lg',
                scope: $scope
            }).opened.catch(function (error) {
                inform.add(error, {
                    'ttl': 0,
                    'type': 'danger'
                });
            });
	    };

        this.delete = function(machine){
            if(confirm("Are you really sure you want to delete this machine?")){
                tc.daaas().deleteMachine(machine.id, timeout).then(pollMachines);
            }
        };

        this.create = function(machine){
            that.machine = machine;
            $uibModal.open({
                templateUrl: tc.daaas().pluginUrl() + 'views/create-machine.html',
                controller: 'CreateMachineController as createMachineController',
                size : 'md',
                scope: $scope
            }).opened.catch(function (error) {
                inform.add(error, {
                    'ttl': 0,
                    'type': 'danger'
                });
            });
        };

	});

})();
