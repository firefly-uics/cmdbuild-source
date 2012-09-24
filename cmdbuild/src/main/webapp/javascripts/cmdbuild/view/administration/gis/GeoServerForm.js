(function() {
	var tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties,
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
				frame: false,
				border: false,
				cls: "x-panel-body-default-framed cmbordertop",
				bodyCls: 'cmgraypanel',
				tbar : buildTBarTools.call(this),
				items : items.call(this),
				layout: "border",
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
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : "name",
			allowBlank : false,
			cmImmutable: true
		});

		var types = new Ext.form.ComboBox({
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
			labelWidth: CMDBuild.LABEL_WIDTH,
			valueField : "value",
			displayField : "name",
			hiddenName : "type",
			queryMode : "local",
			triggerAction: "all",
			cmImmutable: true
		});

		var minZoom = new Ext.form.SliderField({
			fieldLabel : tr.min_zoom,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			minValue : 0,
			maxValue : 25,
			name : "minZoom"
		});

		var maxZoom = new Ext.form.SliderField({
			fieldLabel : tr.max_zoom,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
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
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : "description",
			allowBlank : false
		});

		var file = new Ext.form.TextField({
			inputType : "file",
			fieldLabel: tr_geoserver.file,
			labelWidth: CMDBuild.LABEL_WIDTH,
			name: "file",
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
			region: "center",
			autoScroll: true,
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
			this.modifyButton = new Ext.Button({
				text: tr_geoserver.modify_layer,
				iconCls: "modify",
				scope: this,
				handler: function() {
					this.enableModify();
				}
			}),
			this.deleteButton = new Ext.button.Button({
				text: tr_geoserver.delete_layer,
				iconCls: 'delete'
			})
		];

		return this.cmTBar;
	};
})();