(function() {
	
Ext.define("CMDBuild.field.LookupCombo", {
	extend: "Ext.form.field.ComboBox",
	trigger1cls: Ext.form.field.ComboBox.triggerCls,
    trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
   	hideTrigger1 :false,
	hideTrigger2 :false,

	plugins: new CMDBuild.SetValueOnLoadPlugin(),
	parentId: '',
	
	onTrigger1Click: Ext.form.field.ComboBox.prototype.onTriggerClick,
	onTrigger2Click: function() {
    	if (!this.disabled) {
    		this.focus(); // to fire the change event in the single lookup fields
    		this.chainedClear();
    	}
    },

	chainedClear: function() {
    	this.clearValue();
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
				this.setParentIdAndFilterStore(rec.data.ParentId);
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
			return record.data.ParentId == parentId;
		});
		this.growSizeFix();
	}
});

CMDBuild.Management.LookupCombo = {}
CMDBuild.Management.LookupCombo.build = function(attribute) {	
	if (attribute.lookupchain.length == 1) {
		return buildSingleLookupField(attribute);
	} else {
		var hiddenField = buildHiddenField(attribute);
		var fieldSetItems = buildFieldSetItems(attribute, hiddenField);
		fieldSetItems.push(hiddenField);
		var fieldSet = new Ext.form.FieldSet({
			name: attribute.name, // adds only this field to the basic form
			border: false,
			autoWidth: true,
			items: fieldSetItems,
			autoHeight: true,
			hideMode: 'offsets',
			grow: true,
			style: { margin: '0', padding: '0'},
			labelWidth: 160,
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
};


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

	hiddenField.setValue = hiddenField.setValue.createSequence(function(value, dontUpdateParents) {
		if (!dontUpdateParents) {
			hiddenField.updateParentsIfLoaded();
		}
	});
	
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
	var store = CMDBuild.Cache.getLookupStore(attribute.lookup);
	var field = new CMDBuild.field.LookupCombo({
		fieldLabel: hideLabel ? '' : canBeBlank(attribute) ? attribute.description : '* '+attribute.description,
		labelSeparator: hideLabel ? '' : undefined,
		name: attribute.name,
		hiddenName: attribute.name,
		store: store,
		queryMode: 'local',
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
			autoselectedId = rec.data.Id;
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
			this.childField.filterByParentId(record.data.Id);
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
		// ComboBox.doQuery calls store.clearFilter()
		// TODO override this behavior 
		currentField.on('expand', function(combo) {
			this.filterStoreByParentId();
			var filteredCount = this.store.getCount();
			if (filteredCount > 0) {
				return true;
			} else {
				this.collapse();
				return false;
			}
		}, currentField);
	}
};

})();