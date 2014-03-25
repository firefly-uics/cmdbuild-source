(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', {

		comboField: undefined,
		gridField: undefined,

		/**
		 * Workflow attribute store builder for onWorkflowSelected event
		 */
		buildWorkflowAttributesStore: function(attributes) {
			if (attributes) {
				var data = [];

				for (var key in attributes) {
					data.push({ value: key });
				}

				return Ext.create('Ext.data.Store', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE],
					data: data,
					autoLoad: true
				});
			}
		},

		cleanServerAttributes: function(attributes) {
			var out = {};

			for (var i = 0, l = attributes.length; i < l; ++i) {
				var attr = attributes[i];

				out[attr.name] = '';
			}

			return out;
		},

		onAttributeComboSelect: function(rowIndex) {
			this.gridField.cellEditing.startEditByPosition({ row: rowIndex, column: 1 });
		},

		/**
		 * @param (String) name
		 * @param (Boolean) modify
		 */
		onWorkflowSelected: function(name, modify) {
			var me = this;

			if (typeof modify === 'undefined')
				modify = false;

			CMDBuild.core.proxy.CMProxyTasks.getWorkflowAttributes({
				params: {
					className: name
				},
				success: function(response) {
					var decodedResponse = Ext.JSON.decode(response.responseText);

					me.gridField.keyEditorConfig.store = me.buildWorkflowAttributesStore(me.cleanServerAttributes(decodedResponse.attributes));
					if (!modify) {
						me.gridField.store.removeAll();
						me.gridField.store.insert(0, { key: '', value: '' });
						me.gridField.cellEditing.startEditByPosition({ row: 0, column: 0 });
						me.setDisabledAttributesTable(false);
					}
				}
			});
		},

		setDisabledAttributesTable: function(state) {
			this.gridField.setDisabled(state);
		}
	});

})();