TestEV : UnitTest {

	test_new {
		var ev = EV.new;
		this.assertEquals(ev.spec, ControlSpec(0, 1, \lin, 0, 0), "A new EV created with no parameters should return a ControlSpec(0, 1, \lin, 0, 0) on calling spec");
		this.assertEquals(ev.value, Env([0,1,0],[0.5,0.5], [0,0]), "A new EV created with no parameters should return an Env([0,1,0],[0.5,0.5], [0,0]) on calling value");
		ev = EV(default: Env([0, 5, 0], [0.5, 0.5], [0, 0]));
		this.assertEquals(ev.value.levels, [0, 1, 0], "An Ev created with a default Env whose levels exceed the EV's ControlSpec minval/maxval should get its levels constrained within minval/maxval");
	}

	test_input {
		var ev = EV.new;
		ev.input_(Env([0, 5, 5, 0], [0.2, 0.2, 0.2], [0, 0, 0]));
		this.assertEquals(ev.value.levels, [0, 1, 1, 0], "An Env passed in by calling input_ should have its levels constrained to the EV's ControlSpec minval/maxval");
		this.assertFloatEquals(ev.value.times.sum, 1, "An Env passed in by calling input_ should have its summed times normalized to 1");
	}

	test_value {
		var ev = EV.new;
		ev.value_(Env([0, 5, 0], [1, 1], \lin));
		this.assertEquals(ev.value.levels, [0, 1, 0], "An Env passed in by calling value_ should have its levels constrained to the EV's ControlSpec minval/maxval");
		this.assertEquals(ev.value.curves, #[lin, lin], "An Env with a single curve passed in by calling value_ should have its curves expanded to an array of the same size as its times");
	}

	test_connect {
		var ev = EV.new;
		var view = EnvelopeView.new;
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the EV to the EnvelopeView");
		ev.connect(view);
		this.assertEquals(CVSync.all[view].class, EVSync, "CVSync.all[view] should hold one EVSync after connecting the EV to the EnvelopeView");
		this.assertEquals(dependantsDictionary[ev].select { |d| d.class === EVSync }.size, 1, "The dependantsDictionary at our EV should include one instance of EVSync after connecting the EV to an EnvelopeView");
		ev.disconnect(view);
	}

	test_disconnect {
		var ev = EV.new;
		var view = EnvelopeView.new;
		ev.connect(view);
		ev.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the EV to the EnvelopeView");
		this.assertEquals(dependantsDictionary[ev].select { |d| d.class === EVSync }.size, 0, "The dependantsDictionary at our EV should include no instance of EVSync after disconnecting the EV from the EnvelopeView");
	}

	test_evToView {
		var ev = EV([0.1, 500, \exp].asSpec);
		var view = EnvelopeView.new;
		var t, l, env;
		ev.evToView(view);
		t = ev.value.times.asFloat.addFirst(0).integrate/ev.duration;
		l = ev.spec.unmap(ev.value.levels);
		this.assertArrayFloatEquals(view.value, [t, l], "The views's value should equal a composite array of the integrated envelope's times, inserted 0 at its first slot, divided by the EV's duration and the env's levels constrained by the spec's minval/maxval");
		// can't test for equality of view.curves and ev.value.curves as view.curves simply returns the EnvelopeView
	}

	test_viewToEV {
		var ev = EV.new;
		var view = EnvelopeView.new;
		var t, l, env = Env([0, 1, 1, 0], [1, 1, 1], \lin);
		view.setEnv(env);
		ev.viewToEV(view);
		#t, l = view.value;
		l = ev.spec.map(l);
		this.assertArrayFloatEquals(ev.value.levels, l, "The EV value's (an Env) levels should equal the view value's levels, mapped to the EV's spec");
		t = t.differentiate[1..] * ev.duration;
		this.assertArrayFloatEquals(ev.value.times, t, "The EV value's (an Env) times should equal the differentiated values of the view value's times from index 1 to the end, multiplie by the EV's duration");
	}

}