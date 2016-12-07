
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminMachineTypesController', function($scope, $state, $uibModal){
    	var that = this;
      this.facilities = tc.adminFacilities();


      if(!$state.params.facilityName){
        $state.go('admin.machine-types', {facilityName: this.facilities[0].config().name});
        return;
      }

      var facility = tc.facility($state.params.facilityName);

      this.create = function(machine){
        $uibModal.open({
            templateUrl:  facility.admin().daaas().pluginUrl() + 'views/admin-create-machine-type.html',
            controller: 'AdminCreateMachineTypeController as adminCreateMachineTypeController',
            size : 'md',
            scope: $scope
        });
      };


    });

})();