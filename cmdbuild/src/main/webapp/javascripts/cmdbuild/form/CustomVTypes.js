var fullemailspec = /^[\w ]*\<([^\<\>]+)\>[ ]*$/;
var numericRegExp = /^(([+,\-]?[0-9]+)|[0-9]*)(\.[0-9]+)?$/;
var ipv4RegExp =  /^(1\d{0,2}|2(?:[0-4]\d{0,1}|[6789]|5[0-5]?)?|[3-9]\d?|0)\.(1\d{0,2}|2(?:[0-4]\d{0,1}|[6789]|5[0-5]?)?|[3-9]\d?|0)\.(1\d{0,2}|2(?:[0-4]\d{0,1}|[6789]|5[0-5]?)?|[3-9]\d?|0)\.(1\d{0,2}|2(?:[0-4]\d{0,1}|[6789]|5[0-5]?)?|[3-9]\d?|0)(\/(?:[012]\d?|3[012]?|[0-9]?)){0,1}$/;
var ipv6RegExp = /^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/;
	var numericValidation = function (value, scale, precision) {
	var out = {
		valid: true,
		message: ''
	};

	if (value.match(numericRegExp) == null) {
		out = {
			valid: false,
			message: CMDBuild.Translation.vtype_text.invalid_character
		};
	}
	var splitByDecimalSeparator = value.split(".");
	var integerPart = Math.abs(splitByDecimalSeparator[0]);
	var decimalPart = splitByDecimalSeparator[1];

	if (precision !== undefined) {
		var integerPartMaxlength = precision - (scale || 0);
		if (integerPart && new String(integerPart).length > integerPartMaxlength) {
			out = {
				valid: false,
				message: Ext.String.format(CMDBuild.Translation.vtype_text.wrong_integer_part ,integerPartMaxlength)
			};
		}
	};

	if (scale !== undefined) {
		if (decimalPart && decimalPart.length > scale) {
			out = {
				valid: false,
				message: Ext.String.format(CMDBuild.Translation.vtype_text.wrong_decimal_part, scale)
			};
		}
	}

	return out;
};

Ext.apply(Ext.form.VTypes, {
		cmdbcomment : function(val, field) {
		return !val.match("[|']");
		},
	cmdbcommentText : CMDBuild.Translation.vtype_text.cmdbcomment || 'Pipe or apostrophe not allowed',

		cmdbcommentrelaxed : function(val, field) {
		return !val.match("[|]");
		},
	cmdbcommentrelaxedText :  CMDBuild.Translation.vtype_text.cmdbcommentrelaxedText || 'Pipe not allowed',

	emailaddrspec : function(v) {
		 var inner = v.match(fullemailspec);
		 if (inner) {
			 v = inner[1];
		 }
		 return Ext.form.VTypes.email(v);
	},
	emailaddrspecText : Ext.form.VTypes.emailText,

	emailaddrspeclist : function(v) {
		var a = v.split(",");
		for (var i=0,len=a.length; i<len; ++i) {
			var sv = Ext.String.trim(a[i]);
			if (sv && !Ext.form.VTypes.emailaddrspec(sv)) {
				return false;
			}
		}
			return true;
	},
	emailaddrspeclistText : Ext.form.VTypes.emailText,

	emailOrBlank: function(v) {
		return (v.length == 0 || this.email(v));
	},

	emailOrBlankText : Ext.form.VTypes.emailText,

	/**
	 * FIXME: don't avoids to type alphabetical digits in field
	 */
	numeric: function(val, field) {
		var valid = numericValidation(val, field.scale, field.precision);
		field.vtypeText = valid.message;

		return valid.valid;
	},

	ipv4: function(value, field) {
		 return value.match(ipv4RegExp) != null;
	},
	ipv4Text: CMDBuild.Translation.vtype_text.wrong_ip_address,

	ipv6: function(value, field) {
			return value.match(ipv6RegExp) != null;
	},
	ipv6Text: CMDBuild.Translation.vtype_text.wrong_ip_address,

	time: function(value, field) {
		field.vtypeText = Ext.String.format(CMDBuild.Translation.vtype_text.wrong_time, value, field.format);
		return Ext.Date.parse(value, field.format);
	}
});
