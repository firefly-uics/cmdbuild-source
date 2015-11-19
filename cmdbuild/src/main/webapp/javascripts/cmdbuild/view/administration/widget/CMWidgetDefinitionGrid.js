(function() {

	Ext.define('CMDBuild.view.administration.widget.CMWidgetDefinitionGrid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.widget.WidgetDefinition'
		],

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						text: CMDBuild.Translation.administration.modClass.widgets.commonFields.type,
						dataIndex: CMDBuild.core.constants.Proxy.TYPE,
						flex: 1,

						renderer: function(value) {
							switch (value) {
								case '.OpenReport':
									return CMDBuild.Translation.createReport;

								default:
									return CMDBuild.Translation.administration.modClass.widgets[value].title;
							}
						}
					},
					{
						text: CMDBuild.Translation.administration.modClass.widgets.commonFields.buttonLabel,
						dataIndex: CMDBuild.core.constants.Proxy.LABEL,
						flex: 2
					},
					{
						xtype: 'checkcolumn',
						text: CMDBuild.Translation.administration.modClass.widgets.commonFields.active,
						dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
						width: 60,
						cmReadOnly: true
					}
				],
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.widget.WidgetDefinition',
					data: []
				})
			});

			this.callParent(arguments);
		},

		/**
		 * @param {CMDBuild.model.widget.WidgetDefinition} record
		 * @param {Boolean} selectAfter
		 */
		addRecord: function(record, selectAfter) {
			this.removeRecordWithId(record.get(CMDBuild.core.constants.Proxy.ID));

			var addedRec = this.getStore().add(record);

			if (selectAfter)
				this.getSelectionModel().select(addedRec);
		},

		clearSelection: function() {
			this.getSelectionModel().deselectAll();
		},

		/**
		 * @return {Int}
		 */
		count: function() {
			return this.getStore().count();
		},

		/**
		 * @param {Int} recordId
		 */
		removeRecordWithId: function(recordId) {
			var record = this.getStore().getById(recordId);

			if (!Ext.Object.isEmpty(record))
				this.getStore().remove(record);
		},

		removeAllRecords: function() {
			this.getStore().removeAll();
		}
	});

})();