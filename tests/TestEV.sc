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

}