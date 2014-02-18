(function() {

	var tr = CMDBuild.Translation.administration.setup;

	Ext.define('CMDBuild.view.administration.accordion.CMConfigurationAccordion', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: tr.setupTitle,
		cmName: 'setup',
		hideMode: 'offsets',

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();

			var children = [{
				text: tr.cmdbuild.menuTitle,
				leaf: true,
				cmName: 'modsetupcmdbuild'
			}];

			if (!_CMUIConfiguration.isCloudAdmin()) {
				children = children.concat([
					{
						text: tr.workflow.menuTitle,
						leaf: true,
						cmName: 'modsetupworkflow'
					},
					{
						text: tr.email.title,
						leaf: false,
						expanded: true,
						children: [
							{
								text: tr.email.accounts.title,
								leaf: true,
								cmName: 'setupEmailAccounts'
							},
							{
								text: tr.email.templates.title,
								leaf: true,
								cmName: 'setupEmailTemplates'
							}
						]
					},
					{
						text: tr.graph.menuTitle,
						leaf: true,
						cmName: 'modsetupgraph'
					},
					{
						text: tr.dms.menuTitle,
						leaf: true,
						cmName: 'modsetupalfresco'
					},
					{
						text: tr.gis.title,
						leaf: true,
						cmName: 'modsetupgis'
					},
					{
						text: tr.server.menuTitle,
						leaf: true,
						cmName: 'modsetupserver'
					}
				]);
			}

			root.appendChild(children);
		}
	});

})();