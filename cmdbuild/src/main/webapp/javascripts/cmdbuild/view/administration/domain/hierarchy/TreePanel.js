(function() {

	Ext.define('CMDBuild.view.administration.domain.hierarchy.TreePanel', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Hierarchy}
		 */
		delegate: undefined,

		/**
		 * @cfg {Array}
		 */
		disabledClasses: [],

		autoScroll: true,
		border: true,
		collapsible: false,
		enableColumnHide: false,
		flex: 1,
		frame: false,
		hideCollapseTool: true,
		rootVisible: false,
		sortableColumns: false, // BUGGED in ExtJs 4.2, workaround setting sortable: false to columns

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						xtype: 'treecolumn',
						text: CMDBuild.Translation.className,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						flex: 1,
						sortable: false,
						draggable: false
					},
					{
						xtype: 'checkcolumn',
						header: '@@ Enabled',
						dataIndex: 'enabled',
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					},
				],
				store: this.delegate.buildClassesStore(this.disabledClasses)
			});

			this.callParent(arguments);
		}
	});

})();