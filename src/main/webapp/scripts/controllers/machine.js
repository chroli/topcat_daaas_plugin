'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('MachineController', function($q, $scope, $state, $uibModalInstance, $timeout, tc){
		var that = this;
		var timeout = $q.defer();
    	$scope.$on('$destroy', function(){ timeout.resolve(); });
        var facility = tc.facility($state.params.facilityName);
        this.machine = $scope.myMachinesController.machine;
    	this.token = _.select(this.machine.users, function(user){ return user.userName == facility.icat().session().username})[0].websockifyToken;
        this.open = false;

    	$uibModalInstance.rendered.then(function(){
    		that.open = true;
    	});

        this.toFullScreen = function() {
            this.fullScreen = true;
        };

    	this.close = function() {
            $uibModalInstance.dismiss('cancel');
        };
	});

})();
