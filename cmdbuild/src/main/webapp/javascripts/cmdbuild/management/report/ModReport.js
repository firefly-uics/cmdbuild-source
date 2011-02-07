/**
 * This management module handles the report list and the report attributes
 * 
 * @class CMDBuild.Management.ModReport
 * @extends Ext.Component
 */
CMDBuild.Management.ModReport = Ext.extend(CMDBuild.ModPanel, {
	actions: [],
	id: 'modreport',
	translation : CMDBuild.Translation.management.modreport,

	selectReport: function(params) {
		if (!params) {
			return;
		}
		this.reportType = params.id;
		this.publish('cmdb-init-report', this.reportType);
    },

	initComponent: function() {		
		CMDBuild.log.info("init report module");    	    	    	

		Ext.apply(this,{			
	        id: this.id + '_panel',
	        modtype: 'report',
	        basetitle: this.translation.title,	        
	        layout: 'border',
	        items: [{
		        	id: 'reportlist_grid',
		        	xtype: 'reportlistgrid',
		        	region: 'center',
		        	title: CMDBuild.Translation.management.modreport.treetitle
	        }]
		});				    	

		this.subscribe('cmdb-select-report', this.selectReport, this);
		this.subscribe('cmdb-select-reportpdf', this.bringToFront, this);
		this.subscribe('cmdb-select-reportcsv', this.bringToFront, this);

		CMDBuild.Management.ModReport.superclass.initComponent.apply(this, arguments);
	},
	
	bringToFront: function() {
		this.ownerCt.layout.setActiveItem(this.id);
	}

});

