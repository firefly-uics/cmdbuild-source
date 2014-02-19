(function() {
	Ext.define("CMTasksModelForGrid", {
		extend: 'Ext.data.Model',
		fields: [
	         {	name:'id'	}, 
	         {	name:'type'	}, 
	         {	name:'active', },
		     {	name:'status', 
	        	convert: function(newValue, model) {
	        	 	return (model.get('active')) ? "@@ Active" : "@@ Stopped";
	         	}
	         }, 
	         {	name:'last'	}, 
		     {	name:'lastExecution', 
		        	convert: function(newValue, model) {
		        	 	return Date.parse(model.get('last'));
		         	}
		         }, 
	         {	name:'next'	}
		]	
	});
})();