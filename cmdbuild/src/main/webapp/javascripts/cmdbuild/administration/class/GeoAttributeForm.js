(function() {
	var FIELDSWIDTH = 200;
	var LABELSWIDTH = 150;
	
	var point = CMDBuild.Constants.geoTypes.point;
	var line = CMDBuild.Constants.geoTypes.line;
	var polygon = CMDBuild.Constants.geoTypes.polygon;
	var tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr = CMDBuild.Translation.administration.modClass.geo_attributes;

	var getStyleFieldsMap = function() {
		return {
			externalGraphic: new Ext.form.ComboBox({
				defaultValue: "",
			    store: new Ext.data.JsonStore({
			        url: "services/json/gis/geticonslist",
			        root: "rows",
			        fields: [ 'name', 'description', 'path' ],
			        autoLoad: true
			    }),
			    tpl: new Ext.XTemplate(
		    		'<tpl for=".">',
		        		'<div class="icon-item">',
			        		'<div class="icon-item-image-wrap"><img src="{path}" alt="{description}" class="icon-item-image"/></div>',
			        		'<div class="icon-item-label">{description}</div>',
		        		'</div>',
		    		'</tpl>'
			    ),
			    itemSelector: ".icon-item",
			    name: "externalGraphic",
			    hiddenName: "externalGraphic",
			    fieldLabel: tr.externalGraphic,
				allowedGeoTypes: {POINT: true},
			    valueField: "path",
			    displayField: "description",
			    mode: "local",
			    triggerAction: "all",
			    hidden: true,
			    disabled: true
			}),
		   pointRadius: new Ext.ux.form.SpinnerField({
		        disabled: true,
		        fieldLabel: tr.pointRadius,
		        defaultValue: 6,
		        minValue: 0,
		        maxValue: 100,
		        hidden: true,
		        allowedGeoTypes: {
			        POINT: true
		        },
		        name: 'pointRadius'		        
		   }),
           fillColor: new CMDBuild.form.HexColorField( {
        	   	disabled: true,
		        name: "fillColor",
		        fieldLabel: tr.fillColor,
		        defaultValue: "000000",
		        hidden: true,
		        allowedGeoTypes: {POINT: true, POLYGON: true}		        
		   }),
		   fillOpacity:  new Ext.form.SliderField( {
			   	disabled: true,
			   	minValue: 0,
			    maxValue: 1,
			    defaultValue: 1,			    
			    decimalPrecision: 1,
			    increment: 0.1,
		        name: "fillOpacity",
		        fieldLabel: tr.fillOpacity,		        
		        allowedGeoTypes: {POINT: true, POLYGON: true},
		        clickToChange: true,
			    animate: false,
			    hidden: true,
			    tipText: function(thumb) {
			        return String(thumb.value*100) + '%';
			    }
		   }),
		   strokeColor: new CMDBuild.form.HexColorField( {
			   disabled: true,
		        name: "strokeColor",
		        fieldLabel: tr.strokeColor,
		        defaultValue: "000000",
		        hidden: true,
		        allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true}		      
		   }),
		   strokeOpacity:  new Ext.form.SliderField( {
			   	disabled: true,
			  	minValue: 0,
			    maxValue: 1,
			    defaultValue: 1,			    
			    decimalPrecision: 1,
			    increment: 0.1,
		        name: "strokeOpacity",
		        fieldLabel: tr.strokeOpacity,	        
		        allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true},
		        clickToChange: true,
			    animate: false,
			    hidden: true,			    
			    tipText: function(thumb) {
			        return String(thumb.value*100) + '%';
			    }
		   }),		   
		   strokeWidth: new Ext.ux.form.SpinnerField({
			   disabled: true,
			   fieldLabel: tr.strokeWidth,
			   name: "strokeWidth",
			   defaultValue: 1,
			   hidden: true,
			   minValue: 0,
			   maxValue: 10,
			   allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true}			   			   			   		        		    
		   }),		   
		   strokeDashstyle: new Ext.form.ComboBox( {
			   disabled: true,
			    store: new Ext.data.SimpleStore( {
			        fields: ["value", "name"],
			        data: [["dot", tr.strokeStyles.dot], 
			               ["dash", tr.strokeStyles.dash],
			               ["dashdot", tr.strokeStyles.dashdot],
			               ["longdash", tr.strokeStyles.longdash],
			               ["longdashdot", tr.strokeStyles.longdashdot],
			               ["solid", tr.strokeStyles.solid]]
			    }),
			    autoScroll: true,
			    name: "strokeDashstyle",
			    hiddenName: "strokeDashstyle",
			    fieldLabel: tr.strokeDashstyle,
			    allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true},
			    valueField: "value",
			    displayField: "name",
			    mode: "local",
			    triggerAction: "all",			    
			    hidden: true,
			    defaultValue: "solid"			  
		   })
		};			
	};
	
	var getGenericProperties = function() {
		this.name = new Ext.form.TextField({
	    	disabled: true,
	        fieldLabel: tr_attribute.name,
	        name: "name",
	        allowBlank: false,
	        width: FIELDSWIDTH
	    });
		
		this.description = new Ext.form.TextField({
	    	disabled: true,
	        xtype: "textfield",
	        fieldLabel: tr_attribute.description,
	        name: "description",
	        allowBlank: false,
	        width: FIELDSWIDTH
	    });
		
		this.name.on("change", function(fieldname, newValue, oldValue) {
			this.autoComplete(this.description, newValue, oldValue);
		}, this);
		
		
		var minZoom = new Ext.form.SliderField( {
		    fieldLabel: tr.min_zoom,
		    width: FIELDSWIDTH,
		    minValue: 0,
		    maxValue: 25,
		    name: "minZoom"
		});
		
		var maxZoom = new Ext.form.SliderField( {
		    fieldLabel: tr.max_zoom,
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
		
		return new Ext.form.FieldSet( {
		    title: tr_attribute.baseProperties,
		    layout: "form",
		    flex: 1,
		    autoScroll: true,
		    items: [this.name, this.description, range]
		});		
	};
	
	var callback = function() {
		CMDBuild.LoadMask.get().hide();
	};
	
    var onSave = function() {
    	var params = this.getForm().getValues();
    	var out = {};
    	var style = this.getStyle();
    	
    	for (var key in params) {
    		if (!this.styleFieldsMap[key]) {
    			out[key] = params[key];
    		}
    	}
    	
    	out.style = Ext.encode(style);
    	out.idClass = this.classId;    	
    	out.name = this.name.getValue();
    	
    	CMDBuild.LoadMask.get().show();
    	if (this.modifyMode) {
    		var onSuccess = function(response, request, decoded) {
    			this.publish("cmdb-modify-geoattr", decoded);
    		};
    		CMDBuild.ServiceProxy.modifyGeoAttribute(out, 
    				onSuccess.createDelegate(this),
    				Ext.emptyFn,
    				callback.createDelegate(this));
    	} else {
    		var onSuccess = function(response, request, decoded) {
    			this.publish("cmdb-new-geoattr", decoded);
    		};
    		CMDBuild.ServiceProxy.saveGeoAttribute(out,
    				onSuccess.createDelegate(this),
    				Ext.emptyFn,
    				callback.createDelegate(this));
    	}    	
    };
    
    var onAbort = function() {
    	this.resetForm();
    	this.disableAllField();
    	this.saveButton.disable();
    	this.abortButton.disable();
    	this.stopMonitoring();
    };
    
    var onModify = function() {
    	this.modifyMode = true;
    	this.enableAllField();
    	this.types.disable();
    	this.name.disable();
    	this.saveButton.enable();
    	this.abortButton.enable();
    	this.startMonitoring();
    };
    
    var onDelete = function() {
    	var onSuccess = function(response, request, decoded) {
    		var table = {id: this.classId};
    		var geoAttribute = {name: this.currentName};
			this.publish("cmdb-delete-geoattr", {table: table, geoAttribute: geoAttribute});
		};
		CMDBuild.LoadMask.get().show();
    	CMDBuild.ServiceProxy.deleteGeoAttribute(this.classId,
    			this.currentName, 
    			onSuccess.createDelegate(this),
    			Ext.emptyFn,
    			callback.createDelegate(this));
    };
    
    var buildButtons = function() {
    	this.saveButton = new Ext.Button({
            text: CMDBuild.Translation.common.buttons.save,
            scope: this,
            handler: onSave,
            formBind: true            
        });
		
		this.abortButton = new Ext.Button( {
        	text: CMDBuild.Translation.common.buttons.abort,
        	scope: this,
            handler: onAbort	            
        });
		
		this.cancelAction = new Ext.Button({
        	text: tr_attribute.delete_attribute,
            iconCls: 'delete',
            scope: this,
            handler: onDelete
        });
		
		this.modifyAction = new Ext.Button({
        	text: tr_attribute.modify_attribute,
            iconCls: 'modify',
            scope: this,
            handler: onModify
        });
    };
    
    var enableCancelAndModifyAction = function() {
    	var canEnable = (this.currentFeatureType && this.currentFeatureType.data.masterTableId == this.classId);
    	this.cancelAction.setDisabled(!canEnable);
    	this.modifyAction.setDisabled(!canEnable);
    };
    
    var disableAll = function() {
    	this.stopMonitoring();
    	this.modifyAction.disable();
    	this.cancelAction.disable();
    	this.saveButton.disable();
    	this.abortButton.disable();
    	if (this.isVisible()) {    		
	    	this.disableAllField();	
    	}    	
    };
    
	/**
	 * GeoAttributeForm	
	 */
	CMDBuild.Administration.GeoAttributeForm = Ext.extend(Ext.form.FormPanel, {
	    eventtype: "class",
	    plugins: [new CMDBuild.FormPlugin()],
	    initComponent: function() {
		   	this.genericProperties = getGenericProperties.call(this);
		    this.styleFieldsMap = getStyleFieldsMap();
		    
		    this.types = new Ext.form.ComboBox( {
		    	disabled: true,		    	
			    store: new Ext.data.SimpleStore( {
			        fields: [ "value", "name" ],
			        data: [[ point, tr.type.point ],
			               [ line, tr.type.line ],
			               [ polygon, tr.type.polygon ]]
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
		    
		    this.types.setValue = this.types.setValue.createInterceptor(function(value) {
				for (var i=0, l=this.styleFields.length; i<l; ++i) {
					var f = this.styleFields[i];								
					f.setVisible(f.allowedGeoTypes[value]);
					// sync the sliders position. 
					// Thanks for the 40 minutes spent here
					if (f.slider) {						
						f.slider.syncThumb();
					}
				}
			}, this);
			
			this.styleFields = (function(fieldsMap) {
				var out = [];
				for (var field in fieldsMap) {
					out.push(fieldsMap[field]);
				}
				return out;
			})(this.styleFieldsMap) || [];
			
			this.styleProperties = new Ext.form.FieldSet({
			    title: tr.style,
			    flex: 1,
			    autoScroll: true,
			    margins: "0 0 0 5",
			    layout: "form",
			    items: [ this.types ].concat(this.styleFields),
			    labelWidth: LABELSWIDTH,
			    defaults: {					
					width: FIELDSWIDTH
				}
			});
			 
			
			buildButtons.call(this);
		    Ext.apply(this, {
		        frame: false,
		        monitorValid: true,
		        bodyCssClass: "cmdbuild_background_gray cmdbuild_body_padding",
		        tbar: [this.modifyAction ,this.cancelAction],
		        items: [{
		    		xtype: "panel",
		    		layout: "hbox",
		    		frame: true,
		    		layoutConfig: {
		    			padding: "5",
		    			align: "stretch"
			        },
			        items: [this.genericProperties, this.styleProperties]
			    }],
		        buttonAlign: "center",
		        buttons: [this.saveButton, this.abortButton]
		    });
		    CMDBuild.Administration.GeoAttributeForm.superclass.initComponent.apply(this, arguments);
		    this.subscribe("cmdg-icons-reload", function(){
		    	this.styleFieldsMap.externalGraphic.store.reload();
		    }, this);
	    },
	    
	    getStyle: function() {	
    		var out = {};
    		for (var fieldName in this.styleFieldsMap) {
    			var field = this.styleFieldsMap[fieldName];
    			var value = field.getValue();
    			if (field.isVisible() && value!="") {
    				out[fieldName] = value;
    			}
    		}
    		return out;
	    },
	    
	    loadRecordAfterGridSelection: function(record) {
	    	this.getForm().reset();
	    	this.saveButton.disable();
	    	this.stopMonitoring();
	    	this.getForm().loadRecord(record);
	    	this.currentName = record.data.name;
	    	this.currentFeatureType = record;
	    	
	    	enableCancelAndModifyAction.call(this);
	    	
	    	var style = Ext.decode(record.data.style);
	    	if (style) {
	    		var recFields = [];
	    		for (var propName in style) {
	    			recFields.push({name: propName, mapping: propName});
	    		}	    		
	    		var StyleRec = Ext.data.Record.create(recFields);
	    		var styleRec = new StyleRec(style);
	    		this.getForm().loadRecord(styleRec);
	    	}
	    	
	    },
	    
	    setClass: function(classId) {
	    	this.classId = classId;
	    	this.saveButton.disable();
	    	this.stopMonitoring();
	    	disableAll.call(this);
	    	if (this.isVisible()) {
	    		this.resetForm();
	    	}
	    },
	    
	    resetForm: function() {
	    	this.getForm().items.each(function(f){
	            if (f.defaultValue) {
	            	f.setValue(f.defaultValue);
	            } else {
	            	f.reset();
	            }
	            f.setVisible(!f.allowedGeoTypes); //hide the styles prop
	        });	    	
	    },
	    
	    prepareToAdd: function() {
	    	this.resetForm();
	    	this.enableAllField();
	    	this.saveButton.enable();
	    	this.abortButton.enable();
	    	this.startMonitoring();
	    	this.modifyMode = false; // exit to modify mode
	    }
	});
	Ext.reg("geoattributeform", CMDBuild.Administration.GeoAttributeForm);
})();