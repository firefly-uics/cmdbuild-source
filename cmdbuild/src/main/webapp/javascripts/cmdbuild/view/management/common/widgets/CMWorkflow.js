(function() {
	Ext.define("CMDBuild.view.management.common.widgets.CMWorkflow", {
		extend: "Ext.panel.Panel",

		statics: {
			WIDGET_NAME: ".Workflow"
		},

		formatCombo: {},
		attributeList: [],
		formFields: [],

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;
			this.CMEVENTS = {
					saveButtonClick: "cm-save-click",
					advanceButtonClick: "cm-advance-click"
			};

	
			this.formPanel = new Ext.form.FormPanel({
				timeout : CMDBuild.Config.defaultTimeout * 1000,
				monitorValid : true,
				autoScroll : true,
				frame : false,
				border: false,
				region : 'center',
				bodyCls: "x-panel-body-default-framed",
				padding: "5",
				items : []
			});

			this.widgets = new CMDBuild.view.management.common.widget.CMWidgetButtonsPanelPopup({
				delegate: this,
				region: 'east',
				hideMode: 'offsets',
				cls: "cmborderleft",
				autoScroll: true,
				frame: true,
				border: false,
				items: [],
				_hidden: true,
				hidden: true
			});
			_CMUtils.forwardMethods(this, this.widgets, ["removeAllButtons", "addWidget"]);
			
			Ext.apply(this, {
				layout: 'border',
				buttonAlign: 'center',
				items: [this.formPanel, this.widgets],
				cls: "x-panel-body-default-framed",
				border: false,
				frame: false
			});

			this.callParent(arguments);

			this.addEvents(this.CMEVENTS.saveButtonClick);
			this.addEvents(this.CMEVENTS.advanceButtonClick);
		},

		// buttons that the owner panel add to itself
		setDelegate: function(delegate) {
			this.delegate = delegate;
			this.widgets.delegate = delegate;
		},
		getExtraButtons: function() {
			var me = this;
			return [new Ext.Button( {
				text : CMDBuild.Translation.common.btns.save,
				name : 'saveButton',
				handler: function() {
					me.fireEvent(me.CMEVENTS.saveButtonClick);
				}
			}), new Ext.Button( {
				text : CMDBuild.Translation.common.buttons.workflow.advance,
				name : 'advanceButton',
				handler: function() {
					me.fireEvent(me.CMEVENTS.advanceButtonClick);
				}
			})];
		},

	
		// add the required attributes
		configureForm: function(attributes, parameters) {
			if (!this.formPanelCreated) {
				this.formPanelCreated = true;
				// add fields to form panel
				for (var i=0; i<attributes.length; i++) {
					var attribute = attributes[i],
						field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);
	
					if (field) {
						this.formFields[i] = field;
						this.formPanel.add(field);
					}
				}
				this.formPanel.doLayout();
			}
		},
	
		getFormForTemplateResolver: function() {
			return this.formPanel.getForm();
		},
		
		getWidgetButtonsPanel: function() {
			return this.widgets;
		},
		fillFormValues : function(parameters) {
				for ( var i = 0; i < this.formFields.length; i++) {
					var field = this.formFields[i], value = parameters[field.name]
	
					if (value) {
						if (Ext.getClassName(field) == "Ext.form.field.Date") {
							try {
								field.setValue(new Date(value));
							} catch (e) {
								field.setValue(value);
							}
						} else if (Ext.getClassName(field) == "CMDBuild.Management.ReferenceField.Field"
												&& value == parseInt(value)) {
							field.setValue(parseInt(value));
						} else {
							field.setValue(value);
						}
					}
				}
			}
		});

})();