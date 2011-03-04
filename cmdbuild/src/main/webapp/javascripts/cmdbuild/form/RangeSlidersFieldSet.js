(function() {
	var handleMaxSliderEvents = function(rangeSlider) {
		rangeSlider.maxSliderField.slider.on("dragstart", function(slider, event) {
			slider.startDragValue = slider.getValue();
		});
		
		rangeSlider.maxSliderField.slider.on("dragend", function(slider, event) {
			if (slider.getValue() < rangeSlider.minSliderField.getValue()) {
				slider.setValue(rangeSlider.minSliderField.getValue());
			}
			slider.startDragValue = undefined;
		});
	};
	
	var handleMinSliderEvents = function(rangeSlider) {
		rangeSlider.minSliderField.slider.on("dragstart", function(slider, event) {
			slider.startDragValue = slider.getValue();
		});
		
		rangeSlider.minSliderField.slider.on("dragend", function(slider, event) {
			if (slider.getValue() > rangeSlider.maxSliderField.getValue()) {
				slider.setValue(rangeSlider.maxSliderField.getValue());
			}
			slider.startDragValue = undefined;
		});
	};
	
	var handleDisableEvent = function(rangeSlider) {
		rangeSlider.on("enable", function(){
			rangeSlider.maxSliderField.enable();
			rangeSlider.minSliderField.enable();
		});
		rangeSlider.on("disable", function(){
			rangeSlider.maxSliderField.disable();
			rangeSlider.minSliderField.disable();
		});
	};
	
	CMDBuild.RangeSlidersFieldSet = Ext.extend(Ext.form.FieldSet, {
		maxSliderField: undefined,
		minSliderField: undefined,
		
		initComponent: function() {
			if (!this.maxSliderField) {
				throw new Error("You must assign a maxSliderField to the RangeSliderFieldset");
			}
			if (!this.minSliderField) {
				throw new Error("You must assign a minSliderField to the RangeSliderFieldset");
			}
			Ext.apply(this,{
				border: false,
				style: {
					padding: 0
				},
				bodyStyle: {
					padding: 0
				},
				items: [this.minSliderField,this.maxSliderField]
			});
			CMDBuild.RangeSlidersFieldSet.superclass.initComponent.apply(this, arguments);
			handleDisableEvent(this);
			handleMaxSliderEvents(this);
			handleMinSliderEvents(this);
		},
		
		disable: function() {
			this.maxSliderField.disable();
			this.minSliderField.disable();
		},
		
		enable: function() {
			this.maxSliderField.enable();
			this.minSliderField.enable();
		}
	});
})();