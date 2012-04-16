(function() {

	Ext.define("CMDBuild.view.administration.workflow.CMXpdlUploadForm", {
		extend: "Ext.form.Panel",
		translation: CMDBuild.Translation.administration.modWorkflow.xpdlUpload,
		
		initComponent: function() {

			this.submitButton = new Ext.Button({
				text: this.translation.upload_template
			});

			Ext.apply(this, {
				fileUpload: true,
				frame: true,
				title : this.translation.upload_xpdl_tamplete,
				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH
				},
				items: [{
					xtype: 'textfield',
					inputType : 'file',
					allowBlank: true,
					width: 300,
					inputType: 'file',
					name: 'xpdlfile',
					fieldLabel: this.translation.xpdl_file
				},{
					xtype: 'textfield',
					inputType: 'file',
					name: 'imgfile',
					allowBlank: true,
					width: 300,
					fieldLabel: this.translation.jpg_file
				}],
				buttonAlign: 'center',
				buttons: [this.submitButton]
			});

			this.callParent(arguments);
		}
	});

	Ext.define("XPDLVersionModel", {
		extend: "Ext.data.Model",
		fields: [
			{name: "index", type: "int"},
			{name: "id", type: "string"}
		]
	});

	Ext.define("CMDBuild.view.administration.workflow.CMXpdlDownloadForm", {
		extend: "Ext.form.Panel",
		translation : CMDBuild.Translation.administration.modWorkflow.xpdlDownload,
		initComponent: function() {

			this.submitButton = new Ext.Button({
				text: this.translation.download_tamplete
			});

			this.versionCombo = new Ext.form.ComboBox( {
				fieldLabel : this.translation.package_version,
				labelWidth: CMDBuild.LABEL_WIDTH,
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

			this.initialConfig.standardSubmit = true;

			Ext.apply(this, {
				title : this.translation.download_xpdl_tamplete,
				frame: true,
				items: [this.versionCombo],
				buttonAlign: 'center',
				buttons: [this.submitButton]
			});

			this.callParent(arguments);
		},
		
		onProcessSelected: function(versions) {
			var store = this.versionCombo.store;

			store.removeAll();
			for(var i=0; i<versions.length; i++) {
				var v = versions[i];
				store.add({id: v, index: v});
			}
			store.add({id: "template", index: 0});

			store.sort([
				{
					property : "index",
					direction: 'DESC'
				}
			]);

			this.versionCombo.setValue(store.getAt(0).getId());
		}
	});

	Ext.define("CMDBuild.view.administration.workflow.CMXpdlPanel", {
		extend: "Ext.panel.Panel",
		hideMode: "offsets",
		
		initComponent: function() {
			this.uploadForm = new CMDBuild.view.administration.workflow.CMXpdlUploadForm({
				region: "north",
				split: true,
				height: "50%"
			});
			
			this.downloadForm = new CMDBuild.view.administration.workflow.CMXpdlDownloadForm({
				region: "center"
			});
			
			this.layout = "border";
			this.items = [this.uploadForm, this.downloadForm];

			this.callParent(arguments);
		}
	});

})();