(function() {

Ext.ns("CMDBuild.ServiceProxy");

var GET = "GET", POST = "POST";

/**
 * Constants with the standard
 * parameter names
 */
CMDBuild.ServiceProxy.parameter = {
	// Common
	ACTIVE: "active",
	ATTRIBUTES: "attributes",
	CARDS: "cards",
	CARD_ID: "cardId",
	CONFIRMED: "confirmed",
	CLASS_NAME: "className",
	DATASOURCE: "dataSourceName",
	DESCRIPTION: "description",
	FILTER: "filter",
	FORMAT: "format",
	GROUP_NAME: "groupName",
	INDEX: "index",
	LOOKUP: "lookup",
	NAME: "name",
	MENU: "menu",
	RETRY_WITHOUT_FILTER: "retryWithoutFilter",
	SORT: "sort",
	TABLE_TYPE: "tableType",
	WIDGET: "widget",
	WIDGET_ID: "widgetId",

	// Attributes
	DISPLAY_IN_GRID: "isbasedsp",
	GROUP: "group",
	EDITOR_TYPE: "editorType",
	FIELD_MODE: "fieldmode",
	FK_DESTINATION: "fkDestination",
	LENGTH: "len",
	NOT_NULL: "isnotnull",
	PRECISION: "precision",
	SCALE: "scale",
	TYPE: "type",
	UNIQUE: "isunique",

	// DataView
	SOURCE_CLASS_NAME: "sourceClassName",
	SOURCE_FUNCTION: "sourceFunction",

	// Domain
	DOMAIN_ID: "domainId",
	DOMAIN_NAME: "domainName",
	DOMAIN_LIMIT: "domainlimit",
	DOMAIN_SOURCE: "src",

	// Relation
	RELATION_ID: "relationId",
	RELATION_MASTER_SIDE: "master"
};

/**
 * Constants with the mapped urls
 * Any module add his own urls
 */
CMDBuild.ServiceProxy.url = {
	login: "services/json/login/login",
	cardList: "services/json/management/modcard/getcardlist",
	basicCardList: "services/json/management/modcard/getcardlistshort",
	fkTargetClass: 'services/json/schema/modclass/getfktargetingclass',

	attribute: {
		create: "",
		read: "services/json/schema/modclass/getattributelist",
		update: "services/json/schema/modclass/saveattribute",
		remove: "services/json/schema/modclass/deleteattribute",

		reorder: "services/json/schema/modclass/reorderattribute",
		updateSortConfiguration: "services/json/schema/modclass/saveordercriteria"
	},

	card: {
		create: "",
		read: "services/json/management/modcard/getcard",
		update: "",
		remove: 'services/json/management/modcard/deletecard',

		bulkUpdate: "services/json/management/modcard/bulkupdate",
		bulkUpdateFromFilter: "services/json/management/modcard/bulkupdatefromfilter",
		getPosition: "services/json/management/modcard/getcardposition"
	},

	classes: {
		create: "services/json/schema/modclass/savetable",
		read: "services/json/schema/modclass/getallclasses",
		update: "services/json/schema/modclass/savetable",
		remove: "services/json/schema/modclass/deletetable"
	},

	domain: {
		create: "services/json/schema/modclass/savedomain",
		read: "services/json/schema/modclass/getalldomains",
		update: "services/json/schema/modclass/savedomain",
		remove: "services/json/schema/modclass/deletedomain"
	},

	dataView: {
		sql: {
			create: "services/json/viewmanagement/createsqlview",
			read: "services/json/viewmanagement/readsqlview",
			update: "services/json/viewmanagement/updatesqlview",
			remove: "services/json/viewmanagement/deletesqlview"
		},
		filter: {
			create: "services/json/viewmanagement/createfilterview",
			read: "services/json/viewmanagement/readfilterview",
			update: "services/json/viewmanagement/updatefilterview",
			remove: "services/json/viewmanagement/deletefilterview"
		}
	},

	filter: {
		read: "services/json/filter/read",
		create: "services/json/filter/create",
		update: "services/json/filter/update",
		remove: "services/json/filter/delete",
		
		position: "services/json/filter/position",
		userStore: "services/json/filter/readforuser"
	},

	menu: {
		create: "",
		read: "services/json/schema/modmenu/getassignedmenu",
		update: "services/json/schema/modmenu/savemenu",
		remove: "services/json/schema/modmenu/deletemenu",

		readConfiguration: "services/json/schema/modmenu/getmenuconfiguration",
		readAvailableItems: "services/json/schema/modmenu/getavailablemenuitems"
	}
};

/**
 * Core, wrap the form submission
 * and the Ajax requests
 */
CMDBuild.ServiceProxy.core = {
	submitForm: function(p) {
		if (p.form) {
			p.form.submit({
				url: p.url,
				method: p.mothod,
				scope: p.scope || this,
				success: p.success || Ext.emptyFn,
				failure: p.failure || Ext.emptyFn,
				callback: p.callback || Ext.emptyFn
			});	
		} else {
			throw CMDBuild.core.error.serviceProxy.NO_FORM;
		}
	},

	doRequest: function(p) {
		var successWithAdapter = Ext.Function.createInterceptor(p.success || Ext.emptyFn, function(response) {
			if (p.adapter) {
				var json =  Ext.JSON.decode(response.responseText);
				var adaptedJson = p.adapter(json);
				_debug("Adapted JSON result", json, adaptedJson);
				response.responseText = Ext.JSON.encode(adaptedJson);
			} 
		});

		CMDBuild.Ajax.request( {
			url: p.url,
			method: p.method,
			params: p.params || {},
			scope: p.scope || this,
			success: successWithAdapter,
			failure: p.failure || Ext.emptyFn,
			callback: p.callback || Ext.emptyFn,
			important: p.important
		});
	}
};

/* ===========================================
 * Orphans
 =========================================== */

CMDBuild.ServiceProxy.doLogin = function(p) {
	CMDBuild.Ajax.request( {
		important: true,
		url: CMDBuild.ServiceProxy.url.login,
		method: POST,
		params: p.params,
		success: p.success || Ext.emptyFn,
		failure: p.failure || Ext.emptyFn,
		callback: p.callback || Ext.emptyFn,
		scope: p.scope || this
	});
};

// TODO duplicate in card section, remove this
CMDBuild.ServiceProxy.getCardList = function(p) {
	CMDBuild.Ajax.request( {
		url: CMDBuild.ServiceProxy.url.cardList,
		method: GET,
		params: p.params,
		success: p.success,
		failure: p.failure,
		callback: p.callback
	});
};

CMDBuild.ServiceProxy.getCardBasicInfoList = function(className, success, cb, scope) {
	CMDBuild.ServiceProxy.core.doRequest({
		method: GET,
		url: CMDBuild.ServiceProxy.url.basicCardList,
		params: {
			ClassName: className,
			NoFilter: true
		},
		success: success,
		callback: cb,
		scope: scope
	});
};

/**
 * @param {object} p
 * @param {object} p.params
 * @param {string} p.params.className
 */
CMDBuild.ServiceProxy.getFKTargetingClass = function(p) {
	p.url = CMDBuild.ServiceProxy.url.fkTargetClass;
	p.method = GET;
	CMDBuild.Ajax.request(p);
};

/* ===========================================
 * Attributes
 =========================================== */

CMDBuild.ServiceProxy.attributes = {
	
	/**
	 * 
	 * @param {object} p
	 * @param {string} p.params.className
	 */
	update: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.attribute.update;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {boolean} p.params.active
	 * @param {string} p.params.className
	 */
	read: function(p) {
		p.method = GET;
		p.url = CMDBuild.ServiceProxy.url.attribute.read;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.name
	 * @param {string} p.params.className
	 */
	remove: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.attribute.remove;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.className
	 * @param {array[]} p.params.attributes [{name: "", index: ""}]
	 */
	reorder: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.attribute.reorder;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.className
	 * @param {object} p.params.attributes {attributename: position, ...}
	 */
	updateSortConfiguration: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.attribute.updateSortConfiguration;
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

/* ===========================================
 * Classes
 =========================================== */

CMDBuild.ServiceProxy.classes = {
	read: function(p) {
		p.method = GET;
		p.url = CMDBuild.ServiceProxy.url.classes.read;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	save: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.classes.update;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {object} p.params.className
	 */
	remove: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.classes.remove;
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

/* ===========================================
 * Card
 =========================================== */
CMDBuild.ServiceProxy.card = {
	/**
	 * retrieve the position on the db of the
	 * requiered card, considering the sorting and
	 * current filter applied on the grid
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {int} p.params.cardId the id of the card
	 * @param {string} p.params.className the name of the class
	 * @param {object} p.params.filter the current filter
	 * @param {object} p.params.sort the current sorting
	 */
	getPosition: function(p) {
		p.method = GET;
		p.url = CMDBuild.ServiceProxy.url.card.getPosition;

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	get: function(p) {
		adaptGetCardCallParams(p);
		p.method = GET;
		p.url = CMDBuild.ServiceProxy.url.card.read,

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	remove: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.card.remove,

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * 
	 * @param p
	 */
	bulkUpdate: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.card.bulkUpdate;

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	bulkUpdateFromFilter: function(p) {
		p.method = POST;
		p.url = CMDBuild.ServiceProxy.url.card.bulkUpdateFromFilter;

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

function adaptGetCardCallParams(p) {
	if (p.params.Id && p.params.IdClass) {
		_deprecated();
		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var parameters = {};
		parameters[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.params.IdClass);
		parameters[parameterNames.CARD_ID] = p.params.Id;

		p.params = parameters;
	}
}

/* ===========================================
 * Workflow
 =========================================== */

function adaptVariables(inputVars) {
	var outputVars = {};
	for (i = 0, len = inputVars.length; i < len; ++i) {
		var v = inputVars[i];
		outputVars[v.name] = "";
		outputVars[v.name+"_index"] = i;
		outputVars[v.name+"_type"] = {
			READ_ONLY: "VIEW",
			READ_WRITE: "UPDATE",
			READ_WRITE_REQUIRED: "UPDATEREQUIRED"
		}[v.type];
	}
	return outputVars;
}

CMDBuild.ServiceProxy.workflow = {
	getstartactivitytemplate: function(classId, p) {
		CMDBuild.ServiceProxy.core.doRequest(Ext.apply({
			url: 'services/json/workflow/getstartactivity',
			method: GET,
			params: {
				classId : classId
			}
		}, p));
	},

	getActivityInstance: function(params, conf) {
		conf.url = 'services/json/workflow/getactivityinstance';
		conf.method = GET;
		conf.params = params;
		conf.important = true;

		if (typeof conf.callback == "undefined") {
			conf.callback = function() {
				CMDBuild.LoadMask.get().hide();
			};
		}

		CMDBuild.ServiceProxy.core.doRequest(conf);
	},

	terminateActivity: function(p) {
		p.url = 'services/json/workflow/abortprocess';
		p.method = POST;

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	saveActivity: function(p) {
		p.url = 'services/json/workflow/saveactivity';
		p.method = POST;

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

function adaptWidgets(inputWidgets) {
	var outputWidgets = [];
	Ext.Array.forEach(inputWidgets, function(w) {
		outputWidgets.push(adaptWidget(w));
	});
	return outputWidgets;
}

function adaptWidget(inputWidget) {
	return Ext.apply({
		identifier : inputWidget.id,
		ButtonLabel : inputWidget.label,
		btnLabel : inputWidget.label
	}, {
		".OpenNote" : function() {
			return {
				extattrtype : "openNote"
			};
		},
		".OpenAttachment" : function() {
			return {
				extattrtype : "openAttachment"
			};
		}
	}[inputWidget.type]());
}

/* ===========================================
 * Lookups
 =========================================== */

var LOOKUP_FIELDS = {
	Id: 'Id',
	Code: 'Code',
	Description: 'Description',
	ParentId: 'ParentId',
	Index: 'Number',
	Type: 'Type',
	ParentDescription: 'ParentDescription',
	Active: 'Active',
	Notes: 'Notes'
};

CMDBuild.ServiceProxy.LOOKUP_FIELDS = LOOKUP_FIELDS;

CMDBuild.ServiceProxy.lookup = {
	readAllTypes: function(p) {
		p.method = GET;
		p.url = "services/json/schema/modlookup/tree";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	getLookupFieldStore: function(type) {
		var s = Ext.create("Ext.data.Store", {
			model: "CMLookupFieldStoreModel",
			proxy: {
				type: 'ajax',
				url : 'services/json/schema/modlookup/getlookuplist',
				reader: {
					type: 'json',
					root: 'rows'
				},
				extraParams : {
					type : type,
					active : true,
					"short" : true
				},
				actionMethods: POST // Lookup types can have UTF-8 names not handled correctly
			},
			sorters : [ {
				property : LOOKUP_FIELDS.Index,
				direction : "ASC"
			}],
			autoLoad : true,

			// Disable paging
			defaultPageSize: 0,
			pageSize: 0 
		});

		return s;
	},

	getLookupGridStore: function() {
		return new Ext.data.Store({
			model : "CMLookupForGrid",
			pageSize: parseInt(CMDBuild.Config.cmdbuild.referencecombolimit) || 20,
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modlookup/getlookuplist',
				reader : {
					type : 'json',
					root : 'rows'
				},
				actionMethods: POST // Lookup types can have UTF-8 names not handled correctly
			},
			sorters : [ {
				property : 'Number',
				direction : "ASC"
			}]
		});
	},
	
	setLookupDisabled: function(p, disable) {
		var url = 'services/json/schema/modlookup/enablelookup';
		if (disable) {
			url = 'services/json/schema/modlookup/disablelookup';
		}
		
		p.method = POST;
		p.url = url;
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	saveLookup: function(p) {
		p.method = POST;
		p.url = "services/json/schema/modlookup/savelookup";
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	saveLookupType: function(p) {
		p.method = POST;
		p.url = "services/json/schema/modlookup/savelookuptype";
		
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

/* ===========================================
 * Group
 =========================================== */

CMDBuild.ServiceProxy.group = {
	read: function(p) {
		p.method = GET;
		p.url = "services/json/schema/modsecurity/getgrouplist";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	save: function(p) {
		p.method = POST;
		p.url = "services/json/schema/modsecurity/savegroup";
		CMDBuild.ServiceProxy.core.doRequest(p);
	},
	
	getPrivilegesGridStore: function(pageSize) {
		return new Ext.data.Store({
			model : "CMDBuild.cache.CMPrivilegeModel",
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modsecurity/getprivilegelist',
				reader : {
					type : 'json',
					root : 'rows'
				}
			},
			sorters : [ {
				property : 'classname',
				direction : "ASC"
			}]
		});
	},
	
	getUserPerGroupStoreForGrid: function() {
		return new Ext.data.Store({
			model : "CMDBuild.cache.CMUserForGridModel",
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modsecurity/getgroupuserlist',
				reader : {
					type : 'json',
					root : 'users'
				}
			},
			sorters : [ {
				property : 'username',
				direction : "ASC"
			}]
		});
	},
	
	getUserStoreForGrid: function() {
		return new Ext.data.Store({
			model : "CMDBuild.cache.CMUserForGridModel",
			autoLoad : true,
			proxy : {
				type : 'ajax',
				url : "services/json/schema/modsecurity/getuserlist",
				reader : {
					type : 'json',
					root : 'rows'
				}
			},
			sorters : [ {
				property : 'username',
				direction : "ASC"
			}]
		});
	},

	getUIConfiguration: function(cbs) {
		cbs = cbs || {};

		CMDBuild.ServiceProxy.core.doRequest({
			url: "services/json/schema/modsecurity/getuiconfiguration",
			method: GET,
			success: cbs.success || Ext.emptyFn,
			failure: cbs.failure || Ext.emptyFn,
			callback: cbs.callback || Ext.emptyFn
		});
	},

	getGroupUIConfiguration: function(groupId, cbs) {
		cbs = cbs || {};

		CMDBuild.ServiceProxy.core.doRequest({
			url: "services/json/schema/modsecurity/getgroupuiconfiguration",
			params: {id: groupId},
			method: GET,
			success: cbs.success || Ext.emptyFn,
			failure: cbs.failure || Ext.emptyFn,
			callback: cbs.callback || Ext.emptyFn
		});
	},

	saveUIConfiguration: function(groupId, uiConfiguration, cbs) {
		cbs = cbs || {};

		CMDBuild.ServiceProxy.core.doRequest({
			url: "services/json/schema/modsecurity/savegroupuiconfiguration",
			params: {
				id: groupId,
				uiConfiguration: uiConfiguration
			},
			method: POST,
			success: cbs.success || Ext.emptyFn,
			failure: cbs.failure || Ext.emptyFn,
			callback: cbs.callback || Ext.emptyFn
		});
	}
};

/* ===========================================
 * Report
 =========================================== */

CMDBuild.ServiceProxy.report = {
	getMenuTree: function(p) {
		p.method = GET;
		p.url = "services/json/schema/modreport/menutree",

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	getTypesTree: function(p) {
		p.method = GET;
		p.url = "services/json/management/modreport/getreporttypestree";

		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

/* ===========================================
 * Menu
 =========================================== */

CMDBuild.ServiceProxy.menu = {
	/**
	 * Read the menu designed for this
	 * group. If there are no menu, a default
	 * menu is returned.
	 * If the configuration of the menu
	 * contains some node but the group
	 * has not the privileges to use it
	 * this method does not add it to the
	 * menu
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.groupName
	 * 
	 */
	read: function(p) {
		p.method = GET;
		p.url = CMDBuild.ServiceProxy.url.menu.read;

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * Read the full configuration designed for
	 * the given group.
	 *  
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.groupName
	 */
	readConfiguration: function(p) {
		p.method = GET;
		p.url = _CMProxy.url.menu.readConfiguration;

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * Read the items that are not added to the
	 * current menu configuration
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.groupName
	 */
	readAvailableItems: function(p) {
		p.method = GET;
		p.url = _CMProxy.url.menu.readAvailableItems;

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.groupName
	 * @param {object} p.params.menu
	 */
	save: function(p) {
		p.method = POST,
		p.url = CMDBuild.ServiceProxy.url.menu.update;

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	/**
	 * 
	 * @param {object} p
	 * @param {object} p.params
	 * @param {string} p.params.groupName
	 */
	remove: function(p) {
		p.method = POST,
		p.url = CMDBuild.ServiceProxy.url.menu.remove;

		CMDBuild.ServiceProxy.core.doRequest(p);
	}	
};

// alias
_CMProxy = CMDBuild.ServiceProxy;
})();