(function() {

	Ext.define('CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid', {
		extend: 'CMDBuild.view.management.common.CMCardGrid',

		/**
		 * @property {Object}
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
			beforeitemclick: function(gridView, record, item, index, e, eOpts) {
				this.delegate.cmOn(
					'onCellClick',
					{
						record: record,
						event: e
					}
				);
			},

			deselect: function(selectionModel, record, index, eOpts) {
				this.delegate.cmOn('onDeselect', { record: record });
			},

			itemdblclick: function(gridView, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleclick', { record: record });
			},

			select: function(selectionModel, record, index, eOpts) {
				this.delegate.cmOn('onSelect', { record: record });
			},

			show: function(grid, eOpts) {
				this.delegate.cmOn('onGridShow');
			}
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
				this.updateStoreForClassId(this.paramsToLoadWhenVisible[CMDBuild.core.proxy.CMProxyConstants.CLASS_ID], this.paramsToLoadWhenVisible.o);
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
				this.paramsToLoadWhenVisible[CMDBuild.core.proxy.CMProxyConstants.CLASS_ID] = classId;
			}
		}
	});

})();