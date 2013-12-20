Logger = function() {
	var enabled = true;

	this.enable = function() {
		enabled = true;
	};

	this.disable = function() {
		enabled = false;
	};

	this.log = function() {
		if (enabled
			&& console
			&& typeof console.log == "function") {

			console.log.apply(console, arguments);
		}
	};
};
