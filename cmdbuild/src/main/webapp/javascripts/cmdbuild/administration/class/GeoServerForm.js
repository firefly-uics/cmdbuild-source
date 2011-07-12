(function() {
	var FIELDSWIDTH = 250,
		LABELSWIDTH = 150,
		tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties,
		tr_geoserver = CMDBuild.Translation.administration.modcartography.geoserver,
		tr = CMDBuild.Translation.administration.modClass.geo_attributes,
		TYPES = {
			geotiff: "GEOTIFF",
			worldimage: "WORLDIMAGE",
			shpe: "SHAPE"
		};

	Ext.define("CMDBuild.Administration.GeoServerForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		hideMode: "offsets",
		fileUpload: true,
		plugins: [new CMDBuild.FormPlugin(), new CMDBuild.CallbackPlugin()],
		frame: true,

		initComponent: function() {
			Ext.apply(this, {
				tbar : buildTBarTools.call(this),
				items : items.call(this),
				buttonAlign : "center",
				buttons : buildButtons.call(this)
			});

			this.callParent(arguments);
			this.setFieldsDisabled();
			this.on("clientvalidation", function(formPanel, valid ) {
				this.saveButton.setDisabled(!valid);
			}, this);

			this.disableModify();
		},

		onAddLayer: function() {
			this.lastSelection = undefined;
			this.getForm().reset();
			this.enableModify(all = true);
		},

		onLayerSelect: function(layerModel) {
			this.lastSelection = layerModel;
			this.getForm().loadRecord(layerModel);
			this.disableModify(enableTBar = true);
		}

	});

	function items() {
		var name = new Ext.form.TextField({
			fieldLabel : tr_attribute.name,
			name : "name",
			allowBlank : false,
			width : FIELDSWIDTH,
			cmImmutable: true
		});

		var types = new Ext.form.ComboBox({
			width: FIELDSWIDTH,
			store : new Ext.data.SimpleStore( {
				fields : [ "value", "name" ],
				data : [ [ TYPES.geotiff, "GeoTiff" ],
						[ TYPES.worldimage, "WorldImage" ],
						[ TYPES.shpe, "Shape" ] ]
			}),
			allowBlank : false,
			autoScroll : true,
			name : "type",
			fieldLabel : tr_attribute.type,
			valueField : "value",
			displayField : "name",
			hiddenName : "type",
			queryMode : "local",
			triggerAction: "all",
			cmImmutable: true
		});

		var minZoom = new Ext.form.SliderField({
			fieldLabel : tr.min_zoom,
			plugins : [ new CMDBuild.SliderFieldPlugin() ],
			width : FIELDSWIDTH,
			minValue : 0,
			maxValue : 25,
			name : "minZoom"
		});

		var maxZoom = new Ext.form.SliderField({
			fieldLabel : tr.max_zoom,
			plugins : [ new CMDBuild.SliderFieldPlugin() ],
			width : FIELDSWIDTH,
			minValue : 0,
			maxValue : 25,
			value : 25,
			name : "maxZoom"
		});

		var range = new CMDBuild.RangeSlidersFieldSet( {
			maxSliderField: maxZoom,
			minSliderField: minZoom
		});

		var description = new Ext.form.TextField({
			xtype : "textfield",
			fieldLabel : tr_attribute.description,
			name : "description",
			allowBlank : false,
			width : FIELDSWIDTH
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

		this.getName = function() {
			return name.getValue();
		};

		return {
			xtype: "panel",
			frame: true,
			border: true,
			items: [name, description, file, types, range]
		};
	};

	function buildButtons() {
		this.cmButtons = [
			this.saveButton = new CMDBuild.buttons.SaveButton(),
			this.abortButton = new CMDBuild.buttons.AbortButton()
		];
		
		return this.cmButtons;
	};

	function buildTBarTools() {
		this.cmTBar = [
			this.deleteButton = new Ext.button.Button({
				text: tr_geoserver.delete_layer,
				iconCls: 'delete'
			}),

			this.modifyButton = new Ext.Button({
				text: tr_geoserver.modify_layer,
				iconCls: "modify",
				scope: this,
				handler: function() {
					this.enableModify();
				}
			})
		];

		return this.cmTBar;
	};
})();