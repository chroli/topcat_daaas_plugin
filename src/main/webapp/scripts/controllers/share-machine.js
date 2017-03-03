'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('ShareMachineController', function($state, $uibModalInstance, tc){
		var that = this;
        var facility = tc.facility($state.params.facilityName);

        this.newUser = "";
        this.users = [];
        this.candidateUsers = [];
        this.selectedCandidateUserPosition = -1;


        this.deleteUser = function(user){
            _.remove(this.users, user);
        };

        this.newUserKeydown =  function($event){
            if($event.which == 13){
                this.addUser();
                $event.preventDefault();
            }

            if($event.which == 38){
                this.newUserUpKey();
                $event.preventDefault();
            }

            if($event.which == 40){
                this.newUserDownKey();
                $event.preventDefault();
            }
        };

        this.newUserKeyup = function($event){
            if(!($event.which == 38 || $event.which == 40)){
                this.updateCandidateUsers();
            }
        };

        this.newUserUpKey = function(){
            this.selectedCandidateUserPosition--;
            if(this.selectedCandidateUserPosition < -1) this.selectedCandidateUserPosition = -1;
        };

        this.newUserDownKey = function(){
            this.selectedCandidateUserPosition++;
            if(this.selectedCandidateUserPosition > this.candidateUsers.length - 1) this.selectedCandidateUserPosition = this.candidateUsers.length - 1;
        };

        this.updateCandidateUsers = function(){
            if(this.newUser == ""){
                this.candidateUsers = [];
            } else {
                facility.icat().query([
                    "select user from User user",
                    "where user.name like concat('%', ?, '%')", this.newUser,
                    "or user.fullName like concat('%', ?, '%')", this.newUser,
                    "limit 0, 15"
                ]).then(function(users){
                    that.candidateUsers = _.map(users, function(user, position){
                        user.position = position;
                        return user;
                    });
                    that.selectedCandidateUserPosition = -1;
                });
            }
        };

        this.addUser = function(user){
            if(user){
                this.users.push(user);
                this.newUser = '';
                this.candidateUsers = [];
            } else if(this.selectedCandidateUserPosition > -1){
                this.users.push(this.candidateUsers[this.selectedCandidateUserPosition]);
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
