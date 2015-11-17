(function() {

	var FILTER_FIELD = "_SystemFieldFilter";
	var CHANGE_EVENT = "change";

	Ext.define("CMDBuild.Management.ReferenceField", {
		statics: {
			/**
			 * @param {Object} attribute
			 * @param {Object} subFields
			 * @param {Object} extraFieldConf
			 *
			 * @return {Mixed} field
			 */
			build: function(attribute, subFields, extraFieldConf) {
				var templateResolver;
				var extraFieldConf = extraFieldConf || {};

				if (attribute.filter) { // is using a template
					var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, "system.template.");
					xaVars[FILTER_FIELD] = attribute.filter;
					templateResolver = new CMDBuild.Management.TemplateResolver({
						getBasicForm: function() {
							if (!Ext.isEmpty(getFormPanel(field)))
								return getFormPanel(field).getForm();

							return undefined;
						},
						xaVars: xaVars
					});
				}

				var field = Ext.create("CMDBuild.Management.ReferenceField.Field", Ext.apply(extraFieldConf,{
					attribute: attribute,
					templateResolver: templateResolver
				}));

				if (subFields && subFields.length > 0) {
					return buildReferencePanel(field, subFields);
				} else {
					return field;
				}
			},

			/**
			 * Custom function to force a manual instantiation of templateResolver to hack a problem of this fields structure type
			 *
			 * @param {Object} attribute
			 * @param {CMDBuild.Management.TemplateResolver} templateResolver
			 *
			 * @return {CMDBuild.Management.ReferenceField.Field} field
			 *
			 * TODO: refactor all field building implementations
			 */
			buildEditor: function(attribute, templateResolver) {
				templateResolver = templateResolver || null;

				return Ext.create("CMDBuild.Management.ReferenceField.Field", {
					attribute: attribute,
					templateResolver: templateResolver
				});
			}
		}
	});

	function getFormPanel(field) {
		return field.findParentByType("form");
	}

	function buildReferencePanel(field, subFields) {
		// If the field has no value the relation attributes must be disabled
		field.mon(field, CHANGE_EVENT, function(combo, val) {
			var disabled = val == "";

			for (var i = 0; i < subFields.length; ++i) {
				var sf = subFields[i];

				if (sf)
					sf.setDisabled(disabled);
			}
		});

		var fieldContainer = {
			xtype: 'container',
			layout: 'hbox',
			margin: "0 0 5 0",

			items: [
				Ext.create('CMDBuild.field.CMToggleButtonToShowReferenceAttributes', {
					subFields: subFields
				}),
				field
			]
		};

		field.labelWidth -= 20;

		return Ext.create('Ext.container.Container', {
			margin: "0 0 5 0",
			items: [fieldContainer].concat(subFields),
			resolveTemplate: function() {
				field.resolveTemplate();
			}
		});
	}

	Ext.define("CMDBuild.Management.ReferenceField.Field", {
		extend: "CMDBuild.Management.SearchableCombo",

		mixins: {
			observable: 'Ext.util.Observable'
		},

		attribute: undefined,

		initComponent: function() {
			var attribute = this.attribute;
			var store = CMDBuild.Cache.getReferenceStore(attribute);

			store.on("loadexception", function() {
				field.setValue('');
			});

			Ext.apply(this, {
				fieldLabel: attribute.description || attribute.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: attribute.name,
				store: store,
				queryMode: "local",
				valueField: "Id",
				displayField: 'Description',
				allowBlank: !attribute.isnotnull,
				grow: true, // XComboBox autogrow
				minChars: 1,
				filtered: false,
				CMAttribute: attribute,
				listConfig: {
					loadMask: false
				}
			});

			this.callParent(arguments);
		},

		listeners: {
			// Force store load and Manage preselectIfUnique metadata without CQL filter
			beforerender: function(combo, eOpts) {
				combo.getStore().load({
					scope: this,
					callback: function(records, operation, success) {
						if (
							!Ext.isEmpty(combo.getStore())
							&& !Ext.isEmpty(this.attribute)
							&& !Ext.isEmpty(this.attribute.meta)
							&& this.attribute.meta['system.type.reference.' + CMDBuild.core.constants.Proxy.PRESELECT_IF_UNIQUE] === 'true'
							&& combo.getStore().getCount() == 1
						) {
							combo.setValue(records[0].get('Id'));
						}
					}
				});
			}
		},

		getErrors: function(rawValue) {
			if (this.templateResolver && this.store) {
				var value = this.getValue();

				if (value && this.store.find(this.valueField, value) == -1)
					return [ CMDBuild.Translation.errors.reference_invalid ];
			}

			return this.callParent(arguments);
		},

		setValue: function(v) {
			if (!Ext.isEmpty(this.store)) {
				v = this.extractIdIfValueIsObject(v);

				// Is one time seems that has a CQL filter
				if (this.ensureToHaveTheValueInStore(v) || this.store.isOneTime)
					this.callParent([v]);
			}
		},

		/**
		 * Adds the record when the store is not completely loaded (too many records)
		 * NOTE: if field has preselectIfUnique metadata skip to add value to store, to avoid stores with more than one items (this is an ugly fix but is just temporary)
		 *
		 * @param {Mixed} value
		 *
		 * @return {Boolean}
		 */
		ensureToHaveTheValueInStore: function(value) {
			value = normalizeValue(this, value);

			// Ask to the server the record to add, return false to not set the value, and set it on success
			if (
				!Ext.isEmpty(value)
				&& !this.store.isLoading()
				&& this.getStore().find(this.valueField, value) == -1
				&& !Ext.isEmpty(this.attribute)
				&& !Ext.isEmpty(this.attribute.meta)
				&& this.attribute.meta['system.type.reference.' + CMDBuild.core.constants.Proxy.PRESELECT_IF_UNIQUE] !== 'true'
			) {
				var params = Ext.apply({ cardId: value }, this.getStore().baseParams);

				CMDBuild.core.interfaces.Ajax.request({
					method: 'GET',
					url: 'services/json/management/modcard/getcard',
					params: params,
					scope: this,
					success: function(response, options, decodedResult) {
						if (!Ext.isEmpty(this.getStore())) {
							this.getStore().add({
								Id: value,
								Description: decodedResult.card['Description']
							});

							this.validate();
						}

						this.setValue(value);
					}
				});

				return false;
			}

			return true;
		},

		resolveTemplate : function() {
			var me = this;
			if (me.templateResolver && !me.disabled) {
				// Don't overlap requests
				if (me.templateResolverBusy) {
					me.requireResolveTemplates = true;

					return;
				}

				me.templateResolverBusy = true;
				me.templateResolver.resolveTemplates( {
					attributes: [FILTER_FIELD],
					callback: function(out, ctx) {
						me.onTemplateResolved(
							out,
							function afterStoreIsLoaded() {
								me.templateResolverBusy = false;

								if (me.requireResolveTemplates) {
									me.requireResolveTemplates = false;
									me.resolveTemplate();
								}
							}
						);
					}
				});
			}
		},

		onTemplateResolved: function(out, afterStoreIsLoaded) {
			this.filtered = true;

			var store = this.store;
			var callParams = this.templateResolver.buildCQLQueryParameters(out[FILTER_FIELD]);

			if (!Ext.isEmpty(store)) {
				if (callParams) {
					// For the popup window! baseParams is not meant to be the old ExtJS 3.x property!
					// Ext.apply(store.baseParams, callParams);
					store.baseParams.filter = Ext.encode({
						CQL: callParams.CQL
					});

					var me = this;
					store.load({
						scope: this,
						callback: function(records, operation, success) {
							// Manage preselectIfUnique metadata with CQL filter
							if (
								!Ext.isEmpty(this.getStore())
								&& !Ext.isEmpty(this.attribute)
								&& !Ext.isEmpty(this.attribute.meta)
								&& this.attribute.meta['system.type.reference.' + CMDBuild.core.constants.Proxy.PRESELECT_IF_UNIQUE] === 'true'
								&& this.getStore().getCount() == 1
							) {
								this.setValue(records[0].get('Id'));
							}

							// Fail the validation if the current selection is not in the new filter
							me.validate();
							afterStoreIsLoaded();
						}
					});
				} else {
					var emptyDataSet = {};
					emptyDataSet[store.root] = [];
					emptyDataSet[store.totalProperty] = 0;

					store.loadData(emptyDataSet);

					afterStoreIsLoaded();
				}

				this.addListenerToDeps();
			}
		},

		addListenerToDeps: function() {
			if (this.depsAdded)
				return;

			this.depsAdded = true;
			// Adding the same listener twice does not double the fired events, that's why it works
			var deps = this.templateResolver.getLocalDepsAsField();

			for (var name in deps) {
				var field = deps[name];

				if (field)
					field.mon(field, CHANGE_EVENT, function() {
						this.reset();
						this.resolveTemplate();
					}, this);
			}
		},

		isFiltered: function() {
			return (typeof this.templateResolver != "undefined");
		},

		setServerVarsForTemplate: function(vars) {
			if (this.templateResolver)
				this.templateResolver.serverVars = vars;
		}
	});

	// See SearchableCombo.addToStoreIfNotInIt
	function adaptResult(result) {
		var data = result.card;
		if (data) {
			return {
				get: function(key) {
					return data[key];
				}
			};
		} else {
			return null;
		}
	}

	// If set the velue programmatically it could be a integer or a string or null or undefined if the set is raised after a selection on the UI,
	//the value is an array of models
	function normalizeValue(me, v) {
		v = CMDBuild.Utils.getFirstSelection(v);

		if (v && typeof v == "object" && typeof v.get == "function")
			v = v.get(me.valueField);

		return v;
	}

})();