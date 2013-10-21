Ext.define('CMDBuild.model.CMEmailTemplateModel', {
	extend: 'Ext.data.Model',
	fields: [{
		name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME,
		type: 'string'
	}, {
		name: CMDBuild.ServiceProxy.parameter.TO,
		type: 'string'
	}, {
		name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
		type: 'string'
	}, {
		name: CMDBuild.ServiceProxy.parameter.CC,
		type: 'string'
	}, {
		name: CMDBuild.ServiceProxy.parameter.BCC,
		type: 'string'
	}, {
		name: CMDBuild.ServiceProxy.parameter.SUBJECT,
		type: 'string'
	}, {
		name: CMDBuild.ServiceProxy.parameter.BODY,
		type: 'string'
	}, {
		name: CMDBuild.ServiceProxy.parameter.CLASS_ID,
		type: 'string'
	}],

	getTemplateName: function() {
		return this.get(CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME);
	},

	isGlobal: function() {
		return !!!this.get(CMDBuild.ServiceProxy.parameter.CLASS_ID);
	}
});