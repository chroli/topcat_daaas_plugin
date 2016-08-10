

registerTopcatPlugin(function(pluginUrl){
	return {
		scripts: [
			pluginUrl + 'scripts/controllers/create-machine.js',
			pluginUrl + 'scripts/controllers/machine.js',
			pluginUrl + 'scripts/controllers/my-machines.js',

			pluginUrl + 'scripts/directives/fullscreen.js',
			pluginUrl + 'scripts/directives/vnc.js',

			pluginUrl + 'scripts/services/tc-daaas.js',
		],

		stylesheets: [],

		configSchema: function(){
			
		},

		setup: function(tc, tcDaaas){

			tc.ui().registerMainTab('my-machines', pluginUrl + 'views/my-machines.html', {
				insertAfter: 'my-data',
				controller: 'MyMachinesController as myMachinesController'
			});

			var daaas;
			tc.daaas = function(){
				if(!daaas) daaas = tcDaaas.create(pluginUrl);
				return daaas;
			};

		}
	};
});

