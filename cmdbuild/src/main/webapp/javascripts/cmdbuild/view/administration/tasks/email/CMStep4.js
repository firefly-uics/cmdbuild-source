(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
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
					return this.onWorkflowSelected(param);

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

//		checkWorkflowComboSelected: function() {
//			if (this.getWorkflowComboValue())
//				return true;
//
//			return false;
//		},

		getAttributeTableValues: function() {
			return this.view.attributesTable.getData();
		},

//		getWorkflowComboValue: function() {
//			return this.view.workflowCombo.getValue();
//		},

		fillAttributesGrid: function(data) {
			this.view.attributesTable.fillWithData(data);
		},
//
//		fillWorkflowCombo: function(workflowName) {
//			this.view.workflowCombo.setValue(workflowName);
//		},

		onAttributeComboSelect: function(rowIndex) {
			this.view.attributesTable.cellEditing.startEditByPosition({ row: rowIndex, column: 1 });
		},

		/**
		 * @param (String) name
		 * @param (Boolean) modify
		 */
		onWorkflowSelected: function(name, modify) {
			var me = this;

			if (typeof modify === 'undefined')
				modify = false;

			CMDBuild.core.proxy.CMProxyTasks.getWorkflowAttributes({
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

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep4Delegate', this);

			this.workflowCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.administration.tasks.workflow,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				store: CMDBuild.core.proxy.CMProxyTasks.getWorkflowsStore(),
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				labelWidth: CMDBuild.LABEL_WIDTH,
				forceSelection: true,
				editable: false,
				listeners: {
					select: function() {
						me.delegate.cmOn('onWorkflowSelected', this.getValue());
					}
				}
			});

			this.attributesTable = Ext.create('CMDBuild.view.administration.common.CMDynamicKeyValueGrid', {
				title: tr.workflowAttributes,
				keyLabel: CMDBuild.Translation.name,
				valueLabel: CMDBuild.Translation.value,
				disabled: true,
				margin: '0 0 5 0', // To fix Fieldset bottom padding problem
				keyEditorConfig: {
					xtype: 'combo',
					valueField: CMDBuild.ServiceProxy.parameter.VALUE,
					displayField: CMDBuild.ServiceProxy.parameter.VALUE,
					forceSelection: true,
					editable: false,
					allowBlank: false,
					listeners: {
						select: function(combo, records, eOpts) {
							me.delegate.cmOn(
								'onAttributeComboSelect',
								me.attributesTable.store.indexOf(me.attributesTable.getSelectionModel().getSelection()[0])
							);
						}
					}
				}
			});

			this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.startWorkflow,
				checkboxToggle: true,
				collapsed: true,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				items: [
					{
						xtype: 'container',
						layout: {
							type: 'vbox'
						},
						items: [this.workflowCombo]
					},
					this.attributesTable
				]
			});

			Ext.apply(this, {
				items: [this.workflowFieldset]
			});

			this.callParent(arguments);
		}
	});

})();