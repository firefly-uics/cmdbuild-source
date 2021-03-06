(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.translatable.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.FIELD, type: 'string' }, // Property name to translate
			{ name: CMDBuild.core.constants.Proxy.FIELD_TYPE, type: 'string', defaultValue: 'text' }, // Managed values: text
			{ name: CMDBuild.core.constants.Proxy.IDENTIFIER, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.OWNER, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.TRANSLATIONS, type: 'auto', defaultValue: {} }, // CMDBuild.model.common.field.translatable.Window
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' } // Managed values: class, attributeclass, domain, attributedomain, filter, instancename, lookupvalue, menuitem, report, view, classwidget
		]
	});

})();
