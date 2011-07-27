Ext.define("CMDBuild.view.management.utilities.CMModExportCSV", {
	extend: "Ext.panel.Panel",
	cmName: 'exportcsv',
	layout: 'border',
	hideMode:  'offsets',
	frame: true,
	border: false,
	translation: CMDBuild.Translation.management.modutilities.csv,

	initComponent: function() {
		
		this.exportBtn = new CMDBuild.buttons.ExportButton({
			scope: this,			
			formBind: true,
			handler: function(){
				 // IE Fix (see form for more)
//				this.form.getForm().getEl().dom.action = 'services/json/management/exportcsv/export',
				this.form.getForm().submit();
			}
    	});

		this.classList = new Ext.form.field.ComboBox({
    		store: _CMCache.getClassesStore(),
    		fieldLabel : this.translation.selectaclass,
			queryMode: 'local',
			name : 'idClass',
			hiddenName : 'idClass',
			valueField : 'id',
			displayField : 'description',			
			allowBlank : false,
			editable: false
    	});

    	this.separator = new Ext.form.field.ComboBox({ 
			name: 'separator',
			fieldLabel: this.translation.separator,
			valueField: 'value',
			displayField: 'value',
			hiddenName: 'separator',
			store: new Ext.data.SimpleStore({
				fields: ['value'],
				data : [[','],[';'],['|']]
			}),
			queryMode: 'local',
			triggerAction: 'all',
			editable: false,
			allowBlank: false
		});
    	
    	this.form = new Ext.form.Panel({
    		region: 'center',
    		hideMode:  'offsets',
    		frame: true,
    		border: true,
    		monitorValid: true,
    		labelWidth: 200,
    		method: 'POST',
    		url : 'services/json/management/exportcsv/export',
    	    standardSubmit:true, // IE Fix (see exportBtn for more)
    		items:	[
		       this.classList,
		       this.separator,
		       this.exportBtn
	    	]
    	});
    	
    	this.form.on('clientvalidation', function(form, valid){
    		this.exportBtn.setDisabled(!valid)
    	}, this);
    	
    	Ext.apply(this, {
    		title: CMDBuild.Translation.management.modutilities.csv.title_export,
    		items:[this.form]    		
    	});
    	
    	this.callParent(arguments);
    }
});