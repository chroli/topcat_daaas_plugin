'use strict';

(function(){
 
  var app = angular.module('topcat');

  app.service('tcUserDaaas', function($sessionStorage, $rootScope, $timeout, helpers){
  	
    this.create = function(pluginUrl, user){
      return new UserDaaas(pluginUrl, user);
    };

    function UserDaaas(pluginUrl, user){

      var facility = user.facility();
      var icat = facility.icat();

      this.pluginUrl = function(){
        return pluginUrl;
      };

    	this.machines = helpers.overload({
    		'object': function(options){
    			var params = {
    				icatUrl: facility.config().icatUrl,
    				sessionId: icat.session().sessionId
    			};
    			return this.get('machines', params, options);
    		},
    		'promise': function(timeout){
    			return this.machines({timeout: timeout});
    		},
    		'': function(){
    			return this.machines({});
    		}
    	});

      /*
      this.createMachine = helpers.overload({
        'string, string, object': function(templateId, name, options){
          var params = {
            templateId: templateId,
            name:  name,
            sessionId: this.sessionId()
          };
          return this.post('machines', params, options);
        },
        'string, string, promise': function(templateId, name, timeout){
          return this.createMachine(templateId, name, {timeout: timeout});
        },
        'string, string': function(templateId, name){
          return this.createMachine(templateId, name, {});
        }
      });

      this.deleteMachine = helpers.overload({
        'string, object': function(machineId, options){
          return this.delete('machines/' + machineId, {sessionId: this.sessionId()}, options);
        },
        'string,  promise': function(machineId, timeout){
          return this.deleteMachine(machineId, {timeout: timeout});
        },
        'string, string': function(machineId){
          return this.deleteMachine(machineId, {});
        }
      });

      this.templates = helpers.overload({
        'object': function(options){
          var params = {
            username: this.username(),
            sessionId: this.sessionId()
          };
          return this.get('templates', params, options);
        },
        'promise': function(timeout){
          return this.templates({timeout: timeout});
        },
        '': function(){
          return this.templates({});
        }
      });
      */

      var matches;
      if(matches = pluginUrl.match(/http:\/\/localhost:10080(.*)/)){
        helpers.generateRestMethods(this, "https://localhost:8181" + matches[1] + "api/user/");
      } else {
        helpers.generateRestMethods(this, pluginUrl + "api/user/");
      }
    }
  });

})();