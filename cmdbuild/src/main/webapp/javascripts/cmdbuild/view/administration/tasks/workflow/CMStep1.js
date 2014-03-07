(function() {

	var translation = CMDBuild.Translation.administration.modWorkflow.scheduler;

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate', {

		delegate: undefined,
		filterWindow: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onComboSelect':
					return this.onComboSelect(param);

				case 'onWorkflowSelected':
					return this.onWorkflowSelected(param.id);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

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

		onComboSelect: function(rowIndex) {
			this.view.attributesTable.cellEditing.startEditByPosition({ row: rowIndex, column: 1});
		},

		onWorkflowSelected: function(id) {
			var me = this;

			CMDBuild.core.serviceProxy.CMProxyTasks.getWorkflowAttributes({
				params: {
					className: _CMCache.getEntryTypeNameById(id)
				},
				success: function(response) {
					var ret = Ext.JSON.decode(response.responseText);

					me.view.attributesTable.keyEditorConfig.store = me.buildWorkflowAttributesStore(me.cleanServerAttributes(ret.attributes));
					me.view.attributesTable.store.removeAll();
					me.view.attributesTable.store.insert(0, { key: '', value: '' });
					me.view.attributesTable.cellEditing.startEditByPosition({ row: 0, column: 0 });
					me.view.attributesTable.setDisabled(false);
				}
			});
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

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate');
			this.delegate.view = this;

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

			this.attributesTable = Ext.create('CMDBuild.view.administration.common.CMDynamicKeyValueGrid', {
				title: '@@ Workflow attributes',
				id: 'workflowAttributesGrid',
				keyLabel: '@@ Name',
				valueLabel: '@@ Value',
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
								'onComboSelect',
								me.attributesTable.store.indexOf(me.attributesTable.getSelectionModel().getSelection()[0])
							);
						}
					}
				}
			});

			this.items = [
				this.typeField,
				this.idField,
				{
					name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					fieldLabel: '@@ Description',
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				},
				{
					xtype: 'combo',
					id: 'workflowCombo',
					name: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
					fieldLabel: '@@ Workflow',
					valueField: CMDBuild.ServiceProxy.parameter.ID,
					displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					store: CMDBuild.core.serviceProxy.CMProxyTasks.getWorkflowsStore(),
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					forceSelection: true,
					editable: false,
					allowBlank: false,
					listeners: {
						'select': function() {
							me.delegate.cmOn(
								'onWorkflowSelected',
								{ id: this.getValue() }
							);
						}
					}
				},
				{
					xtype: 'checkbox',
					name: CMDBuild.ServiceProxy.parameter.ACTIVE,
					fieldLabel: '@@ Run on save',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				},
				this.attributesTable
			];

			this.callParent(arguments);
		},

//		fillPresetWithData: function(data) {
//			this.attributesTable.fillWithData(data);
//		}
	});

})();
