(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.CMImportCSVWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		defaultSizeW: 0.90,
		defaultSizeH: 0.20,

		// Configurations
			delegate: undefined,
			classId: undefined, // Target classId to use in form submit (importCSV)
		// END: Configurations

		buttonAlign: 'center',

		initComponent: function() {
			var me = this;

			// Buttons configuration
				this.cancelButton = Ext.create('CMDBuild.buttons.AbortButton', {
					scope: this,

					handler: function() {
						this.destroy();
					}
				});

				this.csvUploadButton = Ext.create('Ext.button.Button', {
					scope: this,
					text: '@@ Upload',

					handler: function() {
						this.delegate.cmOn('onCSVUploadButtonClick');
					}
				});
			// END: Buttons configuration

			this.classIdField = Ext.create('Ext.form.field.Hidden', {
				name: 'idClass',
				value: this.classId
			});

			this.csvFileField = Ext.create('Ext.form.field.File', {
				name: 'filecsv',
				fieldLabel: '@@ CSV file',
				labelWidth: CMDBuild.LABEL_WIDTH,
				labelAlign: 'right',
				allowBlank: true,
				width: CMDBuild.BIG_FIELD_WIDTH
			});

			this.csvSeparatorCombo = Ext.create('Ext.form.field.ComboBox', {
				name: 'separator',
				fieldLabel: '@@ Separator',
				labelWidth: CMDBuild.LABEL_WIDTH,
				labelAlign: 'right',
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				width: 200,
				value: ';',
				editable: false,
				allowBlank: false,

				store: CMDBuild.core.proxy.widgets.CMProxyWidgetGrid.getCsvSeparatorStore(),
				queryMode: 'local'
			});

			this.csvUploadForm = Ext.create('Ext.form.Panel', {
				cls: 'x-panel-body-default-framed',
				bodyCls: 'x-panel-body-default-framed',
				bodyStyle: {
					padding: '5px 5px 0px 5px'
				},
				frame: false,
				border: false,
				encoding: 'multipart/form-data',
				fileUpload: true,
				monitorValid: true,

				items: [this.classIdField, this.csvFileField, this.csvSeparatorCombo]
			});

			Ext.apply(this, {
				items: [this.csvUploadForm],
				buttons: [this.csvUploadButton, this.cancelButton]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;
		}
	});

})();