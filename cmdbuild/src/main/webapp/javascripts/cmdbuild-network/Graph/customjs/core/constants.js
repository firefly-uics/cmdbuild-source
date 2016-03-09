(function($) {
	$.Cmdbuild.g3d.constants = {
		GUICOMPOUNDNODE : "GUICOMPOUNDNODE",
		GUICOMPOUNDNODEDESCRIPTION : "Compound node",
		DEFAULT_SHAPE : "cmdbuildDefaultShape",
		SELECTION_SHAPE : "cmdbuildSelectionShape",
		SPRITES_PATH : "theme/images/sprites/",
		LABELS_ON_SELECTED : "selected",
		NO_LABELS : "none",
		RANGE_VIEWPOINTDISTANCE : 100,
		EXPANDING_THRESHOLD : 30,
		MIN_MOVEMENT : 5,
		TOOLTIP_WINDOW : "viewerInformation",
		COMPOUND_ATTRIBUTES : [ {
			type : "string",
			name : "type",
			description : "Type",
			displayableInList : true
		}, {
			type : "string",
			name : "description",
			description : "Description",
			displayableInList : true
		} ]

	};
})(jQuery);