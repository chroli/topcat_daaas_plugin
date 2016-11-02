'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('CreateMachineController', function($q, $scope, $uibModalInstance, $timeout, tc){
		var that = this;
		var timeout = $q.defer();
    	$scope.$on('$destroy', function(){ timeout.resolve(); });

        this.templateId = null;
        this.name = "";

        that.templates = [];
        tc.daaas().templates(timeout).then(function(templates){
            that.templates = templates;
        })


    	$uibModalInstance.rendered.then(function(){
    		that.open = true;
    	});

        this.create = function() {
            tc.daaas().createMachine(this.templateId, this.name, timeout).then(function(){
                $uibModalInstance.dismiss('cancel');
            });
        };

    	this.close = function() {
            $uibModalInstance.dismiss('cancel');
        };
	});

})();
