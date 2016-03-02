(function($) {
	var navigationTreesBtnMenu = function() {
		var control = "navigationTreesBtnMenu_menu";
		this.updateMenu = function(classId) {
			// get menu and empty it
			var $menu = $("#" + control).find("ul");
			$menu.empty();
			// update menu values
			var values = $.Cmdbuild.customvariables.cacheTrees.getTreesFromClass(classId);
			$.each(values, function(index, value) {
				var $span = $("<span></span>").text(value.description);
				var $li = $("<li></li>").append($span).click(function() {
					$.Cmdbuild.custom.commands.applyNavigationTree({
						treeValue : value._id
					});
				});
				var active = false;
				if (active) {
					$li.addClass("active");
				}
				$menu.append($li);
			});

			// enable / disable menu button
			if (values.length) {
				$menu.parent().parent().removeClass("btn-disabled");
			}
		};
		this.refreshSelected = function() {
			var current = $.Cmdbuild.customvariables.selected.getCurrent();
			$("#" + control).parent().addClass("btn-disabled");
			if (current) {
				var currentNode = $.Cmdbuild.customvariables.model.getNode(current);
				var currentClassId = $.Cmdbuild.g3d.Model.getGraphData(currentNode, "classId");
				this.updateMenu(currentClassId);
			}
		};
		$.Cmdbuild.customvariables.selected.observe(this);
	};
	$.Cmdbuild.g3d.navigationTreesBtnMenu = navigationTreesBtnMenu;
})(jQuery);
