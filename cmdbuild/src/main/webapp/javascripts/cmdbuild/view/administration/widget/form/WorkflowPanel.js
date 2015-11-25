(function() {

	Ext.define('CMDBuild.view.administration.widget.form.WorkflowPanel', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widgets.Workflow',
			'CMDBuild.model.widget.workflow.PresetGrid'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.form.Workflow}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.TextArea}
		 */
		filter: undefined,

		/**
		 * @property {CMDBuild.view.common.field.grid.KeyValue}
		 */
		presetGrid: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		workflow: undefined,

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormBasePropertiesGet: function() {
			return Ext.Array.push(this.callParent(arguments), [
				Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.core.constants.Proxy.FILTER_TYPE,
					fieldLabel: CMDBuild.Translation.selection,
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					valueField: CMDBuild.core.constants.Proxy.NAME,
					displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
					forceSelection: true,
					editable: false,

					store: CMDBuild.core.proxy.widgets.Workflow.getStoreSelectionType(),
					queryMode: 'local',

					listeners: {
						scope: this,
						change: function(combo, newValue, oldValue, eOpts) {
							this.delegate.cmfg('onClassTabWidgetWorkflowFilterTypeChange', newValue);
						}
					}
				})
			]);
		},

		/**
		 * @returns {Array}
		 */
		widgetDefinitionFormAdditionalPropertiesByCqlGet: function() {
			return [
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.additionalProperties,
					flex: 1,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						this.filter = Ext.create('Ext.form.field.TextArea', {
							name: CMDBuild.core.constants.Proxy.FILTER,
							fieldLabel: CMDBuild.Translation.cqlFilter,
							labelWidth: CMDBuild.LABEL_WIDTH,
							maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH
						})
					]
				})
			];
		},

		/**
		 * @returns {Array}
		 */
		widgetDefinitionFormAdditionalPropertiesByNameGet: function() {
			return [
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.additionalProperties,
					flex: 1,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						this.workflow = Ext.create('Ext.form.field.ComboBox', {
							name: CMDBuild.core.constants.Proxy.WORKFLOW_NAME,
							fieldLabel: CMDBuild.Translation.workflow,
							labelWidth: CMDBuild.LABEL_WIDTH,
							maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
							valueField: CMDBuild.core.constants.Proxy.NAME,
							displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename description)
							forceSelection: true,
							editable: false,

							store: CMDBuild.core.proxy.widgets.Workflow.getStoreTargetWorkflow(),
							queryMode: 'local',

							listeners: {
								scope: this,
								change: function(combo, newValue, oldValue, eOpts) {
									this.delegate.cmfg('onClassTabWidgetWorkflowSelectedWorkflowChange', combo.findRecordByValue(newValue));
								}
							}
						}),
						this.presetGrid = Ext.create('CMDBuild.view.common.field.grid.KeyValue', {
							enableCellEditing: true,
							keyAttributeName: CMDBuild.core.constants.Proxy.NAME,
							keyLabel: CMDBuild.Translation.attribute,
							margin: '8 0 9 0',
							modelName: 'CMDBuild.model.widget.workflow.PresetGrid',
							title: CMDBuild.Translation.workflowAttributes
						})
					]
				})
			];
		}
	});

})();