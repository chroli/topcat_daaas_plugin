'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('ShareMachineController', function($state, $uibModalInstance, tc){
		var that = this;
        var facility = tc.facility($state.params.facilityName);

        this.newUser = "";
        this.users = [];

        this.deleteUser = function(user){
            _.remove(this.users, user);
        };

        this.addUser = function(){
            if(this.newUser == '') return;

            facility.icat().query([
                "select user from User user",
                "where user.name like concat('%', ?, '%')", this.newUser,
                "or user.fullName like concat('%', ?, '%')", this.newUser,
                "limit 0, 2"
            ]).then(function(users){
                if(users.length == 1){
                    that.users.push(users[0]);
                    that.newUser = '';
                }
            });
        };

        this.share = function() {
            
        };

    	this.close = function() {
            $uibModalInstance.dismiss('cancel');
        };
	});

})();
