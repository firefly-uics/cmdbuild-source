(function() {
	var withTranslations = false;
	var activeTranslations = [];
	var observers = [];
	var translationsToSave = [];
	var translationsInAdding = false;
	Ext.define("CMDBUild.cache.CMCacheTranslationsFunctions", {
		initAddingTranslations: function() {
			translationsToSave = [];
			translationsInAdding = true;
		},
		finishAdding: function() {
			translationsInAdding = false;
			this.flushTranslationsToSave();
		},
		initModifyingTranslations: function() {
			translationsInAdding = false;
			translationsToSave = [];
		},
		flushTranslationsToSave: function(translationsKeyName, translationsKeySubName) {
			for (var i = 0; i < translationsToSave.length; i++) {
				var t = translationsToSave[i];
				createDeleteUpdateTranslations(t.translationsKeyType, translationsKeyName, 
						translationsKeySubName, t.translationsKeyField, t.values, {});
				// oldValues is always {} because we are inserting a new class
				// and then we have no precedent translations
			}
		},
		pushAddingTranslations: function(addingTranslations) {
			for (var i = 0; i < translationsToSave.length; i++) {
				var t = translationsToSave[i];
				if (t.translationsKeyType == addingTranslations.translationsKeyType && 
						t.translationsKeyField == addingTranslations.translationsKeyField) {
					t.values = addingTranslations.values;
					return;
				}
			}
			translationsToSave.push(addingTranslations);
		},
		popAddingTranslations: function(translationsKeyType, translationsKeyField) {
			for (var i = 0; i < translationsToSave.length; i++) {
				var t = translationsToSave[i];
				if (t.translationsKeyType == translationsKeyType && 
						t.translationsKeyField == translationsKeyField) {
					return t;
				}
			}
			return undefined;
		},
		createTranslations: function(translationsKeyType, translationsKeyName, 
				translationsKeySubName, translationsKeyField, values, oldValues) {	
			if (translationsInAdding) {
				var t = {
					"translationsKeyType" : translationsKeyType, 
					//"translationsKeyName": translationsKeyName, during the adding i don't have this value 
					//"translationsKeySubName": translationsKeySubName, during the adding i don't have this value
					"translationsKeyField": translationsKeyField, 
					"values": values
				};
				this.pushAddingTranslations(t);
			}
			else {
				createDeleteUpdateTranslations(translationsKeyType, translationsKeyName, 
						translationsKeySubName, translationsKeyField, values, oldValues);
			}
		},
		readTranslations: function(translationsKeyType, translationsKeyName, 
				translationsKeySubName, translationsKeyField, callBack) {	
			if (translationsInAdding) {
				var translations = this.popAddingTranslations(translationsKeyType, translationsKeyField);
				if (translations) {
					callBack(null, null, {"response": translations.values});
				}
				else {
					callBack(null, null, {"response": {}});
				}
			}
			else {
				readTranslations(translationsKeyType, translationsKeyName, 
					translationsKeySubName, translationsKeyField, callBack);
			}
		},
		isMultiLanguages: function() {
			return withTranslations;
		},
		resetMultiLanguages: function() {
			setActiveTranslations();
		},
		getActiveTranslations: function() {
			return activeTranslations;
		},
		registerTranslatableText: function(text) {
			observers.push(text);
		}
	});
	function callObservers() {
		for (var i = 0; i < observers.length; i++) {
			var text = observers[i];
			text.resetLanguageButton();
		}
	}
	function setActiveTranslations() {
		activeTranslations = [];
		CMDBuild.ServiceProxy.translations.readActiveTranslations({
			scope: this,
			success: function(response){
				var activeLanguages = Ext.JSON.decode(response.responseText).data;
				CMDBuild.ServiceProxy.translations.readAvailableTranslations({
					success : function(response, options, decoded) {
						withTranslations = false;
						for (key in decoded.translations) {
							if (activeLanguages[decoded.translations[key].name] != "on") {
								continue;
							}
							var item = {
								name: decoded.translations[key].name,
								image: "ux-flag-" + decoded.translations[key].name,
								language: decoded.translations[key].value
							};
							activeTranslations.push(item);
							withTranslations = true;
						}
						callObservers();
					}
				});
			}
		});
	}
	function isEmpty(o) {
	    for ( var p in o ) { 
	        if (o.hasOwnProperty(p)) { return false; }
	    }
	    return true;
	}
	function emptyValue(value) {
		return (! value || Ext.String.trim(value) == "");
	}
	function createDeleteUpdateTranslations(translationsKeyType, translationsKeyName, 
			translationsKeySubName, translationsKeyField, values, oldValues) {
		var createValues = {};
		var updateValues = {};
		var deleteValues = {};
		for (var prop in values) {
			console.log(prop + "-----" + values[prop] + "----" + oldValues[prop]);
			if (emptyValue(values[prop]) && ! emptyValue(oldValues[prop])) {
				deleteValues[prop] = "";
			}
			else if (! emptyValue(values[prop]) && ! emptyValue(oldValues[prop])) {
				if (values[prop] != oldValues[prop]) {
					updateValues[prop] = values[prop];
				}
			}
			else if (! emptyValue(values[prop]) && emptyValue(oldValues[prop])) {
				createValues[prop] = values[prop];
			}
		}
		_debug(createValues, updateValues, deleteValues);
		if (! isEmpty(createValues)) {
			createTranslations(translationsKeyType, translationsKeyName, translationsKeySubName, translationsKeyField, createValues);
		}
		if (! isEmpty(updateValues)) {
			updateTranslations(translationsKeyType, translationsKeyName, translationsKeySubName, translationsKeyField, updateValues);
		}
		if (! isEmpty(deleteValues)) {
			deleteTranslations(translationsKeyType, translationsKeyName, translationsKeySubName, translationsKeyField, deleteValues);
		}
		
	}
	/*
	 * CREATE
	 */
	function createTranslations(translationsKeyType, translationsKeyName, 
			translationsKeySubName, translationsKeyField, values) {
		switch(translationsKeyType) {
			case "Class" :
				createForClass(translationsKeyName, translationsKeyField, values);
				break;
			case "ClassAttribute" :
				createForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "Domain" :
				createForDomain(translationsKeyName, translationsKeyField, values);
				break;
			case "DomainAttribute" :
				createForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "FilterView" :
				createForFilterView(translationsKeyName, translationsKeyField, values);
				break;
			case "SqlView" :
				createForSqlView(translationsKeyName, translationsKeyField, values);
				break;
			case "Filter" :
				createForFilter(translationsKeyName, translationsKeyField, values);
				break;
			case "InstanceName" :
				createForInstanceName(values);
				break;
			case "Widget" :
				createForWidget(translationsKeyName, translationsKeyField, values);
				break;
			case "Dashboard" :
				createForDashboard(translationsKeyName, translationsKeyField, values);
				break;
			case "Chart" :
				createForChart(translationsKeyName, translationsKeyField, values);
				break;
			case "Report" :
				createForReport(translationsKeyName, translationsKeyField, values);
				break;
			case "Lookup" :
				createForLookup(translationsKeyName, translationsKeyField, values);
				break;
			case "GisIcon" :
				createForGisIcon(translationsKeyName, translationsKeyField, values);
				break;
		}
		
	}
	function createForClass(translationsKeyName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForClass);
	}
	function createForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForClassAttribute);
	}
	function createForDomain(translationsKeyName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForDomain);
	}
	function createForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForDomainAttribute);
	}
	function createForFilterView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForFilterView);
	}
	function createForSqlView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForSqlView);
	}
	function createForFilter(translationsKeyName, translationsKeyField, values) {
		var params = {
				filterName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForFilter);
	}
	function createForInstanceName(values) {
		var params = {
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForInstanceName);
	}
	function createForWidget(translationsKeyName, translationsKeyField, values) {
		var params = {
				widgetId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForWidget);
	}
	function createForDashboard(translationsKeyName, translationsKeyField, values) {
		var params = {
				dashboardName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForDashboard);
	}
	function createForChart(translationsKeyName, translationsKeyField, values) {
		var params = {
				chartName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForChart);
	}
	function createForReport(translationsKeyName, translationsKeyField, values) {
		var params = {
				reportName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForReport);
	}
	function createForLookup(translationsKeyName, translationsKeyField, values) {
		var params = {
				lookupId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForLookup);
	}
	function createForGisIcon(translationsKeyName, translationsKeyField, values) {
		var params = {
				iconName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.createForGisIcon);
	}

	/*
	 * READ
	 */
	function readTranslations(translationsKeyType, translationsKeyName, 
			translationsKeySubName, translationsKeyField, callBack) {
		switch(translationsKeyType) {
			case "Class" :
				readForClass(translationsKeyName, translationsKeyField, callBack);
				break;
			case "ClassAttribute" :
				readForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, callBack);
				break;
			case "Domain" :
				readForDomain(translationsKeyName, translationsKeyField, callBack);
				break;
			case "DomainAttribute" :
				readForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, callBack);
				break;
			case "FilterView" :
				readForFilterView(translationsKeyName, translationsKeyField, callBack);
				break;
			case "SqlView" :
				readForSqlView(translationsKeyName, translationsKeyField, callBack);
				break;
			case "Filter" :
				readForFilter(translationsKeyName, translationsKeyField, callBack);
				break;
			case "InstanceName" :
				readForInstanceName(callBack);
				break;
			case "Widget" :
				readForWidget(translationsKeyName, translationsKeyField, callBack);
				break;
			case "Dashboard" :
				readForDashboard(translationsKeyName, translationsKeyField, callBack);
				break;
			case "Chart" :
				readForChart(translationsKeyName, translationsKeyField, callBack);
				break;
			case "Report" :
				readForReport(translationsKeyName, translationsKeyField, callBack);
				break;
			case "Lookup" :
				readForLookup(translationsKeyName, translationsKeyField, callBack);
				break;
			case "GisIcon" :
				readForGisIcon(translationsKeyName, translationsKeyField, callBack);
				break;
		}
	}
	function readForClass(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				className: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForClass);
	}
	function readForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, callBack) {
		var params = {
				className: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForClassAttribute);
	}
	function readForDomain(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				domainName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForDomain);
	}
	function readForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, callBack) {
		var params = {
				domainName: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForDomainAttribute);
	}
	function readForFilterView(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForFilterView);
	}
	function readForSqlView(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForSqlView);
	}
	function readForFilter(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				filterName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForFilter);
	}
	function readForInstanceName(callBack) {
		var params = {
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForInstanceName);
	}
	function readForWidget(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				widgetId: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForWidget);
	}
	function readForDashboard(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				dashboardName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForDashboard);
	}
	function readForChart(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				chartName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForChart);
	}
	function readForReport(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				reportName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForReport);
	}
	function readForLookup(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				lookupId: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForLookup);
	}
	function readForGisIcon(translationsKeyName, translationsKeyField, callBack) {
		var params = {
				iconName: translationsKeyName,
				field: translationsKeyField
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params, success: callBack}, CMDBuild.ServiceProxy.url.translations.readForGisIcon);
	}
		
	/*
	 * UPDATE
	 */
	function updateTranslations(translationsKeyType, translationsKeyName, 
			translationsKeySubName, translationsKeyField, values) {
		switch(translationsKeyType) {
			case "Class" :
				updateForClass(translationsKeyName, translationsKeyField, values);
				break;
			case "ClassAttribute" :
				updateForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "Domain" :
				updateForDomain(translationsKeyName, translationsKeyField, values);
				break;
			case "DomainAttribute" :
				updateForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "FilterView" :
				updateForFilterView(translationsKeyName, translationsKeyField, values);
				break;
			case "SqlView" :
				updateForSqlView(translationsKeyName, translationsKeyField, values);
				break;
			case "Filter" :
				updateForFilter(translationsKeyName, translationsKeyField, values);
				break;
			case "InstanceName" :
				updateForInstanceName(values);
				break;
			case "Widget" :
				updateForWidget(translationsKeyName, translationsKeyField, values);
				break;
			case "Dashboard" :
				updateForDashboard(translationsKeyName, translationsKeyField, values);
				break;
			case "Chart" :
				updateForChart(translationsKeyName, translationsKeyField, values);
				break;
			case "Report" :
				updateForReport(translationsKeyName, translationsKeyField, values);
				break;
			case "Lookup" :
				updateForLookup(translationsKeyName, translationsKeyField, values);
				break;
			case "GisIcon" :
				updateForGisIcon(translationsKeyName, translationsKeyField, values);
				break;
		}
		
	}
	function updateForClass(translationsKeyName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForClass);
	}
	function updateForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForClassAttribute);
	}
	function updateForDomain(translationsKeyName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForDomain);
	}
	function updateForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForDomainAttribute);
	}
	function updateForFilterView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForFilterView);
	}
	function updateForSqlView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForSqlView);
	}
	function updateForFilter(translationsKeyName, translationsKeyField, values) {
		var params = {
				filterName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForFilter);
	}
	function updateForInstanceName(values) {
		var params = {
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForInstanceName);
	}
	function updateForWidget(translationsKeyName, translationsKeyField, values) {
		var params = {
				widgetId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForWidget);
	}
	function updateForDashboard(translationsKeyName, translationsKeyField, values) {
		var params = {
				dashboardName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForDashboard);
	}
	function updateForChart(translationsKeyName, translationsKeyField, values) {
		var params = {
				chartName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForChart);
	}
	function updateForReport(translationsKeyName, translationsKeyField, values) {
		var params = {
				reportName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForReport);
	}
	function updateForLookup(translationsKeyName, translationsKeyField, values) {
		var params = {
				lookupId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForLookup);
	}
	function updateForGisIcon(translationsKeyName, translationsKeyField, values) {
		var params = {
				iconName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.updateForGisIcon);
	}
		
	/*
	 * DELETE
	 */
	function deleteTranslations(translationsKeyType, translationsKeyName, 
			translationsKeySubName, translationsKeyField, values) {
		switch(translationsKeyType) {
			case "Class" :
				deleteForClass(translationsKeyName, translationsKeyField, values);
				break;
			case "ClassAttribute" :
				deleteForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "Domain" :
				deleteForDomain(translationsKeyName, translationsKeyField, values);
				break;
			case "DomainAttribute" :
				deleteForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "FilterView" :
				deleteForFilterView(translationsKeyName, translationsKeyField, values);
				break;
			case "SqlView" :
				deleteForSqlView(translationsKeyName, translationsKeyField, values);
				break;
			case "Filter" :
				deleteForFilter(translationsKeyName, translationsKeyField, values);
				break;
			case "InstanceName" :
				deleteForInstanceName(values);
				break;
			case "Widget" :
				deleteForWidget(translationsKeyName, translationsKeyField, values);
				break;
			case "Dashboard" :
				deleteForDashboard(translationsKeyName, translationsKeyField, values);
				break;
			case "Chart" :
				deleteForChart(translationsKeyName, translationsKeyField, values);
				break;
			case "Report" :
				deleteForReport(translationsKeyName, translationsKeyField, values);
				break;
			case "Lookup" :
				deleteForLookup(translationsKeyName, translationsKeyField, values);
				break;
			case "GisIcon" :
				deleteForGisIcon(translationsKeyName, translationsKeyField, values);
				break;
		}
		
	}
	function deleteForClass(translationsKeyName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForClass);
	}
	function deleteForClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForClassAttribute);
	}
	function deleteForDomain(translationsKeyName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForDomain);
	}
	function deleteForDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForDomainAttribute);
	}
	function deleteForFilterView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForFilterView);
	}
	function deleteForSqlView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForSqlView);
	}
	function deleteForFilter(translationsKeyName, translationsKeyField, values) {
		var params = {
				filterName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForFilter);
	}
	function deleteForInstanceName(values) {
		var params = {
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForInstanceName);
	}
	function deleteForWidget(translationsKeyName, translationsKeyField, values) {
		var params = {
				widgetId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForWidget);
	}
	function deleteForDashboard(translationsKeyName, translationsKeyField, values) {
		var params = {
				dashboardName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForDashboard);
	}
	function deleteForChart(translationsKeyName, translationsKeyField, values) {
		var params = {
				chartName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForChart);
	}
	function deleteForReport(translationsKeyName, translationsKeyField, values) {
		var params = {
				reportName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForReport);
	}
	function deleteForLookup(translationsKeyName, translationsKeyField, values) {
		var params = {
				lookupId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForLookup);
	}
	function deleteForGisIcon(translationsKeyName, translationsKeyField, values) {
		var params = {
				iconName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.deleteForGisIcon);
	}
		

})();