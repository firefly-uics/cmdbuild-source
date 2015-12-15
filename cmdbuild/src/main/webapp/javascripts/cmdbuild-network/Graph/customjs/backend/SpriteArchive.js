(function($) {
	var SpriteArchive = {
		class2Sprite: function(className) {
			var class2Key = $.Cmdbuild.custom.configuration.class2Key;
			var key2Sprite = $.Cmdbuild.custom.configuration.key2Sprite;
			var sprite = $.Cmdbuild.appConfigUrl + "sprites/";
			if (class2Key[className] && key2Sprite[class2Key[className]]) {
				sprite += key2Sprite[class2Key[className]];
			} else {
				sprite += $.Cmdbuild.custom.configuration.defaultSprite;
			}
			return sprite;
		}
	};
	$.Cmdbuild.SpriteArchive = SpriteArchive;
})(jQuery);