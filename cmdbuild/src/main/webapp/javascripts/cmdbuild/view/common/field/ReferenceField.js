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
            var permanentStore = CMDBuild.Cache.getReferenceStoreById(attribute.referencedIdClass);
            var store = CMDBuild.Cache.getReferenceStore(attribute);

            store.on("loadexception", function() {
                field.valueNotFoundText = '';
                field.setValue('');
            });

            Ext.apply(this, {
                plugins: new CMDBuild.SetValueOnLoadPlugin(),
                fieldLabel: attribute.description,
                labelWidth: CMDBuild.CM_LABEL_WIDTH,
                name: attribute.name,
                store: store,
                permanentStore: permanentStore,
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
            this.preSetValue(v);
            this.callParent(arguments);
            this.postSetValue(v);
        },

        /*
         * Adds the record when the store is not completely loaded (too many records)
         */
        preSetValue: function(value) {
            if (typeof value == "undefined" || value == "") {
                return;
            }

            var storeNotEmpty = this.store.getCount() != 0;		
            var valueNotInStore = this.store.find(this.valueField, value) == -1;		
            var storeNotOneTime = !this.store.isOneTime;

            if (storeNotEmpty && this.storeIsLargerThenLimit() && storeNotOneTime && valueNotInStore) {
                this.valueNotFoundText = CMDBuild.Translation.common.loading;
                var _store = this.store;
                var params = Ext.apply({}, _store.baseParams); // WRONG!
                params["Id"] = value;

                CMDBuild.Ajax.request({
                    url : _store.proxy.url,
                    params: params,
                    method : "POST",
                    success : function(response, options, decoded) {
                        decoded[_store.totalProperty] = _store.getTotalCount();
                        _store.loadData(decoded, true);
                    }
                });
            }
        },

        postSetValue: function(v) {
            if (!this.store.isOneTime
                && v != "" && typeof v != "undefined"
                && this.store.find(this.valueField, v) == -1) {

                var recordIndex = this.permanentStore.find(this.valueField, v);
                if (recordIndex == -1) {
                    var me = this;
                    this.permanentStore.load({
                        params: {
                            Id: v
                        },
                        add: true,
                        callback: function(r,o,s) { 
                            me.setRawValue(r[0].data.Description);
                        }
                    });
                } else {
                    var r = this.permanentStore.getAt(recordIndex);
                    if (r) {
                        this.setRawValue(r.data.Description);
                    }
                }
            }
            this.validate();
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

})();