(function() {
Ext.define("CMDBuild.field.LookupCombo", {
	extend: "CMDBuild.field.ErasableCombo",
	plugins: new CMDBuild.SetValueOnLoadPlugin(),
	parentId: '',

	onTrigger2Click: function() {
		if (!this.disabled) {
			this.focus(); // to fire the change event in the single lookup fields
			this.chainedClear();
		}
	},

	chainedClear: function() {
		this.setValue([""]); // if use clearValue the form does not send the value, so it is not possible delete the value on server side
		if (this.childField) {
			this.childField.setParentIdAndFilterStore(undefined);
			this.childField.chainedClear();
		}
	},

	setValueAndUpdateParents: function(value) {
		if (value == '' || typeof value == 'undefined') {
			this.clearValue();
			this.setParentIdAndFilterStore(undefined);
		} else {
			this.store.clearFilter();
			this.setValue(value);
			var index = this.store.find("Id", value);
			var rec = this.store.getAt(index);
			if (rec) {
				this.setParentIdAndFilterStore(rec.get("ParentId"));
			} else {
				this.setParentIdAndFilterStore(undefined);
			}
		}

		if (this.parentField) {
			this.parentField.setValueAndUpdateParents(this.parentId);
		}
	},

	setParentIdAndFilterStore: function(newParentId) {
		if (this.parentId != newParentId) {
			this.parentId = newParentId;
			this.filterStoreByParentId();
		}
	},

	filterStoreByParentId: function() {
		var parentId = this.parentId || '';
		this.store.filterBy(function(record, id) {
			return record.get("ParentId") == parentId;
		});
//		this.growSizeFix(); TODO 3 to 4
	}
});

Ext.define("CMDBuild.Management.LookupCombo", {
	statics: {
		build: function(attribute) {
			if (attribute.lookupchain.length == 1) {
				return buildSingleLookupField(attribute);
			} else {
				var hiddenField = buildHiddenField(attribute);
				var fieldSetItems = buildFieldSetItems(attribute, hiddenField);
				fieldSetItems.push(hiddenField);

				var fieldSet = new Ext.panel.Panel({
					name: attribute.name, // adds only this field to the basic form
					border: false,
					frame: false,
					items: fieldSetItems,
					autoHeight: true,
					hideMode: 'offsets',
					grow: true,
					labelWidth: CMDBuild.LABEL_WIDTH,
					bodyCls: "x-panel-default-framed",
					bodyStyle: {
						padding: "0"
					},
					CMAttribute: attribute,
					
					growSizeFix: function() {
						for (var i = 0; i < fieldSetItems.length; i++) {
							var field = fieldSetItems[i];
							if (field.growSizeFix) {
								if (field.rendered) {
									field.growSizeFix();
								} else {
									field.on("render", field.growSizeFix, field, {single: true});
								}
							}
						}
					},
					
					setValue: function(v) {
						hiddenField.setValue(v);
					}
					
				});
				
				return fieldSet;
			}
		}
	}
});


//private
var canBeBlank = function(attribute) {
	return !((attribute.fieldmode == "required") || attribute.isnotnull);
};

// private
function buildHiddenField(attribute) {
	var hiddenField = new Ext.form.Hidden({
		name: attribute.name,
		allowBlank: canBeBlank(attribute),
		
		updateParentsIfLoaded: function() {
			var value = this.getValue();
			if (this.lastCombo) {
				this.lastCombo.setValueAndUpdateParents(value);
			} else {
				CMDBuild.log.error("Last combo not set");
			}
		},
		
		filterByParentId: function(lastComboId) {
			this.setValueAndFireChange(lastComboId);
		},
		
		chainedClear: function() {
			this.setValueAndFireChange("");
		},

		// The hidden field is never given the focus, so it never fires the change event,
		// thus not updating filtered references depending on it
		setValueAndFireChange: function(newValue) {
			var oldValue = this.getValue() || "";
			if (oldValue != newValue) {
				this.setValue(newValue, true);
				this.fireEvent('change', newValue, oldValue);
			}
		},
		
		setParentIdAndFilterStore: Ext.emptyFn
	});

	hiddenField.setValue = Ext.Function.createSequence(hiddenField.setValue, 
			function(value, dontUpdateParents) {
				if (!dontUpdateParents) {
					hiddenField.updateParentsIfLoaded();
				}
			}
	);
	
	return hiddenField;
};

//private
var buildFieldSetItems = function(attribute, hiddenField) {
	var lookupChain = attribute.lookupchain;
	var fieldSetItems = [];
	var currentField;
	var parentField = null;
	for (var i=0, len=lookupChain.length; i<len; ++i) {
		var currentLookupType = lookupChain[len-i-1]; // reverse order
		var forgedAttribute = forgeAttributeForMultilevelLookup(attribute, currentLookupType);
		var hideLabel = (i != 0);
		currentField = buildSingleLookupField(forgedAttribute, hideLabel);
		
		if (parentField) {
			currentField.parentField = parentField;
			parentField.childField = currentField;
		}
		
		fieldSetItems.push(currentField);
		addEventsToMultilevelLookupCombo(currentField, parentField);
		parentField = currentField;
		
		currentField.store.on('load', function() {
			hiddenField.updateParentsIfLoaded();
		}, currentField);
	}
	
	bindHiddenFieldToLastCombo(hiddenField, currentField);
	return fieldSetItems;
};

//private
var bindHiddenFieldToLastCombo = function(hiddenField, lastCombo) {
	hiddenField.lastCombo = lastCombo;
	lastCombo.childField = hiddenField;
	
	hiddenField.getReadableValue = function() {
		return lastCombo.getRawValue();
	};
};

//private
var buildSingleLookupField = function(attribute, hideLabel) {
	var store = _CMCache.getLookupStore(attribute.lookup),
		fieldLabel,
		labelSeparator,
		padding;

	if (hideLabel) {
		fieldLabel = "";
		labelSeparator = "";
		padding = "0 0 0 " + (CMDBuild.LABEL_WIDTH + 5);
	} else {
		fieldLabel = attribute.description;
		if (!canBeBlank(attribute)) {
			fieldLabel = "* "+fieldLabel;
		}
		labelSeparator = ":";
	}

	var field = new CMDBuild.field.LookupCombo({
		labelAlign: "right",
		labelWidth: CMDBuild.LABEL_WIDTH,
		fieldLabel: fieldLabel,
		labelSeparator: labelSeparator,
		padding: padding,
		name: attribute.name,
		hiddenName: attribute.name,
		store: store,
		queryMode: 'local',
		triggerAction: "all",
		lazyInit: false,
		valueField: 'Id',
		displayField: 'Description',
		allowBlank: canBeBlank(attribute),
		grow: true, // XComboBox autogrow
		minChars: 1,
		CMAttribute: attribute
	});

	field.filterByParentId = function(parentId) {
		var autoselectedId;
		this.setParentIdAndFilterStore(parentId);
		if (this.store.getCount() == 1) {
			var rec = this.store.getAt(0);
			autoselectedId = rec.get("Id");
			this.setValue(autoselectedId);
		} else {
			this.clearValue();
		}
		
		if (this.childField) {
			this.childField.filterByParentId(autoselectedId);
		}
	};
	
	field.on('select', function(combo, record, index) {
		if (this.childField) {
			this.childField.filterByParentId(record[0].get("Id"));
		}
	}, field);
	
	return field;
};

//private
var forgeAttributeForMultilevelLookup = function(attribute, lookupName) {
	var notRequired = (attribute.fieldmode != "required"); 
	var label = (notRequired ? '': '* ') + attribute.description;
	
	var forgedAttribute = {
		lookup: lookupName,
		isnotnull: attribute.isnotnull,
		description: label,
		name: attribute.name + "_" + lookupName
	};
	
	return forgedAttribute;
};

//private
var addEventsToMultilevelLookupCombo = function(currentField, parentField) {
	if (parentField) {
		//HACK ComboBox.doQuery calls store.clearFilter()
		currentField.on('beforequery', function(qe) {
			qe.combo.lastQuery = qe.query; // to deny clearing the filter
		});
	}
};

})();