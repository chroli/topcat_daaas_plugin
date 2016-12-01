

registerTopcatPlugin(function(pluginUrl){
	return {
		scripts: [
			[pluginUrl + 'bower_components/jquery-no-vnc/dist/jquery-no-vnc.js', function(){
				return $.fn.noVnc !== undefined;
			}],
			pluginUrl + 'scripts/controllers/create-machine.js',
			pluginUrl + 'scripts/controllers/machine.js',
			pluginUrl + 'scripts/controllers/my-machines.js',
			pluginUrl + 'scripts/controllers/admin-machine-templates.js',

			pluginUrl + 'scripts/directives/fullscreen.js',
			pluginUrl + 'scripts/directives/vnc.js',

			pluginUrl + 'scripts/services/tc-daaas.js'
		],

		stylesheets: [],

		configSchema: {

		},

		extend: {
			facility: function(){

			}
		},

		setup: function(tc, tcDaaas){

			tc.ui().registerMainTab('my-machines', pluginUrl + 'views/my-machines.html', {
				insertAfter: 'my-data',
				controller: 'MyMachinesController as myMachinesController'
			});

			tc.ui().registerAdminTab('machine-templates', pluginUrl + 'views/admin-machine-templates.html', {
				insertAfter: 'downloads',
				controller: 'AdminMachineTemplatesController as adminMachineTemplatesController',
				multiFacility: true
			});

			var daaas;
			tc.daaas = function(){
				if(!daaas) daaas = tcDaaas.create(pluginUrl);
				return daaas;
			};

		},

		login: function(){
			//'this' is the facility
			//potentually register tabs etc
			//can return promise
		},

		logout: function(){
			//'this' is the facility
			//potentually un register tabs etc
			//can return promise
		}

	};
});

