(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.widget.manageEmail.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATES, type: 'auto', defaultValues: [] }, // Email template array
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		]
	});

})();