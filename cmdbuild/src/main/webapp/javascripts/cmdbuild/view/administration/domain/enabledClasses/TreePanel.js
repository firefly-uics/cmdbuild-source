(function() {

	Ext.define('CMDBuild.view.administration.domain.enabledClasses.TreePanel', {
		extend: 'Ext.tree.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.EnabledClasses}
		 */
		delegate: undefined,

		/**
		 * @cfg {Array}
		 */
		disabledClasses: [],

		/**
		 * @cfg {String}
		 */
		type: undefined,

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
						header: CMDBuild.Translation.enabled,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.ENABLED,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						renderer: this.rendererEnabledColumn
					},
				],
				store: this.delegate.buildClassesStore(this.disabledClasses, this.type)
			});

			this.callParent(arguments);
		},

		/**
		 * Hide checkbox for superClasses
		 *
		 * @param {Object} value
		 * @param {Object} metaData
		 * @param {CMDBuild.model.Classes.domainsTreePanel} record
		 *
		 * @return {String} or null
		 */
		rendererEnabledColumn: function(value, metaData, record) {
			if (record.childNodes.length > 0) {
				return '';
			} else {
				return Ext.create('Ext.grid.column.CheckColumn').renderer(arguments); // Call the original renderer method
			}
		},

		/**
		 * SetDisabled state only if tree has more than one node, otherwise tree still disabled
		 *
		 * @param {Boolean} state
		 *
		 * @override
		 */
		setDisabled: function(state) {
			if (
				Ext.isEmpty(this.getStore().getRootNode().childNodes)
				|| ( // if root has more than one child and that child is not a superclass
					this.getStore().getRootNode().childNodes.length <= 1
					&& this.getStore().getRootNode().getChildAt(0).childNodes.length <= 1 // TODO: use model function isSuperClass
				)
			) {
				return this.callParent([true]);
			} else {
				return this.callParent(arguments);
			}
		}
	});

})();