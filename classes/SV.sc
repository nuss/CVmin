/*
An SV is a CV that models an index into an array stored in the instance variable 'items'
The method 'item' returns the currently selected array element.

The default GUI presentation in ConductorGUI assumes that items is an array of Symbols.
*/

SV : CV {
	var <items;

	*new { |items, default|
		// superclass CV's has two arguments
		// simply pass in nil for them
		^super.newCopyArgs(nil, nil, items).init(default);
	}

	init { |default|
		this.spec_(ControlSpec(0, items.size - 1, \lin, 1, default ? 0));
		this.value_(default);
	}

	default_ { |value|
		var default;
		if (value.isNumber.not) {
			default = this.getIndex(value)
		} {
			default = value;
		};
		super.default_(default);
	}

	items_ { |argItems|
		items = argItems ? [\nil];
		super.sp(this.spec.default, 0, items.size - 1, 1, 'lin');
		this.changed(\items);
	}

	item { ^items[this.value] }

	item_ { |symbol|
		this.value = this.getIndex(symbol);
	}

	getIndex { |symbol|
		items.do { |it, i| if (symbol == it) { ^i } };
		^0
	}

	sp { |default = 0, symbols|
		this.items_(symbols);
		this.default_(default);
	}

	next { ^items[value] }

}

