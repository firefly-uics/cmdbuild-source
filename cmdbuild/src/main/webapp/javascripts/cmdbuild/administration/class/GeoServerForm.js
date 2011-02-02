(function() {
	var FIELDSWIDTH = 200;
	var LABELSWIDTH = 150;
	var tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geoserver = CMDBuild.Translation.administration.modcartography.geoserver;
	var tr = CMDBuild.Translation.administration.modClass.geo_attributes;
	var TYPES = {
		geotiff: "GEOTIFF",
		worldimage: "WORLDIMAGE",
		shpe: "SHAPE"
	};
	
	//for the ajax requests
	var callback = function() {
		CMDBuild.LoadMask.get().hide();
		this.disableModify();
		this.disableToolbarButtons();
		this.getForm().reset();
	};
	
	var items = function() {
		var name = new Ext.form.TextField({
	    	fieldLabel: tr_attribute.name,
	        name: "name",
	        allowBlank: false,
	        width: FIELDSWIDTH
	    });
		
		var types = new Ext.form.ComboBox({
			width: FIELDSWIDTH,
		    store: new Ext.data.SimpleStore( {
		        fields: [ "value", "name" ],
		        data: [[ TYPES.geotiff, "GeoTiff" ],
		               [ TYPES.worldimage, "WorldImage" ],
		               [ TYPES.shpe, "Shape" ]]
		    }),
		    allowBlank: false,
		    autoScroll: true,
		    name: "type",
		    fieldLabel: tr_attribute.type,
		    valueField: "value",
		    displayField: "name",
		    hiddenName: "type",
		    mode: "local",
		    triggerAction: "all"			
		});
		
		var minZoom = new Ext.form.SliderField({
		    fieldLabel: tr.min_zoom,
		    plugins: [new CMDBuild.SliderFieldPlugin()],
		    width: FIELDSWIDTH,
		    minValue: 0,
		    maxValue: 25,
		    name: "minZoom"
		});
		
		var maxZoom = new Ext.form.SliderField({
		    fieldLabel: tr.max_zoom,
		    plugins: [new CMDBuild.SliderFieldPlugin()],
		    width: FIELDSWIDTH,
		    minValue: 0,
		    maxValue: 25,
		    value: 25,
		    name: "maxZoom"
		});
		
		var range = new CMDBuild.RangeSlidersFieldSet( {
			maxSliderField: maxZoom,
			minSliderField: minZoom
		});
		
		var description = new Ext.form.TextField({
	        xtype: "textfield",
	        fieldLabel: tr_attribute.description,
	        name: "description",
	        allowBlank: false,
	        width: FIELDSWIDTH
	    });
		
		var file = new Ext.form.TextField({
			inputType : "file",
			fieldLabel: tr_geoserver.file,
			name: "file",
			width: FIELDSWIDTH,
			form: this
		});
		
		name.on("change", function(fieldname, newValue, oldValue) {
			this.autoComplete(description, newValue, oldValue);
		}, this);
		
		var modifiableFields = [range, description, file];
		
		this.enableModifiableFields = function() {
			for (var i=0, l=modifiableFields.length; i<l; ++i) {
				modifiableFields[i].enable();
			}
		};
		
		this.getName = function() {
			return name.getValue();
		};
		return [name, description, file, types, range];
	};
	
	var buildButtons = function() {
		var onSave = function() {
			CMDBuild.LoadMask.get().show();
			var url = this.lastSelection ?
					CMDBuild.ServiceProxy.geoServer.modifyUrl:
					CMDBuild.ServiceProxy.geoServer.addUrl;
			
			// used to select the right record after store reload
			var nameToSelect = this.getName();
			
			this.getForm().submit({
				method: 'POST',
				url: url,
				params: {
					name: nameToSelect
				},
				scope: this,
				success: function(form, action, result) {
					this.publish("cmdb-modified-geoserverlayers", {nameToSelect: nameToSelect});			
				},
				failure: function() {
					_debug("Failed to add or modify a Geoserver Layer", arguments);	
				},
				callback: callback.createDelegate(this)
			});
		};
		
		this.saveButton = new Ext.Button({
		    text: CMDBuild.Translation.common.buttons.save,
		    scope: this,
		    disabled: true,
		    handler: onSave		    
		});

		this.abortButton = new Ext.Button({
		    text: CMDBuild.Translation.common.buttons.abort,
		    scope: this,
		    disabled: true,
		    handler: function() {
				this.disableModify();
				this.disableToolbarButtons();
				if (this.lastSelection) {
		    		this.getForm().loadRecord(this.lastSelection);
		    		this.enableToolbarButtons();
		    	} else {
		    		this.getForm().reset();
		    	}
			}
		});
		
		this.enableButtons = function() {
			this.saveButton.enable();
			this.abortButton.enable();
		};
		
		return [this.saveButton, this.abortButton];
	};
	
	var buildTBarTools = function() {
		var onDelete = function() {
			CMDBuild.LoadMask.get().show();
			var onSuccess = function() {
				this.publish("cmdb-modified-geoserverlayers");				
			};
			
			CMDBuild.ServiceProxy.geoServer.deleteLayer({
				name: this.getName()
			},
			onSuccess.createDelegate(this),
			Ext.emptyFn,
			callback.createDelegate(this));
		};
		
		this.deleteButton = new Ext.Button({
        	text: tr_geoserver.delete_layer,
            iconCls: 'delete',
            scope: this,
            disabled: true,
            handler: onDelete
        });
		
		this.modifyButton = new Ext.Button({
        	text: tr_geoserver.modify_layer,
            iconCls: "modify",
            scope: this,
            disabled: true,
            handler: this.enableModify
        });
		
		this.disableToolbarButtons = function() {
			this.modifyButton.disable();
			this.deleteButton.disable();
		};
		
		this.enableToolbarButtons = function() {
			this.modifyButton.enable();
			this.deleteButton.enable();
		};
		return [this.modifyButton, this.deleteButton];
	};
	
	
	/**
	 * GeoServerForm
	 */
	CMDBuild.Administration.GeoServerForm = Ext.extend(Ext.form.FormPanel, {
	    hideMode: "offsets",
	    fileUpload: true,
	    plugins: [new CMDBuild.FormPlugin(), new CMDBuild.CallbackPlugin()],
	    frame: false,
        bodyCssClass: "cmdbuild_background_gray cmdbuild_body_padding",
        defaults: {
			disabled: true
		},
		
	    initComponent: function() {
			this.tbar = buildTBarTools.call(this);
        	this.items = items.call(this);
        	this.buttonAlign = "center";
		    this.buttons = buildButtons.call(this);
		    CMDBuild.Administration.GeoServerForm.superclass.initComponent.apply(this, arguments);    
		    this.setFieldsDisabled();
		    
		    this.on("clientvalidation", function(formPanel, valid ) {		    	
		    	this.saveButton.setDisabled(!valid);
		    }, this);
		    
	    },
	    
	    onAddLayer: function() {
	    	this.lastSelection = undefined;
	    	this.getForm().reset();
	    	this.enableModify();
	    },
	    
	    enableModify: function() {
	    	this.disableToolbarButtons();
	    	if (this.lastSelection) {
	    		// we are in modify mode
	    		this.enableModifiableFields();
	    		this.startMonitoring();
	    		this.enableButtons();	    		
	    	} else {
	    		this.setFieldsEnabled();
	    	}
	    },
	    
	    disableModify: function() {
	    	this.enableToolbarButtons();
	    	this.setFieldsDisabled();	    	
	    },
	    
	    onRowSelect: function(sm, rown, rec) {
	    	this.lastSelection = rec;
	    	this.enableToolbarButtons();
	    	this.getForm().loadRecord(rec);
	    }
	});
	Ext.reg("geoattributeform", CMDBuild.Administration.GeoAttributeForm);
})();