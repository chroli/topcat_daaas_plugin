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
          return this.get('images', {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          }, options);
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
    			return this.get('flavors', {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          }, options);
    		},
    		'promise': function(timeout){
    			return this.flavors({timeout: timeout});
    		},
    		'': function(){
    			return this.flavors({});
    		}
    	});

      this.availabilityZones = helpers.overload({
        'object': function(options){
          return this.get('availabilityZones', {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          }, options);
        },
        'promise': function(timeout){
          return this.availabilityZones({timeout: timeout});
        },
        '': function(){
          return this.availabilityZones({});
        }
      });

      this.machineTypes = helpers.overload({
        'object': function(options){
          return this.get('machineTypes', {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          }, options).then(function(machineTypes){
            
            _.each(machineTypes, function(machineType){
              machineType.logoUrl = function(){
                var url;
                var matches;
                if(matches = pluginUrl.match(/http:\/\/localhost:10080(.*)/)){
                  url = "https://localhost:8181" + matches[1];
                } else {
                  url = pluginUrl;
                }

                url += "api/user/machineTypes/" + this.id + "/logo";
                url += "?md5=" + encodeURIComponent(this.logoMd5);

                return url;
              };
            });

            return machineTypes;

          });
        },
        'promise': function(timeout){
          return this.machineTypes({timeout: timeout});
        },
        '': function(){
          return this.machineTypes({});
        }
      });

      this.createMachineType = helpers.overload({
        'string, string, string, string, string, number, string, string, string, string, string, array, object': function(name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes, options){
          return this.post('machineTypes', {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId,
            json: JSON.stringify({
              name: name,
              description: description,
              imageId: imageId,
              flavorId: flavorId,
              availabilityZone: availabilityZone,
              poolSize: poolSize,
              aquilonArchetype: aquilonArchetype,
              aquilonDomain: aquilonDomain,
              aquilonPersonality: aquilonPersonality,
              aquilonSandbox: aquilonSandbox,
              aquilonOSVersion: aquilonOSVersion,
              scopes: scopes
            })
          }, options);
        },
        'promise, string, string, string, string, string, number, string, string, string, string, string, array': function(timeout, name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes){
          return this.createMachineType(name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox. aquilonOSVersion, scopes, {timeout: timeout});
        },
        'string, string, string, string, string, number, string, string, string, string, string, array': function(name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes){
          return this.createMachineType(name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes, {});
        }
      });

      this.updateMachineType = helpers.overload({
        'number, string, string, string, string, string, number, string, string, string, string, string, array, object': function(id, name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes, options){
          return this.put('machineTypes/' + id, {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId,
            json: JSON.stringify({
              name: name,
              description: description,
              imageId: imageId,
              flavorId: flavorId,
              availabilityZone: availabilityZone,
              poolSize: poolSize,
              aquilonArchetype: aquilonArchetype,
              aquilonDomain: aquilonDomain,
              aquilonPersonality: aquilonPersonality,
              aquilonSandbox: aquilonSandbox,
              aquilonOSVersion: aquilonOSVersion,
              scopes: scopes
            })
          }, options);
        },
        'promise, number, string, string, string, string, string, number, string, string, string, string, string, array': function(timeout, id, name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes){
          return this.updateMachineType(id, name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox. aquilonOSVersion, scopes, {timeout: timeout});
        },
        'number, string, string, string, string, string, number, string, string, string, string, string, array': function(id, name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes){
          return this.updateMachineType(id, name, description, imageId, flavorId, availabilityZone, poolSize, aquilonArchetype, aquilonDomain, aquilonPersonality, aquilonSandbox, aquilonOSVersion, scopes, {});
        }
      });

      this.deleteMachineType = helpers.overload({
        'number, object': function(id, options){
          return this.delete('machineTypes/' + id, {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          }, options);
        },
        'promise, number': function(timeout, id){
          return this.deleteMachineType(id, {timeout: timeout});
        },
        'number': function(id){
          return this.deleteMachineType(id, {});
        }
      });

      this.deleteMachineTypeLogo = helpers.overload({
        'number, object': function(id, options){
          return this.delete('machineTypes/' + id + '/logo', {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId
          }, options);
        },
        'promise, number': function(timeout, id){
          return this.deleteMachineTypeLogo(id, {timeout: timeout});
        },
        'number': function(id){
          return this.deleteMachineTypeLogo(id, {});
        }
      });

      this.updateMachineTypeLogo = helpers.overload({
        'number, string, array, object': function(id, mimeType, data, options){

          options.queryParams = {
            icatUrl: facility.config().icatUrl,
            sessionId: icat.session().sessionId,
            mimeType: mimeType,
          }

          options.headers =  {
            'Content-Type': 'application/octet-stream'
          };

          options.transformRequest = [];

          return this.put('machineTypes/' + id + '/logo', data, options);
        },
        'promise, number, array, string': function(timeout, id, mimeType, data){
          return this.updateMachineTypeLogo(id, mimeType, data, {timeout: timeout});
        },
        'number, string, array': function(id, mimeType, data){
          return this.updateMachineTypeLogo(id, mimeType, data, {});
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