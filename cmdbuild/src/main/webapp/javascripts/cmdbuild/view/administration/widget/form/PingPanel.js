(function() {

	Ext.define('CMDBuild.view.administration.widget.form.PingPanel', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.widget.ping.PresetGrid'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.form.OpenReport}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.grid.KeyValue}
		 */
		presetGrid: undefined,

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormBasePropertiesGet: function() {
			return Ext.Array.push(this.callParent(arguments), [
				Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.ADDRESS,
					fieldLabel: CMDBuild.Translation.addressToPing,
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
				}),
				Ext.create('Ext.form.field.Number', {
					name: CMDBuild.core.constants.Proxy.COUNT,
					fieldLabel: CMDBuild.Translation.numberOfPings,
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_SMALL_FIELD_WIDTH,
					maxValue: 10,
					minValue: 1
				})
			]);
		},

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormCustomPropertiesGet: function() {
			return [
				this.presetGrid = Ext.create('CMDBuild.view.common.field.grid.KeyValue', {
					enableRowAdd: true,
					enableRowDelete: true,
					enableCellEditing: true,
					keyAttributeName: CMDBuild.core.constants.Proxy.NAME,
					keyLabel: CMDBuild.Translation.attribute,
					margin: '8 0 9 0',
					modelName: 'CMDBuild.model.widget.ping.PresetGrid',
					title: CMDBuild.Translation.templates
				})
			];
		}
	});

})();