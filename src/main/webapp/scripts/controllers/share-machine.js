'use strict';

(function(){

	var app = angular.module('topcat');

	app.controller('ShareMachineController', function($state, $uibModalInstance, $scope, $q, tc){
		var that = this;
        var facility = tc.facility($state.params.facilityName);

        this.newUser = "";
        this.users = [];
        this.candidateUsers = [];
        this.selectedCandidateUserPosition = -1;
        var timeout = $q.defer();
        $scope.$on('$destroy', function(){ timeout.resolve(); });

        _.each($scope.myMachinesController.machine.users, function(user){
            if(user.type != 'PRIMARY'){
                facility.icat().query(timeout.promise, [
                    "select user from User user where user.name = ?", user.userName
                ]).then(function(users){
                    if(users[0]) that.users.push(users[0]);
                });
            }
        });

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
                facility.icat().query(timeout.promise, [
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

        this.save = function(){
            var userNames = _.map(this.users, function(user){ return user.name; });
            $scope.myMachinesController.machine.share(timeout.promise, userNames).then(function(){
                $uibModalInstance.dismiss('cancel');
            });
        };

    	this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };
	});

})();
