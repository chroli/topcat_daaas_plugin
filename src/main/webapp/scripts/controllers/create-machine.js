'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('CreateMachineController', function($q, $scope, $state, $uibModalInstance, $timeout, tc, inform){
		var that = this;
        var facility = tc.facility($state.params.facilityName);
        var user = facility.user();
        var daaas = user.daaas();
		var timeout = $q.defer();
        $scope.$on('$destroy', function(){ timeout.resolve(); });

        this.machineTypeId = null;

        this.machineTypes = [];
        daaas.machineTypes(timeout).then(function(machineTypes){
            that.machineTypes = machineTypes;
        })


    	$uibModalInstance.rendered.then(function(){
    		that.open = true;
    	});

        this.create = function() {
            daaas.createMachine(timeout.promise, this.machineTypeId).then(function(){
                $uibModalInstance.dismiss('cancel');
            }, function(response){
                $uibModalInstance.dismiss('cancel');
                inform.add(response.message, {
                    'ttl': 0,
                    'type': 'info'
                });
            });
        };

    	this.close = function() {
            $uibModalInstance.dismiss('cancel');
        };
	});

})();
