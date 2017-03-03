

registerTopcatPlugin(function(pluginUrl){
	return {
		scripts: [
			[pluginUrl + 'bower_components/jquery-no-vnc/dist/jquery-no-vnc.js', function(){
				return $.fn.noVnc !== undefined;
			}],
			pluginUrl + 'scripts/controllers/create-machine.js',
			pluginUrl + 'scripts/controllers/machine.js',
			pluginUrl + 'scripts/controllers/my-machines.js',
			pluginUrl + 'scripts/controllers/admin-machine-types.js',
			pluginUrl + 'scripts/controllers/admin-create-machine-type.js',
			pluginUrl + 'scripts/controllers/admin-edit-machine-type.js',
			pluginUrl + 'scripts/controllers/share-machine.js',

			pluginUrl + 'scripts/directives/fullscreen.js',
			pluginUrl + 'scripts/directives/vnc.js',

			pluginUrl + 'scripts/services/tc-admin-daaas.js',
			pluginUrl + 'scripts/services/tc-user-daaas.js'
		],

		stylesheets: [
			pluginUrl + 'styles/main.css'
		],

		configSchema: {

		},

		extend: {
			admin: function(tcAdminDaaas){
				var daaas;

				this.daaas = function(){
					if(!daaas) daaas = tcAdminDaaas.create(pluginUrl, this);
					return daaas;
				};
			},

			user: function(tcUserDaaas){
				var daaas;

				this.daaas = function(){
					if(!daaas) daaas = tcUserDaaas.create(pluginUrl, this);
					return daaas;
				};
			}
			
		},

		setup: function(tc){

			tc.ui().registerMainTab('my-machines', pluginUrl + 'views/my-machines.html', {
				insertAfter: 'my-data',
				controller: 'MyMachinesController as myMachinesController',
				multiFacility: true
			});

			tc.ui().registerAdminTab('machine-types', pluginUrl + 'views/admin-machine-types.html', {
				insertAfter: 'downloads',
				controller: 'AdminMachineTypesController as adminMachineTypesController',
				multiFacility: true
			});

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

