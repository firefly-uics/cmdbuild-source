(function() {

	Ext.define('CMDBuild.controller.navigation.Chronology', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.configurations.DataFormat',
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		records: [],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'navigationChronologyButtonGet',
			'navigationChronologyButtonHandler',
			'navigationChronologyRecordSave',
			'onNavigationChronologyButtonShowMenu'
		],

		/**
		 * @property {Ext.button.Split}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Ext.button.Split} configurationObject.view
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.navigation.chronology.Button', { delegate: this });
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		getIconClass: function(record) {
			switch (record.get(CMDBuild.core.constants.Proxy.MODULE_ID)) {
				case 'class':
				default:
					return 'cmdbuild-tree-class-icon';
			}
		},

		// Label methods
			/**
			 * @param {CMDBuild.model.navigation.chronology.Record} record
			 *
			 * @returns {String}
			 *
			 * @private
			 */
			getLabel: function(record) {
				if (!Ext.isEmpty(record) && Ext.isObject(record)) {
					var itemLabelParts = [];
					itemLabelParts.push(Ext.Date.format(record.get(CMDBuild.core.constants.Proxy.DATE), CMDBuild.core.configurations.DataFormat.getTime()));

					this.getLabelPropertyModuleId(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.MODULE_ID));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.ITEM));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.SECTION));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.SUB_SECTION));

					return itemLabelParts.join(CMDBuild.core.constants.Global.getTitleSeparator());
				}

				return '';
			},

			/**
			 * @param {Array} targetArray
			 * @param {String} property
			 *
			 * @private
			 */
			getLabelProperty: function(targetArray, property) {
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
			 * @param {Array} targetArray
			 * @param {String} property
			 *
			 * @private
			 */
			getLabelPropertyModuleId: function(targetArray, property) {
				if (Ext.isArray(targetArray))
					switch (property) {
						case 'class': {
							return targetArray.push(CMDBuild.Translation.classList);
						} break;
					}
			},

		/**
		 * @returns {CMDBuild.view.navigation.chronology.Button}
		 */
		navigationChronologyButtonGet: function() {
			return this.view;
		},

		// ButtonHandler methods
			/**
			 * @param {CMDBuild.model.navigation.chronology.Record} record
			 */
			navigationChronologyButtonHandler: function(record) {
				var accordion = _CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID));

				if (!Ext.isEmpty(accordion)) {
					switch (record.get(CMDBuild.core.constants.Proxy.MODULE_ID)) {
						case 'class': {
							this.navigationChronologyButtonHandlerClass(record);
						} break;

						// TODO: ...
					}

					accordion.expand();
					accordion.selectNodeById(record.accordionItem);
				}
			},

			/**
			 * @param {CMDBuild.model.navigation.chronology.Record} record
			 */
			navigationChronologyButtonHandlerClass: function(record) {
				if (!Ext.isEmpty(record) && !record.isEmpty()) {
					if (
						!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
						&& !record.isEmpty([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID])
					) {
						_CMMainViewportController.openCard({
							Id: record.get([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID]),
							IdClass: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
							activateFirstTab: record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
								? true : record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
						});

						if (!record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
							_CMMainViewportController.panelControllers['class'].gridController.view.getStore().on('load', function() {
								_CMMainViewportController.viewport.findModuleByCMName('class').cardTabPanel.activeTabSet(
									record.get([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
								);
							}, this, { single: true });

						if (!record.isEmpty([CMDBuild.core.constants.Proxy.SUB_SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
							_CMMainViewportController.panelControllers['class'].gridController.view.getStore().on('load', function() {
								_CMMainViewportController.panelControllers['class'].mdController.activeTabSet(
									record.get([CMDBuild.core.constants.Proxy.SUB_SECTION, CMDBuild.core.constants.Proxy.OBJECT])
								);
							}, this, { single: true });
					} else if (!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])) {
						_CMMainViewportController.findAccordionByCMName('class').selectNodeById(
							record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
						);
					}
				}
			},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.moduleId - cmName (class, workflow, dashboard, dataview, ...)
		 * @param {String} parameters.entryType - selected entryType (Class, process, ...)
		 * @param {Object} parameters.item - item selected from grid (card, instance, ...)
		 * @param {Object} parameters.section - usually form tab object
		 * @param {Object} parameters.subSection - usually form tab sub-section
		 */
		navigationChronologyRecordSave: function(parameters) {
			if (
				!Ext.isEmpty(parameters) && Ext.isObject(parameters)
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.MODULE_ID])
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.ENTRY_TYPE])
			) {
				var record = Ext.create('CMDBuild.model.navigation.chronology.Record', parameters);

				// Filter double records save
				if (Ext.isEmpty(this.records) || !this.records[0].equals(record))
					this.records.unshift(record);

				// Resize array to referenceComboStoreLimit configuration parameter
				this.records = Ext.Array.slice(this.records, 0, CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT));
			} else {
				_warning('invalid record field configuration', this, parameters);
			}
		},

		onNavigationChronologyButtonShowMenu: function() {
			var menuItems = [];

			if (!Ext.isEmpty(this.records) && Ext.isArray(this.records)) {
				CMDBuild.core.Utils.objectArraySort(this.records, CMDBuild.core.constants.Proxy.DATE, 'DESC');

				Ext.Array.forEach(this.records, function(recordObject, i, allRecordObjects) {
					if (!Ext.Object.isEmpty(recordObject))
						menuItems.push({
							delegate: this,

							iconCls: this.getIconClass(recordObject),
							record: recordObject,
							text: this.getLabel(recordObject),

							handler: function(button, e) {
								this.delegate.cmfg('navigationChronologyButtonHandler', this.record);
							}
						});

					if (i == 0)
						menuItems.push('-');
				}, this);
			} else {
				menuItems.push({
					text: '- ' + CMDBuild.Translation.empty + ' -',
					disabled: true
				});
			}

			Ext.apply(this.view, {
				menu: Ext.create('Ext.menu.Menu', {
					overflowX: 'auto',
					maxWidth: CMDBuild.MENU_WIDTH,

					items: menuItems
				})
			});

			this.view.showMenu();
		}
	});

})();