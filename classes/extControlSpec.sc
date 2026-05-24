+ControlSpec {
	// multichannel specs should return 1 or greater - analog to Collection:-size
	// ortherwise 0 - analog to Object:-size
	size {
		var size = [
			minval.size,
			maxval.size,
			step.size,
			this.default.size
		].maxItem;

		^size;
	}

	// split a multichannel ControlSpec into an array of one-dimensional ControlSpecs
	split {
		var specs, thisMinval, thisMaxval, thisStep, thisDefault;

		if (this.size > 1) {
			specs = Array.newClear(this.size);
			this.size.do { |i|
				if (minval.isArray) { thisMinval = minval.wrapAt(i) } { thisMinval = minval };
				if (maxval.isArray) { thisMaxval = maxval.wrapAt(i) } { thisMaxval = maxval };
				if (step.isArray) { thisStep = step.wrapAt(i) } { thisStep = step };
				if (this.default.isArray) { thisDefault = this.default.wrapAt(i) } { thisDefault = this.default };
				specs[i] = ControlSpec(thisMinval, thisMaxval, warp, thisStep, thisDefault, grid);
			}
		} {
			specs = this;
		}

		^specs;
	}

}
