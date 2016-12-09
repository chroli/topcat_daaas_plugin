
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminMachineTypesController', function($scope, $state, $uibModal, $q, $interval){
    	var that = this;
      this.facilities = tc.adminFacilities();


      if(!$state.params.facilityName){
        $state.go('admin.machine-types', {facilityName: this.facilities[0].config().name});
        return;
      }

      var facility = tc.facility($state.params.facilityName);
      var admin = facility.admin();
      var daaas = admin.daaas();
      var timeout = $q.defer();
      $scope.$on('$destroy', function(){ timeout.resolve(); });

      var imageIndex = {};
      var flavorIndex = {};
      var promises = [];

      promises.push(daaas.images(timeout.promise).then(function(images){
        _.each(images, function(image){
          imageIndex[image.id] = image;
        });
      }));

      promises.push(daaas.flavors(timeout.promise).then(function(flavors){
        _.each(flavors, function(flavor){
          flavorIndex[flavor.id] = flavor;
        });
      }));

      this.machineTypes = [];

      $q.all(promises).then(function(){
        function pollMachineTypes(){
          daaas.machineTypes(timeout.promise).then(function(machineTypes){
            _.each(machineTypes, function(machineType){
              machineType.image = imageIndex[machineType.imageId];
              machineType.flavor = flavorIndex[machineType.flavorId];
            });
            that.machineTypes = machineTypes;
          });
        }
        var pollMachineTypesPromise = $interval(pollMachineTypes, 1000);
        timeout.promise.then(function(){ $interval.cancel(pollMachineTypesPromise); });
        pollMachineTypes();
      });

      this.create = function(){
        $uibModal.open({
            templateUrl:  daaas.pluginUrl() + 'views/admin-create-machine-type.html',
            controller: 'AdminCreateMachineTypeController as adminCreateMachineTypeController',
            size : 'md',
            scope: $scope
        });
      };

      this.edit = function(machineType){
        this.machineTypeToEdit = machineType;
        $uibModal.open({
            templateUrl:  daaas.pluginUrl() + 'views/admin-edit-machine-type.html',
            controller: 'AdminEditMachineTypeController as adminEditMachineTypeController',
            size : 'md',
            scope: $scope
        });
      };

      this.delete = function(machineType){
        if(confirm("Are you really you want to delete this machine type?")){
          daaas.deleteMachineType(timeout.promise, machineType.id);
        }
      };

    });

})();