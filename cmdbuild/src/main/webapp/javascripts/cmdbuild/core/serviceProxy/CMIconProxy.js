(function() {
	var iconStore = null;
	CMDBuild.ServiceProxy.Icons = {
		getIconStore: function() {
			if (iconStore == null) {
				iconStore = Ext.create("Ext.data.Store", {
					model : 'IconsModel',
					proxy : {
						type : 'ajax',
						url : "services/json/gis/geticonslist",
						reader : {
							type : 'json',
							root : 'rows'
						}
					},
					autoLoad : true
				});
			}
			
			return iconStore;
		}
	}
})();