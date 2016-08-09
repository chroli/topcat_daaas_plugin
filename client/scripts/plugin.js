

registerTopcatPlugin(function(pluginUrl){
	return {
		scripts: [
			pluginUrl + 'scripts/controllers/my-jobs.controller.js'
		],

		stylesheets: [],

		configSchema: function(){
			this.attribute('site', function(){
				this.attribute('ijp', function(){
					
				});
			});
		},

		setup: function($uibModal, tc){

			tc.ui().registerMainTab('my-jobs', pluginUrl + 'views/my-jobs.html', {
				insertAfter: 'my-data',
				controller: 'MyJobsController as myJobsController'
			});

			tc.ui().registerCartButton('configure-job', {insertBefore: 'cancel'}, function(){
				$uibModal.open({
                    templateUrl : pluginUrl + 'views/my-jobs.html',
                    controller: 'MyJobsController as myJobsController',
                    size : 'lg'
                })
			});

			tc.ui().registerEntityActionButton('configure-job', function(){
				alert("Hello");
			});

		}
	};
});

