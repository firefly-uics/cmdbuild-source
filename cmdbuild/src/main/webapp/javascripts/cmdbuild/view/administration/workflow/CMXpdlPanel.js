(function() {

	Ext.define("CMDBuild.view.administration.workflow.CMXpdlUploadForm", {
		extend: "Ext.form.Panel",
		translation: CMDBuild.Translation.administration.modWorkflow.xpdlUpload,
		
		initComponent: function() {

			this.submitButton = new Ext.Button({
				text: this.translation.upload_template
			});

			this.stoppable = new Ext.ux.form.XCheckbox({
				name: 'userstoppable',
				fieldLabel: CMDBuild.Translation.administration.modWorkflow.xpdlDownload.user_stoppable,
				labelWidth: CMDBuild.CM_LABEL_WIDTH
			});

			Ext.apply(this, {
				fileUpload: true,
				frame: true,
				title : this.translation.upload_xpdl_tamplete,
				defaults: {
					labelWidth: CMDBuild.CM_LABEL_WIDTH
				},
				items: [
					this.stoppable
				,{
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
		},
		onProcessSelected: function(xpdl) {
			this.stoppable.setValue(xpdl.userstoppable);
		}
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
				labelWidth: CMDBuild.CM_LABEL_WIDTH,
				name : 'version',
				xtype : 'combo',
				queryMode : 'local',
				displayField : 'id',
				valueField : 'id',
				store : Ext.create("Ext.data.Store", {
					fields: ['id'],
					data: [],
					sorters : [ {
						property : 'id',
						direction : "DESC"
					}]
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
		
		onProcessSelected: function(xpdl) {
			this.versionCombo.store.removeAll();

			for(var i=0; i<xpdl.versions.length; i++) {
				this.versionCombo.store.add({id: xpdl.versions[i]});
			}

			this.versionCombo.store.add({id: 'template'});
			this.versionCombo.setValue('template');
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