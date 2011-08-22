Ext.define("CMDBuild.view.management.workflow.widgets.CMCreateReport", {
	extend: "Ext.panel.Panel",

	formatCombo: {},
	attributeList: [],
	formFields: [],

	constructor: function(c) {
		this.widgetConf = c.widget;
		this.activity = c.activity.raw || c.activity.data;

		this.callParent([this.widgetConf]); // to apply the conf to the panel
	},

	//create the form and request the report
	initComponent: function() {
		this.formatCombo = new Ext.form.ComboBox({
			fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.createreport.format_label,
			labelAlign: "right",
			labelWidth: CMDBuild.LABEL_WIDTH,
			name : 'reportExtension',
			editable : false,
			disableKeyFilter : true,
			forceSelection : true,
			emptyText : ' ',// this.translation.PleaseSelect,
			triggerAction : 'all',
			mode : 'local',
			store : new Ext.data.SimpleStore( {
				id : 0,
				fields : [ 'value', 'text' ],
				data : [ 
					[ 'pdf', 'PDF' ],
					[ 'csv', 'CSV' ],
					[ 'odt', 'ODT' ],
					[ 'rtf', 'RTF' ]
				]
			}),
			valueField: 'value',
			displayField: 'text',
			hiddenName: 'reportExtension'
		});

		if (this.widgetConf.forceextension) {
			this.formatCombo.setValue(this.widgetConf.forceextension);
			this.formatCombo.disable();
		}

		this.formPanel = new Ext.FormPanel({
			monitorValid : true,
			autoScroll : true,
			frame : false,
			border: false,
			region : 'center',
			bodyCls: "x-panel-body-default-framed",
			cls: "cmborderbottom",
			padding: "5",
			items : [ this.formatCombo ]
		});

		this.saveButton = new Ext.Button( {
			text : CMDBuild.Translation.common.buttons.save,
			name : 'saveButton'
		});

		Ext.apply(this, {
			layout: 'border',
			buttonAlign: 'center',
			items: [this.formPanel],
			cls: "x-panel-body-default-framed",
			border: false,
			frame: false,
			buttons: [this.saveButton]
		});

		this.callParent(arguments);
	},

	cmActivate: function() {
		this.mon(this.ownerCt, "cmactive", function() {
			this.ownerCt.bringToFront(this);
		}, this, {single: true});

		this.ownerCt.cmActivate();
	},

	// add the required attributes
	configureForm: function() {
		if (!this.formPanelCreated) {
			this.formPanelCreated = true;
			var conf = this.widgetConf;
			// add fields to form panel
			for (var i=0; i<this.attributeList.length; i++) {
				var attribute = this.attributeList[i];
				var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);
				if (field) {
					if(conf.parameters[attribute.name] && typeof (conf.parameters[attribute.name] != 'object')) {
						field.setValue( conf.parameters[attribute.name] );
					} else if(attribute.defaultvalue) {
						field.setValue(attribute.defaultvalue);
					}
					this.formFields[i] = field;
					this.formPanel.add(field);
				}
			}
			this.formPanel.doLayout();
		}
	},

	fillFormValues: function() {
		var conf = this.widgetConf;
		for(var i=0;i<this.formFields.length;i++) {
			var field = this.formFields[i];
			if(conf.parameters[field.name] && (typeof conf.parameters[field.name] == 'object')) {
				var value = this.getActivityFormVariable(conf.parameters[field.name]);
				field.setValue(value);
			}
		}
	}
});