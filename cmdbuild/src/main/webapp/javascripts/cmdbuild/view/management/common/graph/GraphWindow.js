Ext.define('CMDBuild.Management.GuiGraphWindow', {
	extend: "CMDBuild.PopupWindow",
	
	requires: ['CMDBuild.core.configurations.CustomPages'],
	
    layout: 'fit',
    items: [
    ],
   listeners: {
		show: function() {
			var basePath = window.location.toString().split('/');
			basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');
			var src = basePath //CMDBuild.core.configurations.CustomPages.getCustomizationsPath()
				+ '/javascripts/cmdbuild-network'
				+ '/?basePath=' + basePath
				+ '&classId=' + this.classId + '&cardId=' + this.cardId 
				+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPages.getVersion();
	   		this.add({
			        xtype: 'box',
			        autoEl: {
			            tag: 'iframe',
			            src: src
			            }
		    	});
	    }
     },
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

CMDBuild.Management.showGraphWindow = function(classId, cardId) {
	var basePath = window.location.toString().split('/');
	basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');	
	var entity = _CMCache.getClassById(classId);
	new CMDBuild.Management.GuiGraphWindow({
		classId: entity.data.name,
		cardId: cardId,
		basePath: basePath
	}).show();
};