CMDBuild.SliderFieldPlugin = function(config) {
    Ext.apply(this, config);
};

Ext.extend(CMDBuild.SliderFieldPlugin, Ext.util.Observable, {
    init: function(field) {
		field.setValue = Ext.Function.createSequence(field.setValue, function(v) {
			// TODO extjs 3 to 4 migration @@
//			field.slider.syncThumb();
		});
    }
});