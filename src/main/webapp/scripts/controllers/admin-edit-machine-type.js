
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminEditMachineTypeController', function($uibModalInstance, $q, $state, $scope){
    	var that = this;

    	var facility = tc.facility($state.params.facilityName);
    	var admin = facility.admin();
    	var daaas = admin.daaas();

        this.logoData = new Uint8Array();
        this.deleteLogo = false;
    	this.loaded = false;
    	this.images = [];
    	this.flavors = [];
    	this.newScope = "";
        _.each($scope.adminMachineTypesController.machineTypeToEdit, function(value, name){
            that[name] = value;
        });

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

        this.save = function(){
        	daaas.updateMachineType(that.id, that.name, that.description, that.imageId, that.flavorId, that.availabilityZone, that.poolSize, that.aquilonArchetype, that.aquilonDomain, that.aquilonPersonality, that.aquilonSandbox, that.aquilonOSVersion, that.scopes).then(function(){
                if(that.deleteLogo){
                    return daaas.deleteMachineTypeLogo(that.id);
                }
            }).then(function(){
                if(that.logoData.length > 0){
                    return daaas.updateMachineTypeLogo(that.id, that.logoMimeType, that.logoData);
                }
            }).then(function(){
                $uibModalInstance.dismiss('cancel');
            });
        };

		this.close = function() {
		  $uibModalInstance.dismiss('cancel');
		};

    });

})();