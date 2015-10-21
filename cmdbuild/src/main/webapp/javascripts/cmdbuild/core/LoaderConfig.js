(function() {

	Ext.require('CMDBuild.core.Utils');

	var extVersion = CMDBuild.core.Utils.getExtJsVersion({
		release: false
	});

	Ext.Loader.setConfig({
		enabled: true,
		paths: {
			'CMDBuild.app': './javascripts/cmdbuild',
			'CMDBuild': './javascripts/cmdbuild',
			'Ext.ux': './javascripts/ext-' + extVersion + '-ux',
			'Logger': './javascripts/log'
		}
	});

})();