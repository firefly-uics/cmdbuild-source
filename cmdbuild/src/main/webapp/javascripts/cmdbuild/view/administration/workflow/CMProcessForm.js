(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("XPDLVersionModel", {
		extend: "Ext.data.Model",
		fields: [
			{name: "index", type: "int"},
			{name: "id", type: "string"}
		]
	});

	Ext.define("CMDBuild.view.administration.workflow.CMProcessForm", {
		extend : "CMDBuild.view.administration.classes.CMClassForm",

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		initComponent : function() {
			this.whithSaveAndCancelButtons = false;

			this.callParent(arguments);
			this.typeCombo.hide();
		},

		// override
		disableFields: function() {
			this.callParent(arguments);
			/*
			 * Live the fields of the XpdlForm
			 * always enabled 
			 */
			this.versionCombo.enable();
			this.fileField.enable();
		},

		// override
		buildButtons: function() {
			this.callParent(arguments);

			this.downloadXPDLSubitButton = new Ext.Button({
				text: CMDBuild.Translation.administration.modWorkflow.xpdlDownload.download_tamplete,
				margins: '0 0 0 5'
			});

			this.uploadXPDLSubitButton = new Ext.Button({
				text: CMDBuild.Translation.administration.modWorkflow.xpdlUpload.upload_template,
				margins: '0 0 0 5'
			});
		},

		// override
		buildFormFields: function() {
			this.callParent(arguments);

			this.userStoppable = new Ext.ux.form.XCheckbox({
				name: 'userstoppable',
				fieldLabel: CMDBuild.Translation.administration.modWorkflow.xpdlDownload.user_stoppable,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			// for the XPDL form
			this.versionCombo = new Ext.form.ComboBox( {
				fieldLabel: CMDBuild.Translation.administration.modWorkflow.xpdlDownload.package_version,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : 'version',
				xtype : 'combo',
				queryMode : 'local',
				displayField : 'id',
				valueField : 'id',
				store : Ext.create("Ext.data.Store", {
					model: "XPDLVersionModel",
					data: []
				}),
				editable : false,
				forceSelection : true,
				disableKeyFilter : true
			});

			this.fileField = new Ext.form.field.File({
				allowBlank: true,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: 'xpdl',
				fieldLabel: CMDBuild.Translation.administration.modWorkflow.xpdlUpload.xpdl_file
			});
		},

		// protected
		buildItems: function() {
			this.callParent(arguments);

			this.xpdlForm = new Ext.form.FormPanel( {
				frame: false,
				border: false,
				defaultType: 'textfield',
				bodyCls: 'cmgraypanel',
				autoScroll: true,
				fileUpload: true,
				items: [ //
					{
						xtype: "fieldset",
						title: CMDBuild.Translation.administration.modWorkflow.xpdlUpload.upload_xpdl_tamplete,
						items: [this.fileField, this.uploadXPDLSubitButton],
						layout: {
							type: 'hbox',
							padding:'0 0 10 0',
							align:'top'
						}
					}, {
						xtype: "fieldset",
						title: CMDBuild.Translation.administration.modWorkflow.xpdlDownload.download_xpdl_tamplete,
						items: [this.versionCombo, this.downloadXPDLSubitButton],
						layout: {
							type: 'hbox',
							padding:'0 0 10 0',
							align:'top'
						}
					}
				]
			});

			this.items = [{
				xtype: "panel",
				region: "center",
				form: false,
				border: false,
				bodyCls: 'cmgraypanel-nopadding',
				items: [ //
					this.form, //
					this.xpdlForm //
				]
			}];
		},

		// override
		getFormItems: function() {
			var items = this.callParent(arguments);
			items.push(this.userStoppable);

			this.saveButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.save
			});

			this.abortButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.abort
			});

			this.cmButtons = [this.saveButton, this.abortButton];
			items.push({
				xtype: "panel",
				buttonAlign: 'center',
				padding: "0 0 5 0",
				buttons: [this.saveButton, this.abortButton]
			});

			var fieldSetConfig = {
				xtype: "fieldset",
				title: CMDBuild.Translation.administration.modClass.attributeProperties.baseProperties,
				items: items
			}

			return fieldSetConfig;
		},

		// override
		setDefaults: function() {
			this.userStoppable.setValue(false);
			this.isActive.setValue(true);
			this.inheriteCombo.setValue(_CMCache.getActivityRootId())
		},

		// override
		buildInheriteComboStore: function() {
			return _CMCache.getSuperProcessAsStore();
		}
	});
})();