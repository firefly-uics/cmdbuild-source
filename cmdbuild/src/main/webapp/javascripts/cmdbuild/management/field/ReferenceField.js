CMDBuild.Management.ReferenceField = (function() {
	return {
		build: function(attribute) {	
			var store = CMDBuild.Cache.getReferenceStore(attribute);
			var permanentStore = CMDBuild.Cache.getReferenceStoreById(attribute.referencedIdClass);
			
			var field = new CMDBuild.Management.SearchableCombo({
				plugins: new CMDBuild.SetValueOnLoadPlugin(),
				fieldLabel: attribute.description,
				name: attribute.name+"_value",
				hiddenName: attribute.name,		
				store: store,
				permanentStore: permanentStore,
				mode: 'local',
				valueField: 'Id',
				displayField: 'Description',
				triggerAction: 'all',
				allowBlank: !attribute.isnotnull,
				grow: true, // XComboBox autogrow
				minChars: 1,
				filtered: false
			});
		
			store.on('loadexception', function() {
				field.valueNotFoundText = '';
				field.setValue('');
			});
			
			var resolveTemplate = function() {
				if (!field.disabled) {
					field.templateResolver.resolveTemplates({
						attributes: ["SystemFieldFilter"],
						callback: onTemplateResolved
					});
				}
			};
			
			var onTemplateResolved = function(out, ctx) {	
				var f = field;
				f.filtered = true;
				var currentValue = f.getValue();
				var currentRawValue = f.getRawValue();
				// was buildCQLQueryParameters(attribute.fieldFilter, ctx), but it did not allow query composition
				var callParams = field.templateResolver.buildCQLQueryParameters(out["SystemFieldFilter"]);
				f.callParams = Ext.apply({}, callParams);
				if (callParams) {
					f.store.baseParams.CQL = callParams.CQL; // NdPaolo: this should not be necessary, where it is set?!
					f.store.load({
						params: callParams,
						//TODO define a business rule to manage the case of inconsistency
						//between data already stored and new filter policy, an idea is an alert message
						//with a pretty window pop up
						callback: function() {
							if (f.isValid()) {
								f.removeClass(f.invalidClass);
							}
						}
					});
				} else {
					var emptyDataSet = {};
					emptyDataSet[f.store.root] = [];
					emptyDataSet[f.store.totalProperty] = 0;
					f.store.loadData(emptyDataSet);
				}
				addListenerToDeps();
			};
			
			var addListenerToDeps = function() {
				var ld = field.templateResolver.getLocalDepsAsField();
				for(var i in ld) {
					//before the blur if the value is changed
					if (ld[i]) {
						ld[i].on('change',resolveTemplate);
					}
				}
			};
			
			if (attribute.fieldFilter) {
				//is using a template
				
				field.vtype = 'valueInStore'; // custom
				field.invalidText = CMDBuild.Translation.errors.reference_invalid;
				
				var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, "system.template.");
				xaVars["SystemFieldFilter"] = attribute.fieldFilter;
		
				field.templateResolver = new CMDBuild.Management.TemplateResolver({
					getBasicForm: function() {
						return field.findParentByType('form').getForm();
					},
					xaVars: xaVars,
					getServerVars: function() {
						return field.findParentByType('form').ownerCt.ownerCt.currentRecord.data;
					}
				});
		
				/*
				 * when the form switches to edit mode, resolve the template and
				 * validate the value in compliance with the filter result
				 */
				field.resolveTemplate = resolveTemplate;
				
				//the validation on select is needed to remove the
				//non validation indicator, after a valid selection
				field.on('select', function(combo, rec, index){
					combo.validate();
				});
			}
			
			// adds the record when the store is not completely loaded (too many records)
			field.setValue = field.setValue.createInterceptor(function(value) {
				if (typeof value == "undefined" || value == "") {
					return;
				}
		
				var storeNotEmpty = this.store.getCount() != 0;		
				var valueNotInStore = this.store.find(this.valueField, value) == -1;		
				var storeNotOneTime = !this.store.isOneTime;
		
				if (storeNotEmpty && this.storeIsLargerThenLimit() && storeNotOneTime && valueNotInStore) {
					this.valueNotFoundText = CMDBuild.Translation.common.loading;
					var newTotal = this.store.getTotalCount();
		
					var _store = this.store;
					var params = Ext.apply({}, _store.baseParams);
					params['Id'] = value;
					
					CMDBuild.Ajax.request({
						url : _store.url,
						params: params,
						method : 'POST',
						success : function(response, options, decoded) {
							decoded[_store.totalProperty] = _store.getTotalCount();
							_store.loadData(decoded, true);
						}
					});
				}
				
			}, field);
			
			field.setValue = field.setValue.createSequence(function(v) {
				if (field.store.isOneTime
						&& v != "" && typeof v != "undefined"
						&& field.store.find(field.valueField, v) == -1) {
					
					var recordIndex = field.permanentStore.find(field.valueField, v);
					if (recordIndex == -1) {
						field.permanentStore.load({
							params: { Id: v },
							add: true,
							callback: function(r,o,s) { 
								field.setRawValue(r[0].data.Description);
							}
						});
					} else {
						var r = field.permanentStore.getAt(recordIndex);
						if (r) {
							field.setRawValue(r.data.Description);
						}
					}			
				}
				
				if (this.storeIsLargerThenLimit()) {
					field.recalculateSize(field.getRawValue());
				}
				
				field.validate();
			});
			
			field.expand = field.expand.createInterceptor(function() {
				if (this.store.isLoading) {
					return false;
				}
				if (this.storeIsLargerThenLimit()) {
					this.createReferenceWindow();
					return false;
				}
			}, field);
			
			return field;
		}
	};
})();