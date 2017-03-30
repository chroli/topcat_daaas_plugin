'use strict';

(function(){
 
  var app = angular.module('topcat');

  app.service('tcUserDaaas', function($sessionStorage, $rootScope, $timeout, helpers){
  	
    this.create = function(pluginUrl, user){
      return new UserDaaas(pluginUrl, user);
    };

    function UserDaaas(pluginUrl, user){

      var that = this;
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
    			return this.get('machines', params, options).then(function(machines){
            _.each(machines, function(machine){

              machine.setResolution = helpers.overload({
                'number, number, object': function(width, height, options){
                  var params = {
                    icatUrl: facility.config().icatUrl,
                    sessionId: icat.session().sessionId,
                    width: width,
                    height: height
                  };
                  return that.post("machines/" + this.id + "/resolution", params, options);
                },
                'promise, number, number': function(timeout, width, height){
                  return this.setResolution(width, height, {timeout: timeout});
                },
                'number, number': function(){
                  return this.setResolution(width, height, {});
                }
              });

              machine.share = helpers.overload({
                'array, object': function(userNames, options){
                  var params = {
                    icatUrl: facility.config().icatUrl,
                    sessionId: icat.session().sessionId,
                    userNames: userNames.join(',')
                  };
                  return that.post("machines/" + this.id + "/share", params, options);
                },
                'promise, array': function(timeout, userNames){
                  return this.share(userNames, {timeout: timeout});
                },
                'array': function(userNames){
                  return this.share(userNames, {});
                }
              });

              machine.screenshotUrl = function(){
                var url;
                var matches;
                if(matches = pluginUrl.match(/http:\/\/localhost:10080(.*)/)){
                  url = "https://localhost:8181" + matches[1];
                } else {
                  url = pluginUrl;
                }

                url += "api/user/machines/" + this.id + "/screenshot?"
                url += "icatUrl=" + encodeURIComponent(facility.config().icatUrl);
                url += "&sessionId=" + encodeURIComponent(icat.session().sessionId);
                url += "&screenshotMd5=" + encodeURIComponent(this.screenshotMd5);

                return url;
              };

            });
            return machines;
          });
    		},
    		'promise': function(timeout, width, height){
    			return this.machines({timeout: timeout});
    		},
    		'': function(width, height){
    			return this.machines({});
    		}
    	});

      this.machineTypes = helpers.overload({
        'object': function(options){
          var params = {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          };
          return this.get('machineTypes', params, options);
        },
        'promise': function(timeout){
          return this.machineTypes({timeout: timeout});
        },
        '': function(){
          return this.machineTypes({});
        }
      });

      this.createMachine = helpers.overload({
        'number, object': function(machineTypeId, options){
          var params = {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId,
            machineTypeId: machineTypeId
          };
          return this.post('machines', params, options);
        },
        'promise, number': function(timeout, machineTypeId){
          return this.createMachine(machineTypeId, {timeout: timeout});
        },
        'number': function(machineTypeId){
          return this.createMachine(machineTypeId, {});
        }
      });

      this.deleteMachine = helpers.overload({
        'string, object': function(machineId, options){
          var params = {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          };
          return this.delete('machines/' + machineId, params, options);
        },
        'promise, string': function(timeout, machineId){
          return this.deleteMachine(machineId, {timeout: timeout});
        },
        'string': function(machineId){
          return this.deleteMachine(machineId, {});
        }
      });

      var matches;
      if(matches = pluginUrl.match(/http:\/\/localhost:10080(.*)/)){
        helpers.generateRestMethods(this, "https://localhost:8181" + matches[1] + "api/user/");
      } else {
        helpers.generateRestMethods(this, pluginUrl + "api/user/");
      }
    }
  });

})();