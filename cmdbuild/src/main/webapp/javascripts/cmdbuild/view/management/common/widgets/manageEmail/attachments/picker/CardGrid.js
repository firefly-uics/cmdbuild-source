(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.attachments.picker.CardGrid', {
		extend: 'CMDBuild.view.management.common.CMCardGrid',

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Attachments}
		 */
		delegate: undefined,

		cmAdvancedFilter: false,
		cmAddGraphColumn: false,
		cmAddPrintButton: false,

		border: false,

		listeners: {
			load: function(store, records, successful, eOpts) {
				this.delegate.cmOn('onPickerWindowCardGridStoreLoad', records);
			},

			select: function(selectionModel, record, index, eOpts) {
				this.delegate.cmOn('onPickerWindowCardSelected', record);
			}
		}
	});

})();