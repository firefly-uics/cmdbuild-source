(function() {

	Ext.define('CMDBuild.controller.administration.widget.CMWidgetDefinitionController', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.widget.WidgetDefinition'
		],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @property {Int}
		 */
		classId: undefined,

		/**
		 * @property {CMDBuild.model.widget.WidgetDefinition}
		 */
		model: undefined,

		/**
		 * @property {Mixed}
		 */
		view: undefined,

		/**
		 * @param {Mixed} view
		 */
		constructor: function(view) {
			this.view = view;

			this.mon(this.view, 'cm-abort', this.onAbortClick, this);
			this.mon(this.view, 'cm-add', this.onAddClick, this);
			this.mon(this.view, 'cm-enable-modify', this.onEnableModify, this);
			this.mon(this.view, 'cm-modify', this.onModifyClick, this);
			this.mon(this.view, 'cm-remove', this.onRemoveClick, this);
			this.mon(this.view, 'cm-save', this.onSaveClick, this);
			this.mon(this.view, 'deselect', this.onWidgetDefinitionDeselect, this);
			this.mon(this.view, 'select', this.onWidgetDefinitionSelect, this);
		},

		/**
		 * @param {CMDBuild.model.widget.WidgetDefinition} widgetDef
		 */
		addRecordToGrid: function(widgetDef) {
			this.view.addRecordToGrid(Ext.create('CMDBuild.model.widget.WidgetDefinition', widgetDef));
		},

		/**
		 * @param {String} widgetName
		 * @param {CMDBuild.model.widget.WidgetDefinition} record
		 * @param {Int} classId
		 */
		buildSubController: function(widgetName, record, classId) {
			if (!Ext.Object.isEmpty(this.subController))
				delete this.subController;

			var subControllerClass = findController(widgetName);

			if (subControllerClass) {
				var subView = this.view.buildWidgetForm(widgetName);

				if (Ext.isString(subControllerClass)) {
					this.subController = Ext.create(subControllerClass, {
						view: subView,
						classId: classId
					});
				} else {
					this.subController = subControllerClass.create({
						view: subView,
						classId: classId
					});
				}

				if (!Ext.Object.isEmpty(record))
					this.subController.fillFormWithModel(record);

				this.view.disableModify(true);
			} else {
				this.view.reset();
			}

			function findController(widgetName) {
				var controller = null;

				switch (widgetName) {
					case '.OpenReport': {
						controller = 'CMDBuild.controller.administration.widget.OpenReport';
					} break;

					default: {
						for (var key in CMDBuild.controller.administration.widget)
							if (CMDBuild.controller.administration.widget[key].WIDGET_NAME == widgetName) {
								controller = CMDBuild.controller.administration.widget[key];

								break;
							}
					}
				}

				return controller;
			}
		},

		onAbortClick: function() {
			if (this.model) {
				this.view.disableModify(true);
				this.subController.fillFormWithModel(this.model);
			} else {
				this.view.reset();
			}
		},

		onAddClassButtonClick: function() {
			this.view.disable();
		},

		/**
		 * @param {String} widgetName
		 */
		onAddClick: function(widgetName) {
			this.model = undefined;
			this.view.reset();

			this.buildSubController(widgetName, null, this.classId);

			if (!Ext.Object.isEmpty(this.subController)) {
				this.view.enableModify();
				this.subController.setDefaultValues();
			}
		},

		/**
		 * @param {Int} classId
		 */
		onClassSelected: function(classId) {
			var et = _CMCache.getEntryTypeById(classId);

			this.classId = classId;
			this.view.reset(true);


			// BUSINESS RULE: currently the widgets are not inherited so, deny the definition on superclasses
			if (et.get('superclass')) {
				this.view.disable();
			} else {
				this.view.enable();

				var widgets = et.getWidgets();

				for (var i = 0; i < widgets.length; ++i)
					this.addRecordToGrid(widgets[i]);
			}
		},

		onEnableModify: function() {
			if (this.subController)
				this.subController.afterEnableEditing()
		},

		onModifyClick: function() {
			this.view.enableModify();
		},

		onRemoveClick: function() {
			var me = this;

			Ext.Msg.show({
				title: CMDBuild.Translation.common.buttons.remove,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes') {
						if (me.model && me.subController) {
							var id = me.model.get(CMDBuild.core.constants.Proxy.ID);
							var params = {};

							params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.classId);
							params[CMDBuild.core.constants.Proxy.WIDGET_ID] = id;

							CMDBuild.ServiceProxy.CMWidgetConfiguration.remove({
								params: params,
								success: function() {
									me.view.removeRecordFromGrid(id);
									me.view.reset();

									_CMCache.onWidgetDeleted(me.classId, id);

									delete me.model;
									delete me.subController;
								}
							});
						}
					}
				}
			});
		},

		onSaveClick: function() {
			var me = this;
			var widgetDef = this.view.getWidgetDefinition();
			var invalidFieldsArray = this.view.form.getNonValidFields();

			// Check for invalid fields and subController
			if (this.subController && invalidFieldsArray.length == 0) {
				if (this.model) {
					widgetDef[CMDBuild.core.constants.Proxy.ID] = this.model.get(CMDBuild.core.constants.Proxy.ID);
				}

				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.classId);
				params[CMDBuild.core.constants.Proxy.WIDGET] = Ext.encode(widgetDef);

				CMDBuild.ServiceProxy.CMWidgetConfiguration.save({
					params: params,
					success: function(result, options, decodedResult) {
						var widgetModel = Ext.create('CMDBuild.model.widget.WidgetDefinition', Ext.apply(decodedResult.response, {
							type: widgetDef.type
						}));

						_CMCache.onWidgetSaved(me.classId, widgetDef);

						me.view.addRecordToGrid(widgetModel, true);
						me.view.disableModify(true);

						CMDBuild.view.common.field.translatable.Utils.commit(me.view.form);
					}
				});
			}
		},

		/**
		 * @param {CMDBuild.view.administration.widget.CMWidgetDefinitionGrid} grid
		 * @param {CMDBuild.model.widget.WidgetDefinition} record
		 * @param {Int} index
		 * @param {Object} eOpts
		 */
		onWidgetDefinitionDeselect: function(grid, record, index, eOpts) {
			this.model = undefined;

			delete this.subController;
		},

		/**
		 * @param {CMDBuild.view.administration.widget.CMWidgetDefinitionGrid} grid
		 * @param {CMDBuild.model.widget.WidgetDefinition} record
		 * @param {Int} index
		 * @param {Object} eOpts
		 */
		onWidgetDefinitionSelect: function(grid, record, index, eOpts) {
			this.model = record;

			this.buildSubController(record.get(CMDBuild.core.constants.Proxy.TYPE), record, this.classId);
		}
	});

})();