(function($) {
	var elements = {
		canvas3d : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var mouseover = "";//"this.style.display = 'none';";
			htmlStr += "<div class='viewerInformation' id='viewerInformation' onmouseenter='" + mouseover + "'>";
//			htmlStr += "<img id='viewerInformationImg' src='" + $.Cmdbuild.SpriteArchive.class2Sprite("Client") + "' />";
//			htmlStr += "<p id='viewerInformationCard'></p>";
//			htmlStr += "<p id='viewerInformationClass'></p>";
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
		combo : function(xmlElement) {
			var htmlStr = "";
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var params = $.Cmdbuild.elementsManager.getParams(xmlElement);
			htmlStr += "<span id='margin10'>" + text + "</span>";
			htmlStr += "<select id='" + id + "'>";
			htmlStr += "<option value='0'>One...</option>";
			htmlStr += "<option value='1'>Two...</option>";
			htmlStr += "<option value='2'>Three...</option>";
			htmlStr += "<option value='3'>Four...</option>";
			htmlStr += "<option value='4'>Five...</option>";
			htmlStr += "<option value='5'>Six...</option>";
			htmlStr += "</select>";
//			$.Cmdbuild.scriptsManager.push({
//				script : "combo",
//				id : id
//			});
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
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var paramForClick = $.Cmdbuild.elementsManager.getEvent("onChange", xmlElement);
			htmlStr += "<div " + ca + ">";
			for (var key in paramActualized) {
				var text = paramActualized[key];
				paramForClick.value = key;
				var onClick = " onClick=\'$.Cmdbuild.eventsManager.onEvent(" + JSON.stringify(paramForClick) + ");\'";
				htmlStr += "<input type='radio' id='" + key + "' name='" + id + "' " + onClick + "><label for='" + key + "'>" +  text + "</label>";
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
