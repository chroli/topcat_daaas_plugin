
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminCreateMachineTypeController', function($uibModalInstance, $q, $state){
    	var that = this;

    	var facility = tc.facility($state.params.facilityName);
    	var admin = facility.admin();
    	var daaas = admin.daaas();

    	this.loaded = false;
    	this.images = [];
    	this.flavors = [];

    	var promises = [];

    	promises.push(daaas.images().then(function(images){
    		that.images = images;
    	}));

    	promises.push(daaas.flavors().then(function(flavors){
    		that.flavors = flavors;
    	}));

    	$q.all(promises).then(function(){
    		that.loaded = true;
    	});

		this.close = function() {
		  $uibModalInstance.dismiss('cancel');
		};

    });

})();