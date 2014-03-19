(function() {

	var translation = CMDBuild.Translation.administration.tasks.taskWorkflow;

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		filterWindow: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAttributeComboSelect':
					return this.onAttributeComboSelect(param);

				case 'onWorkflowSelected':
					return this.onWorkflowSelected(param.name);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Workflow attribute store builder for onWorkflowSelected event
		 */
		buildWorkflowAttributesStore: function(attributes) {
			if (attributes) {
				var data = [];

				for (var key in attributes) {
					data.push({ value: key });
				}

				return Ext.create('Ext.data.Store', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE],
					data: data,
					autoLoad: true
				});
			}
		},

		cleanServerAttributes: function(attributes) {
			var out = {};

			for (var i = 0, l = attributes.length; i < l; ++i) {
				var attr = attributes[i];

				out[attr.name] = '';
			}

			return out;
		},

		checkWorkflowComboSelected: function() {
			if (this.getWorkflowComboValue())
				return true;

			return false;
		},

		getWorkflowComboValue: function() {
			return this.view.workflowCombo.getValue();
		},

		fillActive: function(value) {
			this.view.activeField.setValue(value);
		},

		fillAttributesGrid: function(data) {
			this.view.attributesTable.fillWithData(data);
		},

		fillDescription: function(value) {
			this.view.descriptionField.setValue(value);
		},

		fillId: function(value) {
			this.view.idField.setValue(value);
		},

		fillWorkflowCombo: function(workflowName) {
			this.view.workflowCombo.setValue(workflowName);
		},

		onAttributeComboSelect: function(rowIndex) {
			this.view.attributesTable.cellEditing.startEditByPosition({ row: rowIndex, column: 1 });
		},

		onWorkflowSelected: function(name, modify) {
			var me = this;

			if (typeof modify === 'undefined')
				modify = false;

			CMDBuild.core.serviceProxy.CMProxyTasks.getWorkflowAttributes({
				params: {
					className: name
				},
				success: function(response) {
					var ret = Ext.JSON.decode(response.responseText);

					me.view.attributesTable.keyEditorConfig.store = me.buildWorkflowAttributesStore(me.cleanServerAttributes(ret.attributes));
					if (!modify) {
						me.view.attributesTable.store.removeAll();
						me.view.attributesTable.store.insert(0, { key: '', value: '' });
						me.view.attributesTable.cellEditing.startEditByPosition({ row: 0, column: 0 });
						me.setDisabledAttributesTable(false);
					}
				}
			});
		},

		setDisabledAttributesTable: function(state) {
			this.view.attributesTable.setDisabled(state);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		height: '100%',
		overflowY: 'auto',

		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: CMDBuild.Translation.administration.tasks.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				value: me.taskType,
				disabled: true,
				cmImmutable: true,
				readOnly: true
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.ServiceProxy.parameter.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.ServiceProxy.parameter.ACTIVE,
				fieldLabel: CMDBuild.Translation.administration.tasks.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH
			});

			this.attributesTable = Ext.create('CMDBuild.view.administration.common.CMDynamicKeyValueGrid', {
				title: translation.workflowAttributes,
				id: 'workflowAttributesGrid',
				keyLabel: CMDBuild.Translation.name,
				valueLabel: CMDBuild.Translation.value,
				disabled: true,
				keyEditorConfig: {
					xtype: 'combo',
					valueField: CMDBuild.ServiceProxy.parameter.VALUE,
					displayField: CMDBuild.ServiceProxy.parameter.VALUE,
					forceSelection: true,
					editable: false,
					allowBlank: false,
					listeners: {
						'select': function(combo, records, eOpts) {
							me.delegate.cmOn(
								'onAttributeComboSelect',
								me.attributesTable.store.indexOf(me.attributesTable.getSelectionModel().getSelection()[0])
							);
						}
					}
				}
			});

			this.workflowCombo = Ext.create('Ext.form.field.ComboBox', {
				id: 'workflowCombo',
				name: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.administration.tasks.workflow,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				store: CMDBuild.core.proxy.CMProxyTasks.getWorkflowsStore(),
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				labelWidth: CMDBuild.LABEL_WIDTH,
				forceSelection: true,
				editable: false,
				allowBlank: false,
				listeners: {
					'select': function() {
						me.delegate.cmOn(
							'onWorkflowSelected',
							{ name: this.getValue() }
						);
					}
				}
			});

			Ext.apply(this, {
				items: [this.typeField, this.idField, this.descriptionField, this.workflowCombo, this.activeField, this.attributesTable]
			});

			this.callParent(arguments);
		}
	});

})();