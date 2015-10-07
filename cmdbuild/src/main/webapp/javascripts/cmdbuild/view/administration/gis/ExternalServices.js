(function() {

	var tr = CMDBuild.Translation.administration.modcartography.external_services;

	Ext.define('CMDBuild.view.administration.gis.ExternalServices', {
		extend: 'Ext.form.Panel',

		delegate: undefined,

		buttonAlign: 'center',
		frame: true,
		layout: 'border',
		title: tr.title,

		initComponent: function() {
			var me = this;

			this.services = ['google', 'yahoo', 'osm', 'geoserver'];

			// Buttons configuration
				this.saveButton = Ext.create('Ext.button.Button', {
					text: CMDBuild.Translation.common.buttons.save,
					disabled: false,
					scope: this,

					handler: function() {
						this.delegate.cmOn('onSaveButtonClick');
					}
				});
			// END: Buttons configuration

			this.googleMapsFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.description.google,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'google',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					anchor: '100%'
				},

				items: [
					{
						name: 'google_key',
						fieldLabel: CMDBuild.Translation.key
					},
					Ext.create('CMDBuild.form.RangeSliders', {
						minSliderField: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'google_minzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.min_zoom,
							labelWidth: CMDBuild.LABEL_WIDTH,
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
							clickToChange: false,
							animate: false
						}),
						maxSliderField: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'google_maxzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.max_zoom,
							labelWidth: CMDBuild.LABEL_WIDTH,
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
							clickToChange: false,
							animate: false
						})
					})
				],

				listeners: {
					beforeexpand: function(fieldset, eOpts) {
						me.delegate.cmOn('onFieldsetExpand', 'google');
					}
				}
			});

			this.yahooMapsFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.description.yahoo,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'yahoo',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					anchor: '100%'
				},

				items: [
					{
						name: 'yahoo_key',
						fieldLabel: CMDBuild.Translation.key
					},
					Ext.create('CMDBuild.form.RangeSliders', {
						minSliderField: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'yahoo_minzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.min_zoom,
							labelWidth: CMDBuild.LABEL_WIDTH,
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
							clickToChange: false,
							animate: false
						}),
						maxSliderField: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'yahoo_maxzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.max_zoom,
							labelWidth: CMDBuild.LABEL_WIDTH,
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
							clickToChange: false,
							animate: false
						})
					})
				],

				listeners: {
					beforeexpand: function(fieldset, eOpts) {
						me.delegate.cmOn('onFieldsetExpand', 'yahoo');
					}
				}
			});

			this.openStreetMapsFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.description.osm,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'osm',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					anchor: '100%'
				},

				items: [
					Ext.create('CMDBuild.form.RangeSliders', {
						minSliderField: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'osm_minzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.min_zoom,
							labelWidth: CMDBuild.LABEL_WIDTH,
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
							clickToChange: false,
							animate: false
						}),
						maxSliderField: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'osm_maxzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.max_zoom,
							labelWidth: CMDBuild.LABEL_WIDTH,
							width: CMDBuild.ADM_BIG_FIELD_WIDTH,
							clickToChange: false,
							animate: false
						})
					})
				],

				listeners: {
					beforeexpand: function(fieldset, eOpts) {
						me.delegate.cmOn('onFieldsetExpand', 'osm');
					}
				}
			});

			this.geoServerFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.description.geoserver,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'geoserver',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					anchor: '100%'
				},

				items: [
					{
						name: 'geoserver_url',
						fieldLabel: tr.url
					},
					{
						name: 'geoserver_workspace',
						fieldLabel: tr.workspace
					},
					{
						name: 'geoserver_admin_user',
						fieldLabel: tr.admin_user
					},
					{
						name: 'geoserver_admin_password',
						fieldLabel: tr.admin_password,
						inputType: 'password'
					},
				]
			});

			this.wrapper = Ext.create('Ext.panel.Panel', {
				region: 'center',
				autoScroll: true,
				frame: true,

				items: [
					this.googleMapsFieldset,
					this.yahooMapsFieldset,
					this.openStreetMapsFieldset,
					this.geoServerFieldset
				]
			});

			Ext.apply(this, {
				buttons: [this.saveButton],
				items: [this.wrapper]
			});

			this.callParent(arguments);
		}
	});

})();