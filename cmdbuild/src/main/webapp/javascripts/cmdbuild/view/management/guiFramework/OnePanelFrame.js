(function() {

	Ext.define('CMDBuild.view.management.guiFramework.OnePanelFrame', {
		extend: 'Ext.panel.Panel',
	    layout: 'fit',
	    items: [{
	        xtype: 'box',
	        autoEl: {
	            tag: 'iframe',
	            src: undefined,
	        },
	    }],
		setItems: function(htmlSrc) {
			this.removeAll();
			this.add({
		        xtype: 'box',
		        autoEl: {
		            tag: 'iframe',
		            src: htmlSrc
		        }
	        });
		}
	});
})();