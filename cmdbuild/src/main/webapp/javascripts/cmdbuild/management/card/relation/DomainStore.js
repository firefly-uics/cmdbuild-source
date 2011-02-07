/*
 * @cfg {int} classId The Class ID of which we downloaded the domain list
 */

CMDBuild.Management.DomainReader = function(config) {
	CMDBuild.Management.DomainReader.superclass.constructor.call(this, config);
};

Ext.extend(CMDBuild.Management.DomainReader, Ext.data.JsonReader, {
	//keepReadOnly: undefined, // if true keep read only domains

	readRecords : function(o){
		var records = [];
		var root = o.rows;
		var localClasses = this.getLocalClasses(o.superclasses);
	    for(var i = 0; i < root.length; i++){
			var n = root[i];
			if (!this.meta.keepReadOnly && !n.priv_create)
				continue;
			if (n.class1id in localClasses) {
				records[records.length] = new Ext.data.Record({
					DomainId: n.idDomain,
					DirectedDomain: n.idDomain.toString() + "_D",
					DomainDescription: n.description,
					DireDescription: n.descrdir,
					FullDescription: n.descrdir + " (" + n.class2 + ")",
					Direct: true,
					DestClassId: n.class2id,
					DestClassName: n.class2
				});
			}
			if (n.class2id in localClasses) {
				records[records.length] = new Ext.data.Record({
					DomainId: n.idDomain,
					DirectedDomain: n.idDomain.toString() + "_I",
					DomainDescription: n.description,
					DireDescription:  n.descrinv,
					FullDescription: n.descrinv + " (" + n.class1 + ")",
					Direct: false,
					DestClassId: n.class1id,
					DestClassName: n.class1
				});
			}
		}
		return {
			records : records
		};
	},

	
	getLocalClasses: function(superclassesArray) {
		var localClasses = {};
		for (var i=0; i<superclassesArray.length; ++i)
			localClasses[superclassesArray[i]]=true;
		return localClasses;
	}
});

/*
 * @cfg {int} classId The Class ID of which we want the domain list
 */

CMDBuild.Management.DomainStore = function(config) {
	this.reader = new CMDBuild.Management.DomainReader({
		keepReadOnly: config.keepReadOnly,
		classId: config.classId
	});

	CMDBuild.Management.DomainStore.superclass.constructor.call(this, config);

	this.baseParams['idClass'] = config.classId;
	this.baseParams['WithSuperclasses'] = true;
};

Ext.extend(CMDBuild.Management.DomainStore, Ext.data.Store, {
	url: 'services/json/schema/modclass/getdomainlist',
	baseParams: {
		WithSuperclasses: true
	},
    fields : [
    	{ name:'DomainId' },
    	{ name:'DomainDescription' },
    	{ name:'Direct' },
    	{ name:'DestClassId' }
    ]
});
