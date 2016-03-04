(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.picker.CardGrid', {
		extend: 'CMDBuild.view.management.common.CMCardGrid',

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Picker}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		cmAddGraphColumn: false,

		/**
		 * @cfg {Boolean}
		 */
		cmAddPrintButton: false,

		/**
		 * @cfg {Boolean}
		 */
		cmAdvancedFilter: false,

		border: false,

		listeners: {
			load: function (store, records, successful, eOpts) {
				this.delegate.cmfg('onTabEmailAttachmentPickerWindowCardGridStoreLoad', records);
			},

			select: function (selectionModel, record, index, eOpts) {
				this.delegate.cmfg('onTabEmailAttachmentPickerWindowCardSelected', record);
			}
		}
	});

})();
