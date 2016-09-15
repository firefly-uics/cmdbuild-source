(function () {

	/**
	 * @link CMDBuild.view.administration.workflow.tabs.domains.GridPanel
	 */
	Ext.define('CMDBuild.view.administration.classes.tabs.domains.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.classes.tabs.Domains'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.classes.tabs.Domains}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.domainDescription,
						flex: 1
					},
					{
						dataIndex: 'descrdir',
						text: CMDBuild.Translation.directDescription,
						flex: 1
					},
					{
						dataIndex: 'descrinv',
						text: CMDBuild.Translation.inverseDescription,
						flex: 1
					},
					{
						dataIndex: 'class1',
						text: CMDBuild.Translation.origin,
						flex: 1
					},
					{
						dataIndex: 'class2',
						text: CMDBuild.Translation.destination,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.CARDINALITY,
						text: CMDBuild.Translation.cardinality,
						flex: 1
					},
					Ext.create('Ext.ux.grid.column.Tick', {
						dataIndex: 'md',
						text: CMDBuild.Translation.masterDetailInitials,
						iconAltText: CMDBuild.Translation.masterDetail,
						align: 'center',
						width: 30,
						fixed: true
					})
				],
				store: CMDBuild.proxy.classes.tabs.Domains.getStore()
			});

			this.callParent(arguments);

			this.getStore().on('load', function (store, records, successful, eOpts) {
				this.delegate.cmfg('onClassesTabDomainsIncludeInheritedCheck');
				this.delegate.cmfg('onClassesTabDomainsStoreLoad');
			}, this);
		},

		listeners: {
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onClassesTabDomainsItemDoubleClick');
			},
			select: function (row, record, index) {
				this.delegate.cmfg('onClassesTabDomainsRowSelect');
			}
		}
	});

})();
