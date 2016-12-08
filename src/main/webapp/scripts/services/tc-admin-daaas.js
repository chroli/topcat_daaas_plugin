'use strict';

(function(){
 
  var app = angular.module('topcat');

  app.service('tcAdminDaaas', function($sessionStorage, $rootScope, $timeout, helpers){
  	
    this.create = function(pluginUrl, admin){
      return new AdminDaaas(pluginUrl, admin);
    };

    function AdminDaaas(pluginUrl, admin){

      var facility = admin.facility();
      var icat = facility.icat();

      this.pluginUrl = function(){
        return pluginUrl;
      };

      this.images = helpers.overload({
        'object': function(options){
          return this.get('images', {sessionId: icat.session().sessionId}, options);
        },
        'promise': function(timeout){
          return this.images({timeout: timeout});
        },
        '': function(){
          return this.images({});
        }
      });

    	this.flavors = helpers.overload({
    		'object': function(options){
    			return this.get('flavors', {sessionId: icat.session().sessionId}, options);
    		},
    		'promise': function(timeout){
    			return this.flavors({timeout: timeout});
    		},
    		'': function(){
    			return this.flavors({});
    		}
    	});

      this.createMachineType = helpers.overload({
        'string, string, string, number, string, array, object': function(name, imageId, flavorId, poolSize, personality, scopes, options){
          return this.post('machineType', {
            json: JSON.stringify({
              sessionId: icat.session().sessionId,
              name: name,
              imageId: imageId,
              flavorId: flavorId,
              poolSize: poolSize,
              personality: personality,
              scopes: scopes
            })
          }, options);
        },
        'promise, string, string, string, number, string, array': function(timeout, name, imageId, flavorId, poolSize, personality, scopes){
          return this.createMachineType(name, imageId, flavorId, poolSize, personality, scopes, {timeout: timeout});
        },
        'string, string, string, number, string, array': function(name, imageId, flavorId, poolSize, personality, scopes){
          return this.createMachineType(name, imageId, flavorId, poolSize, personality, scopes, {});
        }
      });

      var matches;
      if(matches = pluginUrl.match(/http:\/\/localhost:10080(.*)/)){
        helpers.generateRestMethods(this, "https://localhost:8181" + matches[1] + "api/admin/");
      } else {
        helpers.generateRestMethods(this, pluginUrl + "api/admin/");
      }
    }
  });

})();