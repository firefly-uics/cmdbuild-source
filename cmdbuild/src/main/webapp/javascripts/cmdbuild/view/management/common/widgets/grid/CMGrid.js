(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.CMGrid', {
		extend: 'Ext.panel.Panel',

		statics: {
			WIDGET_NAME: '.Grid'
		},

		delegate: undefined,

		autoScroll: true,
		border: false,
		frame: false,

		layout: {
			type: 'border'
		},

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;

			this.addButton = Ext.create('Ext.button.Button', {
				iconCls: 'add',
				text: CMDBuild.Translation.row_add,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onAddRowButtonClick');
				}
			});

			// File upload fields
				this.classIdField = Ext.create('Ext.form.field.Hidden', {
					name: 'idClass'
				});

				this.csvFileField = Ext.create('Ext.form.field.File', {
					name: 'filecsv',
					allowBlank: true,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});

				this.csvUploadButton = Ext.create('Ext.button.Button', {
					text: '@@ Upload',
					margins: '0 0 0 5',
					scope: this,

					handler: function() {
						this.delegate.cmOn('onCSVUploadButtonClick');
					}
				});

				this.csvSeparatorCombo = Ext.create('Ext.form.field.ComboBox', {
					name: 'separator',
					fieldLabel: CMDBuild.Translation.management.modutilities.csv.separator,
					labelWidth: CMDBuild.LABEL_WIDTH,
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
					frame: false,
					border: false,
					encoding: 'multipart/form-data',
					fileUpload: true,
					monitorValid: true,

					items: [this.classIdField, this.csvFileField, this.csvSeparatorCombo, this.csvUploadButton]
				});

				this.csvUploadContainer = Ext.create('Ext.form.FieldContainer', {
					fieldLabel: '@@ Import from CSV',
					border: false,
					considerAsFieldToDisable: true,
					labelWidth: CMDBuild.LABEL_WIDTH,
					labelStyle: 'padding: 3px 0px 0px 3px',
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					region: 'north',

					layout: {
						type: 'hbox',
						padding:'3 0 3 3'
					},

					items: [this.csvUploadForm]
				});
			// EDN: File upload fields

			this.grid = Ext.create('CMDBuild.view.management.common.widgets.grid.CMGridPanel', {
				region: 'center'
			});

			Ext.apply(this, {
				tbar: [this.addButton],
				items: [this.csvUploadContainer, this.grid]
			});

			this.callParent(arguments);
		}
	});

})();