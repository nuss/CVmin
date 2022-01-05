/*
 A CV models a value constrained by a ControlSpec. The value can be a single Float or an array of Floats.

 Whenever the CV's value changes, it sends a changed message labeled 'synch'.  This way dependants
 (such as GUI objects or server value) can be updated with SimpleControllers.  The method
 		aCV-addController(function)
 creates such a connection.

 A CV's value can be read with the 'value' message.
 CV can also be used as a Pattern (in Pbind) or in combination with other Streams.
*/

CV : Stream {
	classvar <>viewDictionary;

	var <>spec, <value;

	*initClass {
		StartUp.add ({ CV.buildViewDictionary })
	}

	*new { |spec = \unipolar, value|
		// implicit fallback spec generation:
		// if an invalid Symbol is given for spec
		// it will return nil on calling asSpec on it.
		// calling nil.asSpec will simply return the default
		// \unipolar spec
		^super.newCopyArgs(spec.asSpec.asSpec).init(value);
	}

	init { |value|
		// clean up, if necessary
		this.value_(value ? this.spec.default);
	}

	// SimpleControllers are easy to add and remove
	// however, controllers remain 'hidden' to the users
	// we're taking a shortcut here to Object's dependantsDictionary
	// that way a CV doesn't need its own list for bookkeeping
	numControllers {
		var allControllers, cvDependants = dependantsDictionary[this];
		cvDependants !? {
			allControllers = cvDependants.select { |dep| dep.class === SimpleController };
			if (allControllers.isEmpty) { ^0 } {
				^allControllers.size;
			}
		};
		^0
	}

	addController { |function|
		^SimpleController(this).put(\synch, function);
	}

	removeAllControllers {
		var allControllers, cvDependants = dependantsDictionary[this];
		cvDependants !? {
			allControllers = cvDependants.select { |dep| dep.class === SimpleController };
			if (allControllers.size < cvDependants.size) {
				allControllers.do { |c| cvDependants.remove(c) }
			} {
				dependantsDictionary.removeAt(this)
			}
		}
	}


	// reading and writing the CV
	value_ { |val|
		value = this.spec.constrain(val);
		this.changed(\synch, this);
	}

	input_ { |in| this.value_(this.spec.map(in)) }
	input { ^this.spec.unmap(value) }
	asInput { |val| ^this.spec.unmap(val) }

	default_ { |val|
		var min = min(this.spec.minval, this.spec.maxval);
		var max = max(this.spec.minval, this.spec.maxval);
		if ((min <= val).and(val <= max)) { this.spec.default_(this.spec.constrain(val)) };
		this.value_(val);
	}

	sp { |default = 0, lo = 0, hi = 0, step = 0, warp = 'lin'|
		this.spec = ControlSpec(lo,hi, warp, step, default);
	}

	db {
		this.spec = ControlSpec(-100, 20, \lin, 1, 0);
	}

	// split turns a multi-valued CV into an array of single-valued CV's
	split {
		var specs;

		if (this.spec.size > 1) {
			specs = this.spec.split;
			^value.collect { |v, i| CV(specs[i], v) }
		}
	}

	// Stream and Pattern support
	next { ^value }

	reset {}

	embedInStream { ^value.yield }

	*buildViewDictionary {
		var connectDictionary = (
			numberBox:		CVSyncValue,
			slider:			CVSyncInput,
			multiSliderView:CVSyncMulti,
			popUpMenu:		SVSync,
			listView:		SVSync,
			knob:			CVSyncInput,
			button:			CVSyncValue,
			textView:		CVSyncText,
			textField:		CVSyncText,
			staticText:		CVSyncText,
		);

		connectDictionary.rangeSlider = CVSyncProps(#[loValue, hiValue]);
		connectDictionary.slider2D = CVSyncProps(#[xValue, yValue]);

		this.viewDictionary = IdentityDictionary.new;

		GUI.schemes.do { | gui|
			var class;
			#[
				numberBox, slider, rangeSlider, slider2D, multiSliderView,
				popUpMenu, listView, knob, button, textView, textField, staticText
			].collect { |name|
				if ((class = gui.perform(name)).notNil) {
					class = class.superclass;
					this.viewDictionary.put(class, connectDictionary.at(name))
				}
			}
		};
	}

	connect { |view|
		this.class.viewDictionary[view.class].new(this, view) ;
	}

	// again we take a shortcut to the dependantsDictionary
	disconnect { |view|
		var cvsyncs = dependantsDictionary[this].select { |d| d.class !== SimpleController };
		var selfsync = cvsyncs.detect { |s| s.view === view };
		dependantsDictionary[this].remove(selfsync);
		if (dependantsDictionary[this].isEmpty) {
			dependantsDictionary.removeAt(this)
		}
	}

	asControlInput { ^value.asControlInput }

	asOSCArgEmbeddedArray { | array|
		^value.asOSCArgEmbeddedArray(array)
	}

	indexedBy { | key |
		^Pfunc{ | ev | value.at(ev[key] ) }
	}

	windex { | key |
		^Pfunc{ | ev |
			value.asArray.normalizeSum.windex
		}
	}

	at { | index | ^value.at(index) }

	put { | index, val |
		value = value.putt(index, val)
	}

	size { ^value.size }

}
