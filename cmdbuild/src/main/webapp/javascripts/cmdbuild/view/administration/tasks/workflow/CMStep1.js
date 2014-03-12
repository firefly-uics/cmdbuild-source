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

			// Cron expression builder
			this.base = new Ext.form.ComboBox( {
				name: 'base',
				hiddenName: 'base',
				labelSeparator : '',
				store : new Ext.data.SimpleStore( {
					fields : [ 'value', 'description' ],
					data : [ [ '0 * * * ?', translation.everyHour ],
							[ '0 0 * * ?', translation.everyDay ],
							[ '0 0 1 * ?', translation.everyMounth ],
							[ '0 0 1 1 ?', translation.everyYear ] ]
				}),
				valueField : 'value',
				displayField : 'description',
				typeAhead : true,
				queryMode : 'local',
				selectOnFocus : true,
				margins: '0 0 0 105'
			});

			this.base.on('select', function(combo, record, index) {
				this.setValueOfAdvanced(record[0].data.value);
			}, this);

			this.buildAdvancedFields();

			this.advanceRadio = new Ext.form.Radio({
				name : 'input_type',
				inputValue : 'advance',
				boxLabel : translation.advanced,
				width : 150,
				labelSeparator : ''
			});

			this.advanceRadio.on('change', function(radio, value) {
				if (this.editing) {
					this.setDisabledAdvancedFields(!value);
				}
			},this);

			this.baseRadio = new Ext.form.Radio({
				name : 'input_type',
				inputValue : 'advance',
				boxLabel : translation.basic,
				labelSeparator : '',
				width : 150,
				checked : true
			});

			this.baseRadio.on('change', function(radio, value) {
				if (this.editing) {
					this.base.setDisabled(!value);
				}
			},this);

			this.basePanel = new Ext.panel.Panel({
				frame: true,
				layout: 'hbox',
				margin: '0 0 5 0',
				items : [this.baseRadio, this.base]
			});

			this.advance = new Ext.panel.Panel({
				frame: true,
				layout: 'hbox',
				margin: '0 0 5 0',
				items : [this.advanceRadio,
					{
						xtype: 'panel',
						bodyCls: 'cmgraypanel',
						frame: false,
						border: false,
						items: this.advancedFields
					}
				]
			});
			// END: Cron expression builder

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
					name: 'description',
					fieldLabel: '@@ Description',
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				},
				{
					xtype: 'combo',
					name: 'className',
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
					name: 'isActive',
					fieldLabel: '@@ Run on save',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				},

				this.basePanel, this.advance,

				this.attributesTable
			];

			this.callParent(arguments);
		},

		// private
		buildAdvancedFields: function() {
			this.advancedFields = [
				this.minutes = new CMDBuild.CronTriggerField({
					fieldLabel: translation.minute
				}),
				this.hour = new CMDBuild.CronTriggerField({
					fieldLabel: translation.hour
				}),
				this.dayOfMounth = new CMDBuild.CronTriggerField({
					fieldLabel: translation.dayOfMounth
				}),
				this.mount = new CMDBuild.CronTriggerField({
					fieldLabel: translation.mounth
				}),
				this.dayOfWeek = new CMDBuild.CronTriggerField({
					fieldLabel: translation.dayOfWeek
				})
			];
			this.addListenerToAdvancedFields();
		},

		setValueOfAdvanced: function(cronExpression) {
			var values = cronExpression.split(' ');
			var fields = this.advancedFields;
			for (var i=0, len=fields.length; i<len; i++) {
				var field = fields[i];
				if (values[i]) {
					field.setValue(values[i]);
				}
			}
		},

		addListenerToAdvancedFields: function() {
			var fields = this.advancedFields;
			for (var i=0, len=fields.length; i<len; i++) {
				var field = fields[i];
				field.on('change', function(field, newValue, oldValue){
					this.setValueOfBaseIfPossible(this.getCronExpression());
				}, this);
			}
		},

		getCronExpression: function() {
			var expression = "";
			var fields = this.advancedFields;
			for (var i=0, len=fields.length-1; i<len; i++) {
				var field = fields[i];
				expression += field.getValue()+" ";
			}
			expression += fields[fields.length -1].getValue();
			return expression;
		},

		// private
		setValueOfBaseIfPossible : function(value) {
			var index = this.base.store.find('value', value);
			if (index > -1) {
				this.base.setValue(value);
			} else {
				this.base.setValue('');
			}
			this.baseExpression = index > -1;
		},

//		fillPresetWithData: function(data) {
//			this.attributesTable.fillWithData(data);
//		}
	});

})();
