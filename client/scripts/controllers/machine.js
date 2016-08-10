'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('MachineController', function($q, $scope, $uibModalInstance, $timeout, tc){
		var that = this;
		var timeout = $q.defer();
    	$scope.$on('$destroy', function(){ timeout.resolve(); });
    	this.token = tc.daaas().sessionId();
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
