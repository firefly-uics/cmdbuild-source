(function() {

	Ext.define('CMDBuild.view.administration.accordion.Domain', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.accordion.Domain}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.domains,

		/**
		 * @return {Array} out
		 */
		buildTreeStructure: function() {
			var out = [];

			Ext.Object.each(_CMCache.getDomains(), function(id, domain, myself) {
				out.push({
					id: domain.get(CMDBuild.core.proxy.CMProxyConstants.ID),
					text: domain.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION),
					leaf: true,
					cmName: 'domain',
					iconCls: 'domain'
				});
			}, this);

			return out;
		}
	});

})();