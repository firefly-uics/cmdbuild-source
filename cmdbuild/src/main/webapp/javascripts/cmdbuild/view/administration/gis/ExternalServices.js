(function () {

	Ext.define('CMDBuild.view.administration.gis.ExternalServices', {
		extend: 'Ext.form.Panel',

		delegate: undefined,

		buttonAlign: 'center',
		frame: true,
		layout: 'border',
		title: CMDBuild.Translation.administration.modcartography.external_services.title,

		initComponent: function () {
			var me = this;

			this.services = ['google', 'yahoo', 'osm', 'geoserver'];

			// Buttons configuration
				this.saveButton = Ext.create('Ext.button.Button', {
					text: CMDBuild.Translation.save,
					disabled: false,
					scope: this,

					handler: function () {
						this.delegate.cmOn('onSaveButtonClick');
					}
				});
			// END: Buttons configuration

			this.googleMapsFieldset = Ext.create('Ext.form.FieldSet', {
				title: CMDBuild.Translation.administration.modcartography.external_services.description.google,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'google',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					anchor: '100%'
				},

				items: [
					{
						name: 'google_key',
						fieldLabel: CMDBuild.Translation.key
					},
					Ext.create('CMDBuild.view.common.field.slider.RangeContainer', {
						fieldMin: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'google_minzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.min_zoom,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							clickToChange: false,
							animate: false
						}),
						fieldMax: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'google_maxzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.max_zoom,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							clickToChange: false,
							animate: false
						})
					})
				],

				listeners: {
					beforeexpand: function (fieldset, eOpts) {
						me.delegate.cmOn('onFieldsetExpand', 'google');
					}
				}
			});

			this.yahooMapsFieldset = Ext.create('Ext.form.FieldSet', {
				title: CMDBuild.Translation.administration.modcartography.external_services.description.yahoo,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'yahoo',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					anchor: '100%'
				},

				items: [
					{
						name: 'yahoo_key',
						fieldLabel: CMDBuild.Translation.key
					},
					Ext.create('CMDBuild.view.common.field.slider.RangeContainer', {
						fieldMin: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'yahoo_minzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.min_zoom,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							clickToChange: false,
							animate: false
						}),
						fieldMax: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'yahoo_maxzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.max_zoom,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							clickToChange: false,
							animate: false
						})
					})
				],

				listeners: {
					beforeexpand: function (fieldset, eOpts) {
						me.delegate.cmOn('onFieldsetExpand', 'yahoo');
					}
				}
			});

			this.openStreetMapsFieldset = Ext.create('Ext.form.FieldSet', {
				title: CMDBuild.Translation.administration.modcartography.external_services.description.osm,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'osm',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					anchor: '100%'
				},

				items: [
					Ext.create('CMDBuild.view.common.field.slider.RangeContainer', {
						fieldMin: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'osm_minzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.min_zoom,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							clickToChange: false,
							animate: false
						}),
						fieldMax: Ext.create('Ext.slider.Single', {
							minValue: 0,
							maxValue: 18,
							value: 0,
							width: 300,
							name: 'osm_maxzoom',
							fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.max_zoom,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							clickToChange: false,
							animate: false
						})
					})
				],

				listeners: {
					beforeexpand: function (fieldset, eOpts) {
						me.delegate.cmOn('onFieldsetExpand', 'osm');
					}
				}
			});

			this.geoServerFieldset = Ext.create('Ext.form.FieldSet', {
				title: CMDBuild.Translation.administration.modcartography.external_services.description.geoserver,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',
				serviceName: 'geoserver',

				defaults: {
					xtype: 'textfield',
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					anchor: '100%'
				},

				items: [
					{
						name: 'geoserver_url',
						fieldLabel: CMDBuild.Translation.administration.modcartography.external_services.url
					},
					{
						name: 'geoserver_workspace',
						fieldLabel: CMDBuild.Translation.administration.modcartography.external_services.workspace
					},
					{
						name: 'geoserver_admin_user',
						fieldLabel: CMDBuild.Translation.administration.modcartography.external_services.admin_user
					},
					{
						name: 'geoserver_admin_password',
						fieldLabel: CMDBuild.Translation.administration.modcartography.external_services.admin_password,
						inputType: 'password'
					}
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
