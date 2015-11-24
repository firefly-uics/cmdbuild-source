(function() {

	Ext.define('CMDBuild.controller.administration.widget.Widget', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Classes',
			'CMDBuild.core.proxy.widgets.Configuration',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onClassTabWidgetAbortButtonClick',
			'onClassTabWidgetAddButtonClick',
			'onClassTabWidgetModifyButtonClick = onClassTabWidgetItemDoubleClick',
			'onClassTabWidgetPanelShow',
			'onClassTabWidgetRemoveButtonClick',
			'onClassTabWidgetRowSelected',
			'onClassTabWidgetSaveButtonClick',
			'validate = classTabWidgetValidateForm'
		],

		/**
		 * @property {Mixed}
		 */
		controllerWidgetForm: undefined,

		/**
		 * @property {Mixed}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.widget.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.common.Class}
		 *
		 * @private
		 */
		selectedClass: undefined,

		/**
		 * @property {CMDBuild.model.widget.WidgetDefinition}
		 *
		 * @private
		 */
		selectedWidget: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.widget.WidgetView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.widget.WidgetView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		/**
		 * @private
		 */
		buildForm: function() {
			if (!Ext.isEmpty(this.controllerWidgetForm)) {
				if (!Ext.isEmpty(this.form))
					this.view.remove(this.form);

				// Shorthands
				this.form = this.controllerWidgetForm.getView();
				this.view.form = this.form;

				this.view.add(this.form);
			}
		},

		/**
		 * @param {String} type
		 *
		 * @private
		 */
		buildFormController: function(type) {
			if (!Ext.isEmpty(type) && Ext.isString(type))
				switch (type) {
					case '.Calendar': {
						this.controllerWidgetForm = Ext.create('CMDBuild.controller.administration.widget.form.Calendar', { parentDelegate: this });
					} break;

					case '.CreateModifyCard': {
						this.controllerWidgetForm = Ext.create('CMDBuild.controller.administration.widget.form.CreateModifyCard', { parentDelegate: this });
					} break;

					case '.OpenReport': {
						this.controllerWidgetForm = Ext.create('CMDBuild.controller.administration.widget.form.OpenReport', { parentDelegate: this });
					} break;

					case '.Ping': {
						this.controllerWidgetForm = Ext.create('CMDBuild.controller.administration.widget.form.Ping', { parentDelegate: this });
					} break;

					default: { // TODO: use only string ones + use each()
						for (var key in CMDBuild.controller.administration.widget)
							if (CMDBuild.controller.administration.widget[key].WIDGET_NAME == type) {
								this.controllerWidgetForm = CMDBuild.controller.administration.widget[key].create({
									view: subView,
									classId: classId
								});

								break;
							}
					}
				}
		},

		/**
		 * @param {Object} options
		 * @param {Boolean} success
		 * @param {Object} response
		 *
		 * @private
		 */
		loadCallback: function(options, success, response) {
			if (!this.grid.getSelectionModel().hasSelection())
				this.grid.getSelectionModel().select(0, true);
		},

		/**
		 * Get selected class data and enable/disable tab
		 *
		 * @param {Number} classId
		 */
		onClassSelected: function(classId) {
			if (!Ext.isEmpty(classId) || !Ext.isNumeric(classId)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.core.proxy.Classes.readAll({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

						var selectedClass = Ext.Array.findBy(decodedResponse, function(classObject, i) {
							return classId == classObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedClass)) {
							this.classTabWidgetSelectedClassSet({ value: selectedClass });

							// BUSINESS RULE: currently the widgets are not inherited so, deny the definition on superClasses
							this.view.setDisabled(this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.IS_SUPER_CLASS));

							this.cmfg('onClassTabWidgetPanelShow');
						} else {
							_error('class with id "' + classId + '" not found', this);
						}
					}
				});
			} else {
				_error('onClassSelected empty or invalid class id parameter', this);
			}
		},

		onClassTabWidgetAbortButtonClick: function() {
			if (!this.classTabWidgetSelectedWidgetIsEmpty()) {
				this.cmfg('onClassTabWidgetRowSelected');
			} else if (!Ext.isEmpty(this.form)) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		/**
		 * @param {String} type
		 */
		onClassTabWidgetAddButtonClick: function(type) {
			if (!Ext.isEmpty(type) && Ext.isString(type)) {
				this.grid.getSelectionModel().deselectAll();

				this.classTabWidgetSelectedWidgetReset();

				this.buildFormController(type);
				this.buildForm();

				if (!Ext.isEmpty(this.controllerWidgetForm) && Ext.isFunction(this.controllerWidgetForm.cmfg))
					this.controllerWidgetForm.cmfg('classTabWidgetAdd');
			}
		},

		onClassTabWidgetModifyButtonClick: function() {
			if (!Ext.isEmpty(this.form))
				this.form.setDisabledModify(false);
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.loadCallback
		 *
		 * TODO: waiting for refactor (CRUD)
		 */
		onClassTabWidgetPanelShow: function(parameters) {
			parameters = Ext.Object.isEmpty(parameters) ? {} : parameters;
			parameters.loadCallback = Ext.isFunction(parameters.loadCallback) ? parameters.loadCallback : this.loadCallback;

			this.grid.getStore().removeAll();

			if (!Ext.isEmpty(this.form) && Ext.isFunction(this.form.reset))
				this.form.reset();

			if (!this.view.isDisabled()) {
				CMDBuild.core.proxy.widgets.Configuration.read({ // TODO: better way + comments when i'll get getStore() proxy working
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						var classWidgets = decodedResponse[this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME)];

						if (!Ext.isEmpty(classWidgets) && Ext.isArray(classWidgets))
							this.grid.getStore().loadData(classWidgets);
					},
					callback: parameters.loadCallback
				});
			}
		},

		onClassTabWidgetRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function(buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onClassTabWidgetRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				var selectedWidgetId = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.constants.Proxy.ID);
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = selectedWidgetId;

				CMDBuild.core.proxy.widgets.Configuration.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE] || [];

						var classWidgets = decodedResponse[this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME)];

						if (!Ext.isEmpty(classWidgets)) {
							var selectedWidget = Ext.Array.findBy(classWidgets, function(widgetObject, i) {
								return selectedWidgetId == widgetObject[CMDBuild.core.constants.Proxy.ID];
							}, this);

							this.buildFormController(selectedWidget[CMDBuild.core.constants.Proxy.TYPE]);

							this.classTabWidgetSelectedWidgetSet({ value: selectedWidget });

							if (!this.classTabWidgetSelectedWidgetIsEmpty()) {
								this.buildForm();

								if (!Ext.isEmpty(this.controllerWidgetForm))
									this.controllerWidgetForm.cmfg('classTabWidgetLoadRecord', this.classTabWidgetSelectedWidgetGet());

								this.form.setDisabledModify(true, true);
							}
						}
					}
				});
			}
		},

		onClassTabWidgetSaveButtonClick: function() {
			if (this.controllerWidgetForm.cmfg('classTabWidgetValidateForm', this.form)) {
				var widgetDefinition = this.controllerWidgetForm.cmfg('classTabWidgetDefinitionGet');

				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.WIDGET] = Ext.encode(widgetDefinition);

				if (Ext.isEmpty(widgetDefinition[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.core.proxy.widgets.Configuration.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.widgets.Configuration.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @private
		 */
		removeItem: function() {
			if (!this.classTabWidgetSelectedWidgetIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.WIDGET_ID] = this.classTabWidgetSelectedWidgetGet(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.widgets.Configuration.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						this.cmfg('onClassTabWidgetPanelShow');
					}
				});
			}
		},

		// SelectedClass property functions
		// TODO: this functionality should be inside main class controller
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			classTabWidgetSelectedClassGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Object} parameters
			 */
			classTabWidgetSelectedClassSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.Class';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';

					this.propertyManageSet(parameters);
				}
			},

		// SelectedWidget property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			classTabWidgetSelectedWidgetGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWidget';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			classTabWidgetSelectedWidgetIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWidget';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			classTabWidgetSelectedWidgetReset: function() {
				this.propertyManageReset('selectedWidget');
			},

			/**
			 * @param {Object} parameters
			 */
			classTabWidgetSelectedWidgetSet: function(parameters) {
				if (
					!Ext.Object.isEmpty(parameters)
					&& !Ext.isEmpty(this.controllerWidgetForm)
				) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = this.controllerWidgetForm.cmfg('classTabWidgetDefinitionModelNameGet');
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWidget';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 */
		success: function(response, options, decodedResponse) {
			decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

			CMDBuild.view.common.field.translatable.Utils.commit(this.view.form);

			this.cmfg('onClassTabWidgetPanelShow', {
				loadCallback: function(options, success, response) {
					this.grid.getSelectionModel().select(
						this.grid.getStore().find(CMDBuild.core.constants.Proxy.ID, decodedResponse[CMDBuild.core.constants.Proxy.ID]),
						true
					);

					this.form.setDisabledModify(true);
				}
			});
		}
	});

})();