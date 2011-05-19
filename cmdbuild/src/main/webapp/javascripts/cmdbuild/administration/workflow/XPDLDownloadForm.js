CMDBuild.Administration.XPDLDownloadForm = Ext.extend(Ext.form.FormPanel, {
    translation : CMDBuild.Translation.administration.modWorkflow.xpdlDownload,

    initComponent : function() {
        this.versionComboStore = new Ext.data.SimpleStore({
            fields: ['id','description'],
            sortInfo : {field:'id', direction: 'DESC'},
            data : []
        });
        this.submitButton = new Ext.Button({
            text: this.translation.download_tamplete,
            id: 'downloadxpdltemplatebutton',
            name: 'downloadxpdltemplatebutton',
            scope: this,
            handler: function() {
            	var version = this.versionCombo.getValue();
            	if(version == 'template') {
                    this.getForm().getEl().dom.action = 'services/json/schema/modworkflow/workflowtemplate';
            	} else {
            		this.getForm().getEl().dom.action = 'services/json/schema/modworkflow/downloadxpdl';
            	}
                this.getForm().submit();
            }
        });
        
        this.initialConfig.standardSubmit = true;

        this.versionCombo = new Ext.form.ComboBox({
            fieldLabel : this.translation.package_version,
            name: 'version',
            xtype: 'combo',
            mode: 'local',
            displayField: 'description',
            valueField: 'id',
            sortInfo: {field: 'description', direction: 'DESC'},
            store: this.versionComboStore,
            editable: false,
            forceSelection: true,
            disableKeyFilter: true,
            triggerAction: 'all'
        });
        
        Ext.apply(this, {
        	//standardSubmit: true,
            title : this.translation.download_xpdl_tamplete,
            labelWidth : 200,
            defaultType : 'textfield',
            monitorValid : true,
            layout: 'fit',
            items: [{
            	xtype: 'panel',
            	border: true,
            	frame: true,
            	layout: 'form',
            	items : [{
	                name : 'idClass',
	                xtype : 'hidden',
	                value : -1
	            },this.versionCombo]
            }],
            buttonAlign: 'center',
            buttons: [this.submitButton]
        });
        CMDBuild.Administration.XPDLDownloadForm.superclass.initComponent.apply(this, arguments);
        
        this.subscribe('cmdb-xpdl-loaded',this.loadData, this);
    },
    
    loadData: function(params) {
    	
    	/*
    	 * if the form has not been rendered yet, async call loadData on afterlayout
    	 */
    	if(!this.rendered) {
    		this.on('afterlayout',function(){this.loadData(params);},this,{single: true});
    		return;
    	}
    	
    	this.getForm().reset();
    	this.idClass = -1;
    	if(params.idClass) {
    		this.idClass = params.idClass;
    	}
    	if(this.idClass < 0) {
    		this.getForm().reset();
    	} else {
    		this.getForm().setValues(params);
    	}
    	
    	var versions = [];
    	for(var i=0; i<params.versions.length; i++) {
    		var id = params.versions[i];
    		versions.push([id, id]);
    	}
    	versions.push(['template','template']);
    	this.versionComboStore.loadData(versions);
    	this.versionCombo.setValue('template');
    }
});

Ext.reg('xpdldownloadform', CMDBuild.Administration.XPDLDownloadForm);