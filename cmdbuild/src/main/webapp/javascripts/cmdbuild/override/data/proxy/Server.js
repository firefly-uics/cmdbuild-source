(function() {

	Ext.define('CMDBuild.override.data.proxy.Server', {
		override: 'Ext.data.proxy.Server',

		limitParam: undefined, // Avoid to send limit in server calls

		pageParam: undefined, // Avoid to send page in server calls

		startParam: undefined // Avoid to send start in server calls
	});

})();