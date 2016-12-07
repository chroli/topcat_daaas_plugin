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
          var params = {
            sessionId: icat.session().sessionId
          };
          return this.get('images', params, options);
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
    			var params = {
    				sessionId: icat.session().sessionId
    			};
    			return this.get('flavors', params, options);
    		},
    		'promise': function(timeout){
    			return this.flavors({timeout: timeout});
    		},
    		'': function(){
    			return this.flavors({});
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