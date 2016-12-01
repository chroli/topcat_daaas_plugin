
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('AdminMachineTemplatesController', function($translate, $scope, $state, $timeout, $q, tc, helpers){
    	var that = this;
      this.facilities = tc.adminFacilities();


      if(!$state.params.facilityName){
        $state.go('admin.machine-templates', {facilityName: this.facilities[0].config().name});
        return;
      } 

    });

})();