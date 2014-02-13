(function() {

	Ext.define("CMDBuild.view.administration.tasks.workflow.CMStep1Delegate", {
		constructor: function(view) {
			this.view = view;
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onWorkflowSelected': {
					this.onWorkflowSelected(param.id);
					return;
				}

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
					url: 'services/json/workflow/getstartactivity',
					params: {
						classId: id
					},
					success: function(response) {
						var ret = Ext.JSON.decode(response.responseText),
							filteredAttributes = CMDBuild.controller.common.WorkflowStaticsController.filterAttributesInStep(attributes, ret.response.variables);

						filteredAttributes = me.cleanServerAttributes(filteredAttributes);
						me.view.fillPresetWithData(filteredAttributes);
					}
				});
			});
		}
	});

	Ext.define("CMDBuild.view.administration.tasks.workflow.CMStep1", {
		extend: "Ext.panel.Panel",

		initComponent: function() {
			var me = this;

//			this.taskType = Ext.create('Ext.form.field.Text', {
//				fieldLabel: '@@ Type',
//				name: 'type',
//				value: 'workflow',
//				xtype: 'textfield',
//				disabled: true,
//				cmImmutable: true,
//				readOnly: true,
//				width: CMDBuild.ADM_BIG_FIELD_WIDTH
//			});
//
//			this.workflowPanel = Ext.create('Ext.panel.Panel', {
//				bodyCls: 'cmgraypanel',
//				margins: '0px 3px 0px 0px',
//				autoScroll: true,
//				border: false,
//
//				layout: {
//					type: 'hbox',
//					align: 'stretch'
//				},
//
//				items: [
//					Ext.create('Ext.form.field.ComboBox', {
//						name: 'workflow',
//						fieldLabel: '@@ Workflow',
//						valueField: 'id',
//						displayField: 'description',
//						store: me.buildWorkflowsStore(),
//						listeners: {
//							'select': function() {
//								me.delegate.cmOn(
//									'onWorkflowSelected',
//									{ id: this.getValue() }
//								);
//							}
//						}
//					}),
//					Ext.create('CMDBuild.view.administration.common.CMKeyValueGrid', {
//						title: "@@ Workflow attributes",
//						keyLabel: "@@ Attribute",
//						valueLabel: "@@ Name",
//						margin: "0px 0px 0px 3px"
//					})
//				]
//			});
//
//			Ext.apply(this, {
//				layout: {
//					type: "vbox"
//				},
//				items: [this.taskType, this.workflowPanel]
//			});

			this.workflowComboPanel = Ext.create('Ext.panel.Panel', {
				bodyCls: 'cmgraypanel',
				margins: '0px 3px 0px 0px',
				autoScroll: true,
				border: false,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				items: [
					{
						fieldLabel: '@@ Type',
						name: 'type',
						value: 'workflow',
						xtype: 'textfield',
						disabled: true,
						cmImmutable: true,
						readOnly: true
					},
					Ext.create('Ext.form.field.ComboBox', {
						name: 'workflow',
						fieldLabel: '@@ Workflow',
						valueField: 'id',
						displayField: 'description',
						store: me.buildWorkflowsStore(),
						listeners: {
							'select': function() {
								me.delegate.cmOn(
									'onWorkflowSelected',
									{ id: this.getValue() }
								);
							}
						}
					})
				]
			});

			this.presetGrid = new CMDBuild.view.administration.common.CMKeyValueGrid({
				title: "@@ Workflow attributes",
				keyLabel: "@@ Attribute",
				valueLabel: "@@ Name",
				margin: "0px 0px 0px 3px"
			});

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.workflowComboPanel, this.presetGrid]
			});

			this.delegate = new CMDBuild.view.administration.tasks.workflow.CMStep1Delegate(this);

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
			this.presetGrid.fillWithData(data);
		}
	});

})();
