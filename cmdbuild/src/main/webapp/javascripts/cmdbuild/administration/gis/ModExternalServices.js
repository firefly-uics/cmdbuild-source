(function() {
	var tr = CMDBuild.Translation.administration.modcartography.external_services;

	var KEY_FIELD = "key";
	var URL_FIELD = "url";
	var WORKSPACE_FIELD = "workspace";
	var ADMIN_USER_FIELD = "admin_user";
	var ADMIN_PASSWORD_FIELD = { name: "admin_password", inputType: 'password' };

	var createServiceField = function(serviceName, fieldSpec) {
		var fieldName = fieldSpec.name || fieldSpec;
		var fieldInputType = fieldSpec.inputType || 'text';
		return new Ext.form.TextField({
			name: serviceName+"_"+fieldName,
            fieldLabel: tr[fieldName],
            inputType: fieldInputType,
            width: 300,
            allowBlank: false
		});
	};

	/*
	 * FIXME change translation name when possible
	 */
	var createSliderField = function(serviceName, fieldName, translationName) {
		return new Ext.form.SliderField({
		    minValue: 0,
		    maxValue: 25,
		    value: 0,
		    width: 300,
		    name: serviceName+"_"+fieldName,
		    fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes[translationName],
		    clickToChange: false,
		    animate: false
		});
	};

	/*
	 * pass to it an object like: { 
	 * 	    serviceName: String,
	 *      serviceFields: (String|Object)[]
	 *      withZoom: boolean
	 * }
	 */
	var buildServiceFieldset = function(o) {
		var serviceFields = o.serviceFields || [];
		var items = [];

		for (var i=0, l=serviceFields.length; i<l; ++i) {
			var field = createServiceField(o.serviceName, serviceFields[i]);
			items.push(field);
		}
		
		if (o.withZoom) {
			var range = new CMDBuild.RangeSlidersFieldSet( {
				minSliderField: createSliderField(o.serviceName, "minzoom", "min_zoom"),
				maxSliderField: createSliderField(o.serviceName, "maxzoom", "max_zoom")
			});
			items.push( range );
		}
		
		var setDisabled = function(disabled) {
			for (var i in items) {
				try {
					items[i].setDisabled(disabled);
				} catch (Error) {}
			}
		};
		
		return {
			xtype: "fieldset",
			title: tr.description[o.serviceName],
			checkboxToggle: true,
	        collapsed: false,
	        autoWidth: true,
	        serviceName: o.serviceName,
            layout: "form",
            items: items,
            checkboxName: o.serviceName,
            listeners: {
				expand: function(){setDisabled(false);},
				collapse: function(){setDisabled(true);}
			}
		};
	};
	
	var collapseFieldsets = function(formPanel, data) {
		var collapseFieldset = function(serviceName) {
			if (data[serviceName] == "off") {
				var fieldset = formPanel.find("serviceName", serviceName); 
				if (fieldset[0]) {
					fieldset[0].collapse();
				}
			}
		};
		
		for (var service in formPanel.services) {
			collapseFieldset(service);
		}
	};
	
	var fillForm = function(formPanel, data) {
		for (var name in data) {
			var field = formPanel.find("name", name);
			if (field[0]) {
				field[0].setValue(data[name]);
			}
		}
	};
	
	var getConfigFromServer = function(){
		if (this.loaded) {
			return;
		} else {
			CMDBuild.Ajax.request({
				url : String.format('services/json/schema/setup/getconfiguration'),
				params: {
					name: "gis"
				},
				success: function(response){
					var decodedResponse = Ext.util.JSON.decode(response.responseText);
					this.loaded = true;
					fillForm(this, decodedResponse.data);
					collapseFieldsets(this, decodedResponse.data);
				},
				scope: this
			});
		}
	};
	
	CMDBuild.Administration.ModExternalServices = Ext.extend(CMDBuild.ModPanel, {
	    modtype: "gis-external-services",
	
	    initComponent: function() {
			var services = {
				google: {
					serviceName: "google",
    		  		serviceFields: [KEY_FIELD],
    		  		withZoom: true
				},
				yahoo: {
	   		  		serviceName: "yahoo",
	   		  		serviceFields: [KEY_FIELD],
	   		  		withZoom: true
				},
				osm: {
	   		  		serviceName: "osm",
	   		  		withZoom: true
				},
				geoserver: {
	   		  		serviceName: "geoserver",
	   		  		serviceFields: [URL_FIELD, WORKSPACE_FIELD, ADMIN_USER_FIELD, ADMIN_PASSWORD_FIELD]	   		  		
               }
			};
			
			var form = new Ext.form.FormPanel({
				url : 'services/json/schema/setup/saveconfiguration',
				plugins: [new CMDBuild.CallbackPlugin()],
				baseParams: {
					name: "gis"
				},
				labelWidth: 200,
				services: services,
	            frame: true,
	            border: false,
	            labelSeparator: "",
	            monitorValid: true,
	            items: (function(){
	            	var items = [];
	    			for (var service in services) {
	    				items.push(buildServiceFieldset(services[service]));
	    			}
	    			return items;
	            })()
			});
			
			var saveButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.save,
				disabled: true,
				scope: this,
				handler: function() {
					CMDBuild.LoadMask.get().show();
					var collapsedField = function() {
						var services = {};
						for (service in form.services) {
							var fieldset = form.find("serviceName", service);
							if (fieldset[0] && fieldset[0].collapsed) {
								services[service] = "off";
							}
						}
						return services;
					};
					
					form.getForm().submit({
						params: collapsedField(),
						success: function() {
							var valuesToApply = form.getForm().getValues();
							Ext.apply(valuesToApply, collapsedField());
							CMDBuild.Config.gis = Ext.apply(CMDBuild.Config.gis,valuesToApply);
							form.publish("cmdb-geoservices-config-changed");
						},
						callback: function() {
							CMDBuild.LoadMask.get().hide();
						}
					});
				}
			});
			
			Ext.apply(this, {
		        title: tr.title,
		        frame: true,
		        border: true,		        
		        layout: "fit",	            
		        items: [ {
		        	xtype: "panel",
		        	autoScroll: true,
		        	items: [form]
		        } ],
		        buttonAlign: "center",
	            buttons: [ saveButton ]
		    });
		    CMDBuild.Administration.ModExternalServices.superclass.initComponent.apply(this, arguments);
		    form.on("clientvalidation", function(form, valid) {
		    	saveButton.setDisabled(!valid);
		    });
		    this.on("show", getConfigFromServer, form);
	    }
	});	
})();