(function() {

	Ext.define("CMDomainModelForCombo", {
		extend: 'Ext.data.Model',
		fields: [{
			name: 'idDomain',
			type: "int"
		}, {
			name: 'description',
			type: "string"
		}, {
			name: 'name',
			type: "string"
		}]
	});

})();