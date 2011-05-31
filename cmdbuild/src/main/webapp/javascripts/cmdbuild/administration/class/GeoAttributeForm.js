(function() {
	var FIELDSWIDTH = 400;
	var LABELSWIDTH = 150;
	
	var point = CMDBuild.Constants.geoTypes.point;
	var line = CMDBuild.Constants.geoTypes.line;
	var polygon = CMDBuild.Constants.geoTypes.polygon;
	var tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr = CMDBuild.Translation.administration.modClass.geo_attributes;

	Ext.define("CMDBuild.Administration.GeoAttributeForm", {
		extend: "Ext.form.FormPanel",
		alias: "geoattributeform",

		initComponent: function() {
			this.genericProperties = getGenericProperties.call(this);
			this.styleFieldsMap = getStyleFieldsMap();

			this.types = new Ext.form.ComboBox( {
				store: new Ext.data.SimpleStore( {
					fields: [ "value", "name" ],
					data: [
						[ point, tr.type.point ],
						[ line, tr.type.line ],
						[ polygon, tr.type.polygon ]
					]
				}),
				allowBlank: false,
				autoScroll: true,
				name: "type",
				fieldLabel: tr_attribute.type,
				valueField: "value",
				displayField: "name",
				queryMode: "local"
			});

			this.types.setValue = Ext.Function.createInterceptor(
				this.types.setValue,
				function(value) {
					for (var i=0, l=this.styleFields.length; i<l; ++i) {
						var f = this.styleFields[i];
						f.setVisible(f.allowedGeoTypes[value]);
						// sync the sliders position. 
						// Thanks for the 40 minutes spent here
						if (f.slider) {
							f.slider.syncThumb();
						}
					}
				},
				this
			);

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
				items: [ this.types ].concat(this.styleFields),
				labelWidth: LABELSWIDTH,
				defaults: {
					width: FIELDSWIDTH
				}
			});

			buildButtons.call(this);

			Ext.apply(this, {
				frame: true,
				border: "0 0 0 0",
				layout: "border",
				tbar: [this.modifyButton ,this.cancelButton],
				items: [{
					xtype: "panel",
					region: "center",
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

			this.callParent(arguments);
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
		}

	});

	function getGenericProperties() {
		this.name = new Ext.form.TextField({
			fieldLabel: tr_attribute.name,
			name: "name",
			allowBlank: false
		});

		this.description = new Ext.form.TextField({
			fieldLabel: tr_attribute.description,
			name: "description",
			allowBlank: false
		});

		this.name.on("change", function(fieldname, newValue, oldValue) {
			this.autoComplete(this.description, newValue, oldValue);
		}, this);

		this.minZoom = new Ext.form.SliderField( {
			fieldLabel: tr.min_zoom,
			minValue: 0,
			maxValue: 25,
			name: "minZoom"
		});

		this.maxZoom = new Ext.form.SliderField( {
			fieldLabel: tr.max_zoom,
			minValue: 0,
			maxValue: 25,
			value: 25,
			name: "maxZoom"
		});

		return new Ext.form.FieldSet( {
			title: tr_attribute.baseProperties,
			flex: 1,
			autoScroll: true,
			labelWidth: LABELSWIDTH,
			defaults: {
				width: FIELDSWIDTH
			},
			items: [this.name, this.description, this.minZoom, this.maxZoom]
		});
	};

	function buildButtons() {
		this.saveButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.save
		});

		this.abortButton = new Ext.button.Button( {
			text: CMDBuild.Translation.common.buttons.abort
		});

		this.cancelButton = new Ext.button.Button({
			text: tr_attribute.delete_attribute,
			iconCls: 'delete'
		});

		this.modifyButton = new Ext.button.Button({
			text: tr_attribute.modify_attribute,
			iconCls: 'modify'
		});
	};

	function getStyleFieldsMap() {
		return {

			// TODO Extjs 3 to 4 - the template to show the icon
			externalGraphic: new Ext.form.ComboBox({
				store: CMDBuild.ServiceProxy.Icons.getIconStore(),
				name: "externalGraphic",
				hiddenName: "externalGraphic",
				fieldLabel: tr.externalGraphic,
				allowedGeoTypes: {POINT: true},
				valueField: "path",
				displayField: "description",
				queryMode: "local"
			}),

			pointRadius: new Ext.form.field.Number({
				fieldLabel: tr.pointRadius,
				defaultValue: 6,
				minValue: 0,
				maxValue: 100,
				allowedGeoTypes: {
					POINT: true
				},
				name: 'pointRadius'
			}),

//			fillColor: new CMDBuild.form.HexColorField( {
//				name: "fillColor",
//				fieldLabel: tr.fillColor,
//				allowedGeoTypes: {POINT: true, POLYGON: true}
//			}),

			fillOpacity: new Ext.form.SliderField( {
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
				tipText: function(thumb) {
					return String(thumb.value*100) + '%';
				}
			}),

//		   strokeColor: new CMDBuild.form.HexColorField( {
//			   disabled: true,
//		        name: "strokeColor",
//		        fieldLabel: tr.strokeColor,
//		        defaultValue: "000000",
//		        hidden: true,
//		        allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true}		      
//		   }),

			strokeOpacity:  new Ext.form.SliderField( {
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
				tipText: function(thumb) {
					return String(thumb.value*100) + '%';
				}
			}),

			strokeWidth: new Ext.form.field.Number({
				fieldLabel: tr.strokeWidth,
				name: "strokeWidth",
				defaultValue: 1,
				minValue: 0,
				maxValue: 10,
				allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true}
			}),

			strokeDashstyle: new Ext.form.ComboBox( {
				store: new Ext.data.SimpleStore( {
					fields: ["value", "name"],
					data: [
						["dot", tr.strokeStyles.dot], 
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
				queryMode: "local"
			})
		}
	};
})();