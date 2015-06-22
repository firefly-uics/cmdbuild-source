(function() {

	Ext.define('CMDBuild.view.administration.localizations.advancedTable.SectionDomainsPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.SectionDomains}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.localizations.common.AdvancedTableGrid}
		 */
		grid: undefined,

		bodyCls: 'cmgraypanel',
		layout: 'fit',
		title: '@@ Domains',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.localizations.common.AdvancedTableGrid', {
						delegate: this.delegate,
						columns: this.delegate.cmfg('onAdvancedTableBuildColumns'),
						store: this.delegate.cmfg('onAdvancedTableBuildStore')
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onAdvancedTableDomainShow');
			}
		}
	});

})();