(function() {

	Ext.define('CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid', {
		extend: 'CMDBuild.view.management.common.CMCardGrid',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		cmVisible: true,

		/**
		 * @property {Object}
		 */
		paramsToLoadWhenVisible: undefined,

		listeners: {
			deselect: function(selectionModel, record, index, eOpts) {
				this.delegate.cmOn('onDeselect', { record: record });
			},

			select: function(selectionModel, record, index, eOpts) {
				this.delegate.cmOn('onSelect', { record: record });
			},

			show: function(grid, eOpts) {
				this.delegate.cmOn('onGridShow');
			}
		},

		/**
		 * @return {Array}
		 *
		 * @override
		 */
		buildExtraColumns: function() {
			return [
				{
					xtype: 'actioncolumn',
					align: 'center',
					width: 25,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							icon: 'images/icons/zoom.png',
							tooltip: CMDBuild.Translation.viewDetails,
							scope: this,

							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onRowViewButtonClick', record);
							}
						}
					]
				},
				{
					xtype: 'actioncolumn',
					width: 30,
					align: 'center',
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							iconCls: 'modify',
							tooltip: CMDBuild.Translation.editRow,
							scope: this,

							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onRowEditButtonClick', record);
							},

							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return !this.delegate.widgetConf[CMDBuild.core.constants.Proxy.ALLOW_CARD_EDITING];
							}
						}
					]
				}
			];
		},

		/**
		 * @param {Int} cardId
		 */
		selectByCardId: function(cardId) {
			if (typeof cardId == 'number') {
				var recIndex = this.getStore().find('Id', cardId);

				if (recIndex >= 0)
					this.getSelectionModel().select(recIndex, true);
			}
		},

		/**
		 * @param {Boolean} visible
		 */
		setCmVisible: function(visible) {
			this.cmVisible = visible;

			if (this.paramsToLoadWhenVisible) {
				this.updateStoreForClassId(this.paramsToLoadWhenVisible[CMDBuild.core.constants.Proxy.CLASS_ID], this.paramsToLoadWhenVisible.o);
				this.paramsToLoadWhenVisible = null;
			}

			this.fireEvent('cmVisible', visible);
		},

		/**
		 * @param {Int} classId
		 * @param {Object} o
		 *
		 * @override
		 */
		updateStoreForClassId: function(classId, o) {
			if (this.cmVisible) {
				this.callParent(arguments);

				this.paramsToLoadWhenVisible = null;
			} else {
				this.paramsToLoadWhenVisible = {};
				this.paramsToLoadWhenVisible[CMDBuild.core.constants.Proxy.CLASS_ID] = classId;
			}
		}
	});

})();