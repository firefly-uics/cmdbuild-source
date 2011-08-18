(function() {

    var FILTER_FIELD = "_SystemFieldFilter";

    Ext.define("CMDBuild.Management.ReferenceField", {
        statics: {
            build: function(attribute, subFields) {
                var templateResolver;

                if (attribute.fieldFilter) { // is using a template
                    var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, "system.template.");
                    xaVars[FILTER_FIELD] = attribute.fieldFilter;
                    templateResolver = new CMDBuild.Management.TemplateResolver({
                        getBasicForm: function() {
                            return getFormPanel(field).getForm();
                        },
                        xaVars: xaVars,
                        getServerVars: function() {
                            // FIXME! It gives me the shivers! Use the controllers for God's sake!
                            return getFormPanel(field).ownerCt.ownerCt.currentRecord.data;
                        }
                    });
                }

                var field = Ext.create("CMDBuild.Management.ReferenceField.Field", {
                    attribute: attribute,
                    templateResolver: templateResolver
                });

                if (subFields && subFields.length > 0) {
                    return buildReferencePanel(field, subFields);
                } else {
                    return field;
                }
            }
        }
    });

    function getFormPanel(field) {
        return field.findParentByType("form");
    }

    function buildReferencePanel(field, subFields) {
        var subFieldsPanel = new Ext.panel.Panel({
            bodyCls: "x-panel-body-default-framed",
            bodyStyle: {
                padding: "0 0 10px 15px"
            },
            hideMode: "offsets",
            hidden: true,
            frame: false,
            items: [subFields]
        }),

        button = new CMDBuild.field.CMToggleButtonToShowReferenceAttributes({
            subfieldsPanel: subFieldsPanel,
            margin: "1"
        });

        // If the field has no value the relation attributes must be disabled
        field.mon(field, "change", function(combo, val) {
            var disabled = val == "";
            Ext.Array.forEach(subFields, function (sf) {
                sf.setDisabled(disabled);
            });
        });

        return new Ext.panel.Panel({
            frame: false,
            border: false,
            bodyCls: "x-panel-body-default-framed",
            items: [{
                    xtype:'panel',
                    bodyCls: "x-panel-body-default-framed",
                    frame: false,
                    layout: "hbox",
                    items: [field, button]
                },
                subFieldsPanel
            ]
        });
    }

    Ext.define("CMDBuild.Management.ReferenceField.Field", {
        extend: "CMDBuild.Management.SearchableCombo",
        attribute: undefined,

        initComponent: function() {
            var attribute = this.attribute;
            var store = CMDBuild.Cache.getReferenceStore(attribute);

            store.on("loadexception", function() {
                field.setValue('');
            });

            Ext.apply(this, {
                plugins: new CMDBuild.SetValueOnLoadPlugin(),
                fieldLabel: attribute.description,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
                name: attribute.name,
                store: store,
                queryMode: "local",
                valueField: "Id",
                displayField: 'Description',
                allowBlank: !attribute.isnotnull,
                grow: true, // XComboBox autogrow
                minChars: 1,
                filtered: false,
                CMAttribute: attribute
            });

            this.callParent(arguments);
        },

        getErrors: function(rawValue) {
            if (this.templateResolver) {
                var value = this.getValue();
                if (value && this.store.find(this.valueField, value) == -1) {
                    return [CMDBuild.Translation.errors.reference_invalid];
                }
            }
            return this.callParent(arguments);
        },

        setValue: function(v) {
            if (this.store.isOneTime || this.preSetValue(v) !== false) {
            	this.callParent(arguments);
            }
        },

        /*
         * Adds the record when the store is not completely loaded (too many records)
         */
        preSetValue: function(value) {
			value = CMDBuild.Utils.getFirstSelection(value);

        	if (!value || this.store.isLoading()) {
        		return true;
        	}

        	var theValue = value; // but value is a string or an array
            if (typeof value == "object") {
            	theValue = value.get(this.valueField);
            }

	        var valueNotInStore = this.store.find(this.valueField, theValue) == -1;
            if (valueNotInStore) {
            	// ask to the server the record to add, return false to
            	// not set the value, and set it on success

                var params = Ext.apply({Id: theValue}, this.store.baseParams);

                CMDBuild.Ajax.request({
                    url : this.store.proxy.url,
                    params: params,
                    method : "POST",
                    scope : this,
                    success : function(response, options, decoded) {
                		this.addToStoreIfNotInIt(adaptResult(decoded));
                		this.setValue(theValue);
                    }
                });

                return false;
            }
            return true;
        },

        resolveTemplate: function() {
            if (this.templateResolver && !this.disabled) {
                this.templateResolver.resolveTemplates({
                    attributes: [FILTER_FIELD],
                    callback: this.onTemplateResolved,
                    scope: this
                });
            }
        },

        onTemplateResolved: function(out, ctx) {
            this.filtered = true;
            var store = this.store;
            var callParams = this.templateResolver.buildCQLQueryParameters(out[FILTER_FIELD]);
            this.callParams = Ext.apply({}, callParams);
            if (callParams) {
                // For the popup window! baseParams is not meant to be the old ExtJS 3.x property!
                Ext.apply(store.baseParams, callParams);

                var me = this;
                store.load({
                    params: callParams,
                    callback: function() {
                        // Fail the validation if the current selection is not in the new filter
                        me.validate();
                    }
                });
            } else {
                var emptyDataSet = {};
                emptyDataSet[store.root] = [];
                emptyDataSet[store.totalProperty] = 0;
                store.loadData(emptyDataSet);
            }
            this.addListenerToDeps();
        },

        addListenerToDeps: function() {
            // Adding the same listener twice does not double the fired events, that's why it works
            var ld = this.templateResolver.getLocalDepsAsField();
            for (var i in ld) {
                // Before the blur if the value is changed
                if (ld[i]) {
                    ld[i].on("change", this.resolveTemplate, this);
                }
            }
        }
    });

    // see SearchableCombo.addToStoreIfNotInIt
    function adaptResult(result) {
    	var data = result.rows[0];
    	return {
    		get: function(key) {
    			return data[key];
    		}
    	};
    }
})();