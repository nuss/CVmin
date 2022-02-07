TestExtControlSpec : UnitTest {

	test_size {
		var spec = ControlSpec([0, 0, 0, 0, 0], [5, 5, 5], \lin, [0, 0, 0, 0, 0, 0, 0], [2, 2, 2, 2]);
		this.assertEquals(spec.size, 7, "ControlSpec:-size should return the maximum of minval.size, maxval.size, step.size, default.size");
		spec = ControlSpec.new;
		this.assertEquals(spec.size, 1, "By default a ControlSpec created with no parameters should return a size of 1");
	}

	test_split {
		var spec = ControlSpec([0, 1, 2, 3, 4], [5, 6, 7], \lin, [0, 0, 0, 0, 0, 0, 0], [2, 3, 4, 5]);
		var specs = spec.split;
		this.assertEquals(specs.size, spec.size, "Calling split on a multi-dimensional ControlSpec should return an Array of ControlSpecs of the same size as the original ControlSpec");
		this.assertEquals(specs[5], ControlSpec(0, 7, \lin, 0, 3), "If the Arrays for minval, maxval, step or default in the original multi-dimensional ControlSpec are smaller than the size of the original multi-dimensional ControlSpec these parameters should be representaions of the wrapped values in the original ControlSpec");
		spec = ControlSpec.new;
		this.assert(spec.split === spec, "A one-dimensional ControlSpec should simply return itself upon calling split on it");
	}

}