'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('ShareMachineController', function($state, $uibModalInstance, tc){
		var that = this;
        var facility = tc.facility($state.params.facilityName);

        this.newUser = "";
        this.users = [];
        this.candidateUsers = [];


        this.deleteUser = function(user){
            _.remove(this.users, user);
        };

        this.updateCandidateUsers = function(){
            if(this.newUser == ""){
                this.candidateUsers = [];
            } else {
                facility.icat().query([
                    "select user from User user",
                    "where user.name like concat('%', ?, '%')", this.newUser,
                    "or user.fullName like concat('%', ?, '%')", this.newUser,
                    "limit 0, 11"
                ]).then(function(users){
                    that.candidateUsers = users
                });
            }
        };

        this.addUser = function(user){
            if(user){
                this.users.push(user);
                this.newUser = '';
                this.candidateUsers = [];
            } else if(this.candidateUsers.length == 1){
                this.users.push(this.candidateUsers[0]);
                this.newUser = '';
                this.candidateUsers = [];
            }
        };

        this.share = function() {
            
        };

    	this.close = function() {
            $uibModalInstance.dismiss('cancel');
        };
	});

})();
