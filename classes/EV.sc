EV : CV {
	var <>duration, <>warp, <>rateWarp;

	*new { |spec = \unipolar, default|
		^super.new(spec, default ? Env([0, 1, 0], [0.5, 0.5], [0, 0]))
	}

	input_ { |env|
		env.times = env.times * duration / env.times.sum;
		env.levels = spec.map(env.levels);
		value = env;
	}

	input {
		^this.value;
	}

	value_ { |env|
		duration = env.times.sum;
		env.levels = spec.constrain(env.levels);
		env.curves = [env.times, env.curves].flop.flop[1];
		value = env;
		this.changed(\synch, this);
	}

	evToView { |view|
		var t, l, c, env;
		env = value;

		t = env.times.asFloat.addFirst(0).integrate;	// convert deltas to absolute times
		t = t/duration;							// rescale to [0,1]
		l = spec.unmap(env.levels);
		view.value = [t, l];
		view.curves = env.curves;
	}

	viewToEV { |view|
		var t, l, c, env;
		env = value;
		#t, l = view.value;
		env.levels = spec.map(l);
		env.times = t.differentiate[1..] * duration;
		this.changed(\synch, this);
	}

	connect { |view| ^EVSync(this, view) }

	asOSCArgEmbeddedArray { |array|
		var retVal = value.copy;
		if (rateWarp.notNil) {
			retVal.levels = retVal.levels.midiratio;
			retVal = retVal.rateWarp;
		};
		^retVal.asArray.asOSCArgEmbeddedArray(array);
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<<* [this.spec, value] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}	