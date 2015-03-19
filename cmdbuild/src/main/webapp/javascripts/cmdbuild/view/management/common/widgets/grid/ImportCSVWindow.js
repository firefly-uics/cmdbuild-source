(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.ImportCSVWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.Grid'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.ImportCSV}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.buttons.AbortButton}
		 */
		abortButton: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		classIdField: undefined,

		/**
		 * @property {Ext.form.field.File}
		 */
		csvFileField: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		csvImportModeCombo: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		csvSeparatorCombo: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		csvUploadForm: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		uploadButton: undefined,

		buttonAlign: 'center',
		border: false,
		defaultSizeW: 0.90,
		autoHeight: true,
		title: CMDBuild.Translation.importFromCSV,

		initComponent: function() {
			// Buttons configuration
				this.abortButton = Ext.create('CMDBuild.buttons.AbortButton', {
					scope: this,

					handler: function() {
						this.delegate.cmOn('onImportCSVAbortButtonClick');
					}
				});

				this.uploadButton = Ext.create('Ext.button.Button', {
					scope: this,
					text: CMDBuild.Translation.upload,

					handler: function() {
						this.delegate.cmOn('onImportCSVUploadButtonClick');
					}
				});
			// END: Buttons configuration

			this.classIdField = Ext.create('Ext.form.field.Hidden', {
				name: 'idClass',
				value: this.delegate.classId
			});

			this.csvFileField = Ext.create('Ext.form.field.File', {
				name: 'filecsv',
				fieldLabel: CMDBuild.Translation.csvFile,
				labelWidth: CMDBuild.LABEL_WIDTH,
				labelAlign: 'right',
				allowBlank: true,
				width: CMDBuild.BIG_FIELD_WIDTH
			});

			this.csvSeparatorCombo = Ext.create('Ext.form.field.ComboBox', {
				name: 'separator',
				fieldLabel: CMDBuild.Translation.separator,
				labelWidth: CMDBuild.LABEL_WIDTH,
				labelAlign: 'right',
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				width: 200,
				value: ';',
				editable: false,
				allowBlank: false,

				store: CMDBuild.core.proxy.widgets.Grid.getCsvSeparatorStore(),
				queryMode: 'local'
			});

			this.csvImportModeCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.proxy.CMProxyConstants.MODE,
				fieldLabel: CMDBuild.Translation.mode,
				labelWidth: CMDBuild.LABEL_WIDTH,
				labelAlign: 'right',
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				width: CMDBuild.MEDIUM_FIELD_WIDTH,
				value: 'replace',
				editable: false,
				allowBlank: false,

				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.VALUE],
					data: [
						[CMDBuild.Translation.add, 'add'],
						[CMDBuild.Translation.replace , 'replace']
					]
				}),
				queryMode: 'local'
			});

			this.csvUploadForm = Ext.create('Ext.form.Panel', {
				frame: true,
				border: false,
				encoding: 'multipart/form-data',
				fileUpload: true,
				monitorValid: true,

				items: [this.classIdField, this.csvFileField, this.csvSeparatorCombo, this.csvImportModeCombo]
			});

			Ext.apply(this, {
				items: [this.csvUploadForm],
				buttons: [this.uploadButton, this.abortButton]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.width = this.width * this.defaultSizeW;
		}
	});

})();