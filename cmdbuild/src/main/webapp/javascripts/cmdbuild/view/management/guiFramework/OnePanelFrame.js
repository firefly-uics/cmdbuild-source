(function() {

	Ext.define('CMDBuild.view.management.guiFramework.OnePanelFrame', {
		extend: 'Ext.panel.Panel',
	    layout: 'fit',
	    items: [{
	        xtype: 'box',
	        autoEl: {
	            tag: 'iframe',
	            src: 'http://10.0.0.107:8080/cbap/cbap/demo/managemail/',
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