(function($) {
	var SpriteArchive = {
		class2Sprite: function(className) {
			var defaultShape = $.Cmdbuild.g3d.constants.DEFAULTSHAPE;
			var prefix = $.Cmdbuild.appConfigUrl + $.Cmdbuild.g3d.constants.SPRITESPATH;
			var sprite = "";
			sprite = this.keyInArchive("systemKeys", className);
			if (sprite) {
				return prefix + sprite;				
			}
			sprite = this.keyInArchive("networkKeys", className);
			if (sprite) {
				return prefix + sprite;				
			}
			sprite = this.keyInArchive("systemKeys", defaultShape);
			if (sprite) {
				return prefix + sprite;				
			}
			console.log("SpriteArchive not found default sprite key:" + className);
			return "";
		},
		keyInArchive: function(database, className) {
			var class2Key = $.Cmdbuild.custom.configuration.class2Key;
			var key2Sprite = $.Cmdbuild.custom.configuration.key2Sprite;
			if (class2Key[database] && class2Key[database][className] && key2Sprite[class2Key[database][className]]) {
				return key2Sprite[class2Key[database][className]];
			}
			else {
				return undefined;
			}
		}
	};
	$.Cmdbuild.SpriteArchive = SpriteArchive;
})(jQuery);