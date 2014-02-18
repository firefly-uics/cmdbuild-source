(function() {

	// FAKE DATAS
	var workflowsComboValues = Ext.create('Ext.data.Store', {
		fields: ['id', 'description'],
		data: [
			{ 'id': 1, 'description': 'FromAddress' },
			{ 'id': 2, 'description': 'ToAdress' },
			{ 'id': 3, 'description': 'CCAddress' },
			{ 'id': 4, 'description': 'BCCAddress' },
			{ 'id': 5, 'description': 'Date' },
			{ 'id': 6, 'description': 'Subject' },
			{ 'id': 7, 'description': 'Body' }
		]
	});
	// END FAKE DATAS

	Ext.define("CMDBuild.view.administration.tasks.email.CMStep3Delegate", {

		delegate: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onWorkflowSelected': {
					this.onWorkflowSelected(param.id);
					return;
				}

				case 'onWorkflowChecked':
					return showComponent(this.view, 'workflowWrapper', param.checked);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
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

		onWorkflowSelected: function(id) {
			var me = this;

			_CMCache.getAttributeList(id, function(attributes) {
				Ext.Ajax.request({
					url: CMDBuild.ServiceProxy.url.workflow.getStartActivity,
					params: {
						classId: id
					},
					success: function(response) {
						var ret = Ext.JSON.decode(response.responseText),
							filteredAttributes = CMDBuild.controller.common.WorkflowStaticsController.filterAttributesInStep(attributes, ret.response.variables);

						filteredAttributes = me.cleanServerAttributes(filteredAttributes);

						me.view.workflowSetup.add( me.buildWorkflowConfigItems(filteredAttributes) );
						me.view.workflowSetup.doLayout();
					}
				});
			});
		},

		buildComboBoxSetupFieldsStore: function() {
			return workflowsComboValues;
		},

		buildWorkflowConfigItems: function(values) {
			var me = this,
				items = [];

			if (typeof values === 'undefined') {
				values = [''];
			}

			for (key in values) {
				items.push({
					fieldLabel: key,
					valueField: 'id',
					displayField: 'description',
					queryMode: 'local', // Change in "remote" when server side will be implemented
					store: me.buildComboBoxSetupFieldsStore(),
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				});
			}

			return items;
		}
	});

	Ext.define("CMDBuild.view.administration.tasks.email.CMStep3", {
		extend: "Ext.form.Panel",

		border: false,
		bodyCls: 'cmgraypanel',
		height: "100%",
		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.workflowSetup = Ext.create('Ext.container.Container', {
				layout: 'vbox',
				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					xtype: 'combo'
				},
				items: []
			});

			this.items = [
				{
					fieldLabel: '@@ Start workflow',
					name: 'workflow',
					xtype: 'checkbox',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					listeners: {
						change: function(that, newValue, oldValue, eOpts) {
							me.delegate.cmOn('onWorkflowChecked', { 'checked': newValue });
						}
					}
				},
				{
					xtype: 'container',
					itemId: 'workflowWrapper',
					layout: 'vbox',
					hidden: true,
					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH,
						xtype: 'combo'
					},
					items: [
						{
							name: 'workflow',
							fieldLabel: '@@ Workflow',
							valueField: 'id',
							displayField: 'description',
							store: me.buildWorkflowsStore(),
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
							listeners: {
								'select': function() {
									me.delegate.cmOn(
										'onWorkflowSelected',
										{ id: this.getValue() }
									);
								}
							}
						},
						me.workflowSetup
					]
				}
			];

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep3Delegate');
			this.delegate.view = this;

			this.callParent(arguments);
		},

		buildWorkflowsStore: function() {
			var processes = _CMCache.getProcesses();
			var data = [];

			for (var key in processes) {
				var obj = processes[key];

				if (obj.raw.superclass)
					continue;

				data.push({
					id: obj.raw.id,
					description: obj.raw.text
				});
			}

			return Ext.create('Ext.data.Store', {
				fields: ['id', 'description'],
				data: data,
				autoLoad: true
			});
		},

		fillPresetWithData: function(data) {
			this.workflowSetup.fillWithData(data);
		}
	});

	function showComponent(view, fieldName, showing) {
		var component = view.query("#" + fieldName)[0];

		if (showing) {
			component.show();
		} else {
			component.hide();
		}
	}

})();