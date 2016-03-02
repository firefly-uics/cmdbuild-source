(function($) {
	var scripts = {
		canvas3d : function(param) {
			new $.Cmdbuild.g3d.Viewer(param.id);
		},
		counter : function(param) {
			new counter(param);
		},
		navigationTreesCombo : function(param) {
			$("#" + param.id).selectmenu({});
			var position = {
				top : -400,
				of : "#" + "optionsPanelContainer"
			};
			$("#" + param.id).selectmenu("option", "style", "popup");
			$("#" + param.id).selectmenu("option", "position", position);
		},
		buttonset : function(param) {
			$("#" + param.id).buttonset();
		}
	};
	$.Cmdbuild.custom.scripts = scripts;
})(jQuery);

function counter(param) {
	var myVar = setInterval(function() {
		setText(param);
	}, 1000);

	function setText(param) {
		if ($("#" + param.id).length > 0) {
			var text = "";
			switch (param.type) {
			case "nodes":
				text = $.Cmdbuild.customvariables.model.nodesLength();
				break;
			case "edges":
				text = $.Cmdbuild.customvariables.model.edgesLength();
				break;
			case "selected":
				text = $.Cmdbuild.customvariables.selected.length();
				break;
			default:
				alert("Unknown counter");
			}
			$("#" + param.id).text(text);
		} else {
			clearInterval(myVar);
		}
	}
}
