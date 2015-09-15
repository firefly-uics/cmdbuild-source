(function() {

	Ext.define('CMDBuild.view.administration.accordion.Domain', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.accordion.Domain}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.domains,

		/**
		 * @return {Array} out
		 */
		buildTreeStructure: function() {
			var out = [];

			Ext.Object.each(_CMCache.getDomains(), function(id, domain, myself) {
				out.push({
					id: domain.get(CMDBuild.core.proxy.Constants.ID),
					text: domain.get(CMDBuild.core.proxy.Constants.DESCRIPTION),
					leaf: true,
					cmName: this.cmName,
					iconCls: 'domain'
				});
			}, this);

			return out;
		}
	});

})();