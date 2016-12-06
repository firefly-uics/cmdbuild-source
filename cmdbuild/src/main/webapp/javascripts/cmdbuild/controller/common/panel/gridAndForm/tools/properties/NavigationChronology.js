(function () {

	Ext.define('CMDBuild.controller.common.panel.gridAndForm.tools.properties.NavigationChronology', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.configurations.DataFormat',
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.tools.properties.Properties}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'panelGridAndFormtoolsPropertiesNavigationConfigObjectGet'
		],

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		getIconClass: function (record) {
			switch (record.get(CMDBuild.core.constants.Proxy.MODULE_ID)) {
				case 'class': {
					var isSuperClass = record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.OBJECT, 'superclass']);

					return isSuperClass ? 'cmdb-tree-superclass-icon' : 'cmdb-tree-class-icon';
				}

				case CMDBuild.core.constants.ModuleIdentifiers.getCustomPage():
					return 'cmdb-tree-custompage-icon';

				case 'dashboard':
					return 'cmdb-tree-dashboard-icon';

				case CMDBuild.core.constants.ModuleIdentifiers.getWorkflow(): {
					var isSuperClass = record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.OBJECT, 'superclass']);

					return isSuperClass ? 'cmdb-tree-superprocessclass-icon' : 'cmdb-tree-processclass-icon';
				}

				default:
					return 'x-tree-icon-leaf';
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		getItemsConfigArray: function () {
			var menuItems = [];

			if (CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyIsEmpty')) {
				menuItems.push({
					text: '- ' + CMDBuild.Translation.empty + ' -',
					disabled: true
				});
			} else {
				var chronologyItems = CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyGet');

				CMDBuild.core.Utils.objectArraySort(chronologyItems, CMDBuild.core.constants.Proxy.DATE, 'DESC');

				Ext.Array.forEach(chronologyItems, function (itemObject, i, allItemObjects) {
					if (Ext.isObject(itemObject) && !Ext.Object.isEmpty(itemObject))
						menuItems.push({
							iconCls: this.getIconClass(itemObject),
							record: itemObject,
							text: this.labelBuild(itemObject),

							handler: function (button, e) {
								CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyButtonHandler', button.record);
							}
						});

					if (i == 0)
						menuItems.push('-');
				}, this);
			}

			return menuItems;
		},

		// Label methods
			/**
			 * @param {CMDBuild.model.navigation.chronology.Record} record
			 *
			 * @returns {String}
			 *
			 * @private
			 */
			labelBuild: function (record) {
				if (!Ext.isEmpty(record) && Ext.isObject(record)) {
					var itemLabelParts = [];
					itemLabelParts.push(Ext.Date.format(record.get(CMDBuild.core.constants.Proxy.DATE), CMDBuild.core.configurations.DataFormat.getTime()));

					this.labelModuleIdGet(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.MODULE_ID));
					this.labelPropertyGet(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE));
					this.labelPropertyGet(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.ITEM));
					this.labelPropertyGet(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.SECTION));
					this.labelPropertyGet(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.SUB_SECTION));

					return itemLabelParts.join(CMDBuild.core.constants.Global.getTitleSeparator());
				}

				return '';
			},

			/**
			 * @param {Array} targetArray
			 * @param {String} property
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			labelModuleIdGet: function (targetArray, property) {
				if (Ext.isArray(targetArray))
					switch (property) {
						case 'class':
							return targetArray.push(CMDBuild.Translation.classes);

						case CMDBuild.core.constants.ModuleIdentifiers.getCustomPage():
							return targetArray.push(CMDBuild.Translation.customPages);

						case 'dashboard':
							return targetArray.push(CMDBuild.Translation.dashboard);

						case CMDBuild.core.constants.ModuleIdentifiers.getDataView():
							return targetArray.push(CMDBuild.Translation.views);

						case CMDBuild.core.constants.ModuleIdentifiers.getReport():
							return targetArray.push(CMDBuild.Translation.report);

						case CMDBuild.core.constants.ModuleIdentifiers.getWorkflow():
							return targetArray.push(CMDBuild.Translation.processes);
					}
			},

			/**
			 * @param {Array} targetArray
			 * @param {String} property
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			labelPropertyGet: function (targetArray, property) {
				if (
					Ext.isArray(targetArray)
					&& !Ext.isEmpty(property) && Ext.isObject(property)
				) {
					if (!property.isEmpty(CMDBuild.core.constants.Proxy.DESCRIPTION)) {
						targetArray.push(property.get(CMDBuild.core.constants.Proxy.DESCRIPTION));
					} else if (!property.isEmpty(CMDBuild.core.constants.Proxy.ID)) {
						targetArray.push(property.get(CMDBuild.core.constants.Proxy.ID));
					}
				}
			},

		/**
		 * @returns {Void}
		 */
		panelGridAndFormtoolsPropertiesNavigationConfigObjectGet: function () {
			return {
				iconCls: 'navigation-chronology',
				text: CMDBuild.Translation.navigationChronology,

				menu: {
					xtype: 'menu',
					overflowX: 'auto',
					maxWidth: CMDBuild.core.constants.FieldWidths.MENU_DROPDOWN,

					items: this.getItemsConfigArray()
				}
			};
		}
	});

})();
