(function() {

	Ext.define("CMDBuild.view.management.dashboard.CMChartPortletForm", {
		extend: "Ext.form.Panel",

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		initComponent: function() {
			this.callParent(arguments);
			this.configureForChartParameters();
		},

		configureForChartParameters: function() {
			var params = this.chartConfiguration.getDataSourceInputConfiguration();
			for (var i=0, l=params.length, field; i<l; ++i) {
				field = getFormFieldForParameter(params[i], this);
				this.add(field);
			}
		},

		/*
		 * Used from the controller of the portlet
		 * to syncronize the store load with the request of the data
		 * for the chart. We want to load only the remove stores that
		 * are the ones with a url setted on the proxy
		 * */
		checkStoreLoad: function(cb) {
			var requestBarrier = new CMDBuild.Utils.CMRequestBarrier(cb);
			var someStore = false;

			this.cascade(function(item) {
				if (item
						&& item.store
						&& item.store.proxy
						&& item.store.proxy.url) {

					someStore = true;
					item.store.load({callback: requestBarrier.getCallback()});
				}
			});

			// call the callback directly if there is no store to load
			if (!someStore) {
				cb();
			} else {
				requestBarrier.start();
			}
		}
	});

	/**
	 * @param {Object} parameterConfiguration
	 * @param {CMDBuild.view.management.dashboard.CMChartPortletForm} form
	 */
	function getFormFieldForParameter(parameterConfiguration, form) {
		var builders = {
			STRING: function(parameterConfiguration) {
				var types = {
					classes: function(parameterConfiguration) {
						var f = new CMDBuild.field.ErasableCombo({
							plugins: [new CMDBuild.SetValueOnLoadPlugin()],
							name: parameterConfiguration.name,
							fieldLabel : parameterConfiguration.name,
							labelWidth: CMDBuild.LABEL_WIDTH,
							labelAlign: "right",
							valueField : 'name',
							displayField : 'description',
							editable: false,
							store : _CMCache.getClassesAndProcessesStore(),
							queryMode: 'local',
							allowBlank: !parameterConfiguration.required
						});

						f.setValue(parameterConfiguration.defaultValue);
						return f;
					},

					user: function(parameterConfiguration) {
						return new Ext.form.field.Hidden({
							name: parameterConfiguration.name,
							value: CMDBuild.Runtime.Username
						});
					},

					group: function(parameterConfiguration) {
						return new Ext.form.field.Hidden({
							name: parameterConfiguration.name,
							value: CMDBuild.Runtime.DefaultGroupName
						});
					}
				};

				if (typeof types[parameterConfiguration.fieldType] == "function") {
					return types[parameterConfiguration.fieldType](parameterConfiguration);
				} else {
					return builders["DEFAULT"](parameterConfiguration);
				}
			},

			INTEGER: function(parameterConfiguration) {
				var defaultValue = parseInt(parameterConfiguration.defaultValue) || null;
				var types = {
					classes: function(parameterConfiguration) {
						var f = new CMDBuild.field.ErasableCombo({
							plugins: [new CMDBuild.SetValueOnLoadPlugin()],
							name: parameterConfiguration.name,
							fieldLabel : parameterConfiguration.name,
							labelWidth: CMDBuild.LABEL_WIDTH,
							labelAlign: "right",
							valueField : 'id',
							displayField : 'description',
							editable: false,
							store : _CMCache.getClassesAndProcessesStore(),
							queryMode: 'local',
							allowBlank: !parameterConfiguration.required
						});

						f.setValue(parameterConfiguration.defaultValue); 	// use the string for the known problem with the CMTableModel
																			// that has a string for the Id
						return f;
					},

					lookup: function(parameterConfiguration) {
						var ltype = parameterConfiguration.lookupType;
						var f;
						if (typeof ltype != "string") {
							f = builders["DEFAULT"](parameterConfiguration);
						} else {
							var conf = {
								description: parameterConfiguration.name,
								name: parameterConfiguration.name,
								isnotnull: parameterConfiguration.required,
								fieldmode: "write",
								type: "LOOKUP",
								lookup: ltype,
								lookupchain: _CMCache.getLookupchainForType(ltype)
							};

							f = CMDBuild.Management.FieldManager.getFieldForAttr(conf, readonly=false, skipSubField=true);
						}

						f.setValue(defaultValue);
						return f;
					},

					user: function(parameterConfiguration) {
						return new Ext.form.field.Hidden({
							name: parameterConfiguration.name,
							value: CMDBuild.Runtime.UserId
						});
					},

					group: function(parameterConfiguration) {
						return new Ext.form.field.Hidden({
							name: parameterConfiguration.name,
							value: CMDBuild.Runtime.DefaultGroupId
						});
					},

					/**
					 * @param {Object} parameterConfiguration
					 *
					 * @returns {CMDBuild.Management.ReferenceField.Field} field
					 */
					card: function(parameterConfiguration) {
						var required = parameterConfiguration[CMDBuild.core.proxy.CMProxyConstants.REQUIRED];
						var filter = parameterConfiguration[CMDBuild.core.proxy.CMProxyConstants.FILTER];
						var field = CMDBuild.Management.ReferenceField.build({
							description: (required ? '* ' : '' ) + parameterConfiguration[CMDBuild.core.proxy.CMProxyConstants.NAME],
							filter: !Ext.isEmpty(filter) ? filter[CMDBuild.core.proxy.CMProxyConstants.EXPRESSION] : null,
							isnotnull: required,
							name: parameterConfiguration[CMDBuild.core.proxy.CMProxyConstants.NAME],
							referencedIdClass: parameterConfiguration.classToUseForReferenceWidget
						});

						field.setValue(defaultValue);

						return field;
					}
				};

				if (typeof types[parameterConfiguration.fieldType] == "function") {
					return types[parameterConfiguration.fieldType](parameterConfiguration);
				} else {
					return builders["DEFAULT"](parameterConfiguration);
				}
			},

			DEFAULT: function(parameterConfiguration) {
				var conf = {
					name: parameterConfiguration.name,
					type: parameterConfiguration.type,
					description: parameterConfiguration.name,
					isnotnull: parameterConfiguration.required
				};

				var field = CMDBuild.Management.FieldManager.getFieldForAttr(conf, // TODO: implementation of filter
					readonly=false, skipSubField=true);

				if (field) {
					field.setValue(parameterConfiguration.defaultValue);
					return field;
				}
			}
		};

		if (typeof builders[parameterConfiguration.type] == "function") {
			return builders[parameterConfiguration.type](parameterConfiguration);
		} else {
			return builders["DEFAULT"](parameterConfiguration);
		}
	}

})();