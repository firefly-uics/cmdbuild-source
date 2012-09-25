Ext.define("CMDBuild.Management.AddAttachmentWindow", {
	extend: "Ext.window.Window",

	classId: undefined, // set on instantiation
	cardId: undefined, // set on instantiation
	translation: CMDBuild.Translation.management.modcard.add_attachment_window,

	initComponent: function() {

		this.confirmBtn = new CMDBuild.buttons.ConfirmButton({
			scope: this,
			handler: this.onConfirm
		});

		this.abortBtn = new CMDBuild.buttons.AbortButton({
			scope: this,
			handler: function(){ this.destroy();}
		});

		this.store = _CMCache.getLookupStore(CMDBuild.Config.dms['category.lookup']);

		this.combo = new Ext.form.ComboBox({
			fieldLabel: this.translation.category,
			name: 'Category',
			store: this.store,
			valueField: CMDBuild.ServiceProxy.LOOKUP_FIELDS.Description,
			displayField: CMDBuild.ServiceProxy.LOOKUP_FIELDS.Description,
			triggerAction: 'all',
			allowBlank: false,
			forceSelection: true,
			queryMode: 'local',
			emptyText: this.translation.select_category
		});

		this.form = new Ext.form.Panel({
			encoding: 'multipart/form-data',
			fileUpload:true,
			method: 'POST',
			frame: true,
			url : 'services/json/management/importcsv/uploadcsv',
			monitorValid: true,

			items: [{
				xtype: 'hidden',
				name: 'IdClass',
				value: this.classId || this.IdClass
			},{
				xtype: 'hidden',
				name: 'Id',
				value: this.cardId || this.Id
			}, this.combo,
			{
				xtype: 'filefield',
				width: CMDBuild.BIG_FIELD_ONLY_WIDTH,
				fieldLabel: this.translation.load_attachment,
				allowBlank: false,
				name: 'File'
			},{
				xtype: 'textarea',
				fieldLabel: this.translation.description,
				name: 'Description',
				allowBlank: false,
				width: CMDBuild.BIG_FIELD_ONLY_WIDTH,
				anchor: "100%"
			}]
		});

		Ext.apply(this, {
			title: this.translation.window_title,
			items: [this.form],
			autoScroll: true,
			autoHeight: true,
			modal: true,
			layout:'anchor',
			frame: false,
			border: false,
			buttonAlign: 'center',
			buttons: [this.confirmBtn, this.abortBtn]
		});

		if (Ext.isGecko) { // auto width does not work for upload field
			this.width = 450;
		}

		this.callParent(arguments);
	},

	onConfirm: function() {
		CMDBuild.LoadMask.get().show();
		this.form.getForm().submit({
			method: 'POST',
			url: 'services/json/attachments/uploadattachment',
			scope: this,
			success: function() {
				// Defer the call because Alfresco is not responsive
				function deferredCall() {
					CMDBuild.LoadMask.get().hide();
					this.fireEvent('saved');
					this.close();
				};
				Ext.Function.createDelayed(deferredCall, CMDBuild.Config.dms.delay, this)();
			},
			failure: function () {
				CMDBuild.LoadMask.get().hide();
				this.enable();
			}
		});
	}
});