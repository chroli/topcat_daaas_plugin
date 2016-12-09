
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
        this.availabilityZones = [];
    	this.poolSize = 10;
        this.personality = ""
    	this.scopes = [];
    	this.newScope = "";

    	var promises = [];

    	promises.push(daaas.images().then(function(images){
    		that.images = _.sortBy(images, 'name');
    	}));

    	promises.push(daaas.flavors().then(function(flavors){
    		that.flavors = _.sortBy(flavors, 'ram');
    	}));

        promises.push(daaas.availabilityZones().then(function(availabilityZones){
            that.availabilityZones = _.sortBy(availabilityZones, 'name');
        }));

    	$q.all(promises).then(function(){
    		that.loaded = true;
    	});

    	this.deleteScope = function(scope){
            this.scopes = _.select(this.scopes, function(currentScope){
                return currentScope != scope;
            });
        };

        this.addScope = function(){
            if(this.newScope != "" && !_.includes(this.scopes, this.newScope)){
                this.scopes.push({query: this.newScope});
                this.newScope = "";
            }
        };

        this.create = function(){
        	daaas.createMachineType(that.name, that.imageId, that.flavorId, that.availabilityZone, that.poolSize, that.personality, that.scopes).then(function(){
                $uibModalInstance.dismiss('cancel');
            });
        };

		this.close = function() {
		  $uibModalInstance.dismiss('cancel');
		};

    });

})();