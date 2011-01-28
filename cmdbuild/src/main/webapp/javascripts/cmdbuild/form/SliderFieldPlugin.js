CMDBuild.SliderFieldPlugin = function(config) {
    Ext.apply(this, config);
};

Ext.extend(CMDBuild.SliderFieldPlugin, Ext.util.Observable, {
    init: function(field) {
		field.setValue = field.setValue.createSequence(function(v) {
			field.slider.syncThumb();
		});
    }
});