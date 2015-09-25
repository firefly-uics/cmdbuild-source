(function() {

	Ext.define('CMDBuild.core.proxy.Attachment', {
		alternateClassName: 'CMDBuild.ServiceProxy.attachment', // Legacy class name

		requires: [
			'CMDBuild.core.proxy.CMProxy',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.CMAttachment', // TODO: refactor
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.attachments.getAttachmentList,
					reader: {
						type: 'json',
						root: 'rows'
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: {
					property: 'Filename',
					direction: 'ASC'
				},
			});
		},

		// TODO: refactor

		download: function(params) {
			var url = 'services/json/attachments/downloadattachment?' + Ext.urlEncode(params);

			window.open(url, "_blank");
		},

		getattachmentdefinition: function(p) {
			p.method = "GET";
			p.url = "services/json/attachments/getattachmentscontext";

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		remove: function(p) {
			p.method = "POST";
			p.url = "services/json/attachments/deleteattachment";

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	});

})();