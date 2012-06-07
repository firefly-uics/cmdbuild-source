(function() {

	Ext.define("CMDBuild.view.management.dashboard.CMChartPortletForm", {
		extend: "Ext.form.Panel",

		initComponent: function() {
			this.callParent(arguments);
			this.configureForChartParameters();
		},

		configureForChartParameters: function() {
			var params = this.chartConfiguration.getDataSourceInputConfiguration();
			for (var i=0, l=params.length, field; i<l; ++i) {
				field = getFormFieldForParameter(params[i]);
				this.add(field);
			}
		}
	});

	function getFormFieldForParameter(parameterConfiguration) {
		var builders = {
			STRING: function(parameterConfiguration) {
				var types = {
					classes: function(parameterConfiguration) {
						return new CMDBuild.field.ErasableCombo({
							name: parameterConfiguration.name,
							value: parameterConfiguration.defaultValue,
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
				var types = {
					classes: function(parameterConfiguration) {
						return new CMDBuild.field.ErasableCombo({
							fieldLabel : parameterConfiguration.name,
							labelAlign: "right",
							labelWidth: CMDBuild.LABEL_WIDTH,
							valueField : 'id',
							displayField : 'description',
							editable: false,
							store : _CMCache.getClassesAndProcessesStore(),
							queryMode: 'local',
							allowBlank: !parameterConfiguration.required,
							value: parameterConfiguration.defaultValue
						});
					},

					lookup: function(parameterConfiguration) {
						var ltype = parameterConfiguration.lookupType;

						if (typeof ltype != "string") {
							return builders["DEFAULT"](parameterConfiguration);
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

							return CMDBuild.Management.FieldManager.getFieldForAttr(conf, readonly=false, skipSubField=true);
						}
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

					card: function(parameterConfiguration) {
						var required = parameterConfiguration.required;

						var field =  CMDBuild.Management.ReferenceField.build({
							name: parameterConfiguration.name,
							description: (required ? "* " : "" ) + parameterConfiguration.name,
							referencedIdClass: parameterConfiguration.classToUseForReferenceWidget,
							isnotnull: required
						});

						if (field) {
							field.setValue(parameterConfiguration.defaultValue);
							return field;
						}
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

				var field = CMDBuild.Management.FieldManager.getFieldForAttr(conf,
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