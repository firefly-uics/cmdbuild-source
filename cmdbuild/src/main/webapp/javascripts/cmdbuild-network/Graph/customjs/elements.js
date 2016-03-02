(function($) {
	var elements = {
		canvas3d : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var mouseover = "";
			htmlStr += "<div class='viewerInformation ui-widget ui-corner-all' id='viewerInformation' onmouseenter='" + mouseover + "'>";
			htmlStr += "</div>";
			htmlStr += "<div  " + ca + ">";
			htmlStr += "</div>";

			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			$.Cmdbuild.scriptsManager.push({
				script : "canvas3d",
				id : id
			});
			return htmlStr;
		},
		navigationTreesCombo : function(xmlElement) {
			var htmlStr = "";
			htmlStr += $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var params = $.Cmdbuild.elementsManager.getParams(xmlElement);
			htmlStr += "<span id='margin10'>" + text + "</span>";
			htmlStr += "<select id='" + id + "'>";
			htmlStr += "</select>";
			$.Cmdbuild.scriptsManager.push({
				script : "navigationTreesCombo",
				id : id
			});
			return htmlStr;
		},
		counter : function(xmlElement) {
			var htmlStr = "";
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var params = $.Cmdbuild.elementsManager.getParams(xmlElement);
			htmlStr += "<span class='counterType'>" + text + "</span>";
			htmlStr += "<span class='counterValue' id='" + id + "'>" + "0" + "</span>";
			$.Cmdbuild.scriptsManager.push({
				script : "counter",
				id : id,
				type: params.type
			});
			return htmlStr;
		},
		buttonset : function(xmlElement) {
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);

			if (paramActualized.condition !== undefined && ! paramActualized.condition) {
				return "";
			}
			var htmlStr = "";
			htmlStr += $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var paramForClick = $.Cmdbuild.elementsManager.getEvent("onChange", xmlElement);
			htmlStr += "<div " + ca + ">";
			for (var key in paramActualized) {
				var text = paramActualized[key];
				paramForClick.value = key;
				var onClick = " onClick=\'$.Cmdbuild.eventsManager.onEvent(" + JSON.stringify(paramForClick) + ");\'";
				htmlStr += "<input type='radio' id='" + key + "' name='" + id + "'><label for='" + key +"' " + onClick + ">" +  text + "</label>";
			}
			htmlStr += "</div>";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			if (param) {
				$.Cmdbuild.dataModel.prepareCallerParameters(id, param);
			}
			$.Cmdbuild.scriptsManager.push({
				script : "buttonset",
				id : id
			});
			return htmlStr;
		}
	};
	$.Cmdbuild.custom.elements = elements;
})(jQuery);

