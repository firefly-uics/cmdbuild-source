(function() {

	GET_PROCESS_INSTANCE_URL = "services/json/workflow/getprocessinstancelist",
	Ext.define("CMDBuild.view.administration.widget.form.CMWorkflowDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".Workflow"
		},

		initComponent: function() {
			this.callParent(arguments);

			this.addEvents(
				/* fired when is set the workflow in the combo-box*/
				"cm-selected-workflow"
			);

			var me = this;
			this.mon(this.workflowId, "select", function(field, records) {
				me.fireEvent("cm-selected-workflow", records);
			}, this.workflowId);

		},

		// override
		buildForm: function() {
			var tr = CMDBuild.Translation.administration.modClass.widgets;
			var me = this;
			var workflowsStore = buildWorkflowsStore();
			this.callParent(arguments);

			this.workflowId = new Ext.form.field.ComboBox({
				name: "code",
				fieldLabel: "@@ Workflow",//tr[me.self.WIDGET_NAME].fields.workflow,
				labelWidth: CMDBuild.LABEL_WIDTH,
				valueField: 'id',
				displayField: 'description',
				store: workflowsStore
			});


			this.presetGrid = new CMDBuild.view.administration.common.CMKeyValueGrid({
				title: "@@ Workflow attributes",
				keyLabel: "@@ Attribute",//tr[me.self.WIDGET_NAME].presetGrid.attribute,
				valueLabel: "@@ Name", //tr[me.self.WIDGET_NAME].presetGrid.value,
				margin: "0 0 0 3"
			});

			// defaultFields is inherited
			this.defaultFields.add(this.workflowId, this.forceFormat);

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.defaultFields, this.presetGrid]
			});
		},

		fillPresetWithData: function(data) {
			this.presetGrid.fillWithData(data);
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			this.workflowId.setValue(parseInt(model.get("workflowId")));

			this.fillPresetWithData(model.get("preset"));
		},

		// override
		disableNonFieldElements: function() {
			this.presetGrid.disable();
		},

		// override
		enableNonFieldElements: function() {
			this.presetGrid.enable();
		},

		// override
		getWidgetDefinition: function() {
			var me = this;

			return Ext.apply(me.callParent(arguments), {
				workflowId: me.workflowId.getValue(),
				preset: me.presetGrid.getData()
			});
		}
	});
	function buildWorkflowsStore() {
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
		var workflows = Ext.create('Ext.data.Store', {
		    fields: ['id', 'description'],
		    data : data,
		    autoLoad: true
		});
		return workflows;
	}
})();