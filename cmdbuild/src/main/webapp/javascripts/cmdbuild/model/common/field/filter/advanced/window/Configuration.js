(function() {

	Ext.define('CMDBuild.model.common.field.filter.advanced.window.Configuration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.MODE, type: 'string', defaultValue: 'field' }, // ['field', 'grid']
			{
				name: CMDBuild.core.constants.Proxy.TABS,
				type: 'auto',
				defaultValue: {
					attributes: {
						selectAtRuntimeCheckDisabled: false
					}
				}
			}
		]
	});

})();