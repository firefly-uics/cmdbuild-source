(function() {

	Ext.define('CMDBuild.view.management.common.widgets.CMOpenReport', {
		extend: 'Ext.panel.Panel',

		statics: {
			WIDGET_NAME: '.OpenReport'
		},

		layout: 'border',
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed',
		border: false,
		frame: false,

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.CMOpenReportController}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox} report format selection ComboBox
		 */
		formatCombo: {},

		/**
		 * @property {Array} fields to display in form
		 */
		formFields: [],

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;
			this.CMEVENTS = {
				saveButtonClick: 'cm-save-click'
			};

			this.formatCombo = Ext.create('Ext.form.field.ComboBox', {
				fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.createreport.format_label,
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: 'reportExtension',
				editable: false,
				disableKeyFilter: true,
				forceSelection: true,
				queryMode: 'local',

				store: Ext.create('Ext.data.ArrayStore', {
					autoDestroy: true,
					fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE, CMDBuild.core.proxy.CMProxyConstants.TEXT],
					data: [
						['pdf', 'PDF'],
						['csv', 'CSV'],
						['odt', 'ODT'],
						['rtf', 'RTF']
					]
				}),
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.TEXT,
				value: 'pdf'
			});

			this.formPanel = Ext.create('Ext.form.Panel', {
				autoScroll: true,
				frame: false,
				border: false,
				region: 'center',
				bodyCls: 'x-panel-body-default-framed',
				padding: '5',

				items: [this.formatCombo]
			});

			Ext.apply(this, {
				items: [this.formPanel]
			});

			this.callParent(arguments);

			this.addEvents(this.CMEVENTS.saveButtonClick);
		},

		/**
		 * Buttons that the owner panel add to itself
		 *
		 * @return {Ext.button.Button} as array
		 */
		getExtraButtons: function() {
			return [
				Ext.create('Ext.button.Button', {
					text: CMDBuild.Translation.common.buttons.confirm,
					name: 'saveButton',
					scope: this,

					handler: function() {
						this.delegate.cmOn('onSaveButtonClick');
					}
				})
			];
		},

		/**
		 * @param {String} extension
		 */
		forceExtension: function(extension) {
			if (extension) {
				this.formatCombo.setValue(extension);
				this.formatCombo.disable();
			} else {
				this.formatCombo.enable();
			}
		},

		/**
		 * Add the required attributes and disable fields if in readOnlyAttributes array
		 *
		 * @param {Array} attributes
		 * @param {Object} widgetConfiguration
		 */
		configureForm: function(attributes, widgetConfiguration) {
			this.formFields = []; // Reset fields array before add new ones

			if (!this.formPanelCreated) {
				this.formPanelCreated = true;

				// Add fields to form panel
				for (var i = 0; i < attributes.length; i++) {
					var attribute = attributes[i];
					var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);

					if (field) {

						// To disable if field name is contained in widgetConfiguration.readOnlyAttributes
						field.setDisabled(
							Ext.Array.contains(
								widgetConfiguration[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY_ATTRIBUTES],
								attribute[CMDBuild.core.proxy.CMProxyConstants.NAME]
							)
						);

						this.formFields.push(field);
						this.formPanel.add(field);
					}
				}
			}
		},

		/**
		 * @param {Object} parameters - Ex: { input_name: value, ...}
		 */
		fillFormValues: function(parameters) {
			for (var i = 0; i < this.formFields.length; i++) {
				var field = this.formFields[i];
				var value = parameters[field.name];

				if (value) {
					if (Ext.getClassName(field) == 'Ext.form.field.Date') {
						try {
							field.setValue(new Date(value));
						} catch (e) {
							field.setValue(value);
						}
					} else {
						field.setValue(value);
					}
				}
			}
		}
	});

})();