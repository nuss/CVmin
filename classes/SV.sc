/*
An SV is a CV that models an index into an array stored in the instance variable 'items'
The method 'item' returns the currently selected array element.

The default GUI presentation in ConductorGUI assumes that items is an array of Symbols.
*/

SV : CV {
	var <items;

	*new { |items, default|
		^super.new.init(items, default);
	}

	init { |argItems, default|
		if (argItems.notNil and: { argItems.isKindOf(Collection) }) {
			items = argItems.collect(_.asSymbol);
		} {
			items = [\nil];
		};
		this.spec_(ControlSpec(0, items.size - 1, \lin, 1, default ? 0));
		this.value_(default ? 0);
	}

	default_ { |value|
		var default;
		if (value.isNumber.not) {
			default = this.getIndex(value)
		} {
			default = value;
		};
		this.prDefault(default);
	}

	items_ { |argItems|
		argItems = argItems.collect(_.asSymbol);
		items = argItems ? [\nil];
		super.sp(this.spec.default, 0, argItems.size - 1, 1, 'lin');
		this.changed(\items);
	}

	item { ^items[this.value] }

	item_ { |item|
		this.value = this.getIndex(item.asSymbol);
	}

	getIndex { |item|
		items.collect(_.asSymbol).do { |it, i| if (item == it) { ^i } };
		^0
	}

	sp { |default = 0, items|
		this.items_(items);
		this.default_(default);
	}

	next { ^items[this.value] }

	storeOn { |stream|
		stream << this.class.name << "(" <<<* [this.items] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}

