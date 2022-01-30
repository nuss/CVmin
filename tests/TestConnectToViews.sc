TestCVSync : UnitTest {

	test_initClass {
		this.assert(CVSync.all.class == IdentityDictionary, "CVSync.all should be an IdentityDictionary");
	}

	test_new {
		var cv = CV.new;
		var sync, view = Slider.new;
		view.value_(1.0);
		sync = CVSync(cv, view);
		this.assert(view.action === sync, "The view's action should be identical to the CVSync");
		this.assertEquals(CVSync.all[view], sync, "CVSync.all should hold the CVSync instance at the key identical to the view");
		this.assertEquals(view.value, 0, "The view's value should have been set to the initial value of the CV (0.0)");
	}

	test_linkToCV {
		var cv = CV.new;
		var sync, view = Slider.new;
		sync = CVSync(cv, view);
		this.assertEquals(dependantsDictionary[cv].class, IdentitySet, "Creating a new CVSync with given arguments CV and view should create a new IdentitySet at a key identical to the CV");
		this.assertEquals(dependantsDictionary[cv].size,1, "The IdentitySet at key CV should contain one value");
		this.assert(dependantsDictionary[cv].includes(sync), "The IdentitySet at key CV should contain the CVSync");
	}

	test_linkToView {
		var cv = CV.new;
		var view = Slider.new;
		CVSync(cv, view);
		view.close;
		// needs a little time...
		this.wait({ CVSync.all[view].isNil }, "Dependants were not removed within 0.2 seconds", 0.2);
		this.assert(CVSync.all[view].isNil, "Upon closing a connected view the CVSync held in CVSync.all at key view should be removed");
	}

	test_update {
		var cv = CV.new;
		var view = Slider.new;
		var sync = CVSync(cv, view);
		cv.input_(0.5);
		sync.update(what: \synch);
		this.assertFloatEquals(view.value, 0.5, "The view's value should have been set to 0.5 on calling sync.update");
	}

	test_value {
		var cv = CV.new;
		var view = Slider.new;
		var sync = CVSync(cv, view);
		view.value_(0.5);
		sync.value;
		this.assertFloatEquals(cv.input, 0.5, "The CV's input should have been set to 0.5 upon calling sync.value");
	}

	test_remove {
		var cv = CV.new;
		var view = Slider.new;
		var sync = CVSync(cv, view);
		sync.remove;
		this.assert(dependantsDictionary[cv].isNil, "The dependandantsDictionary should hold have all keys identical to the CV removed after calling sync.remove");
	}

}

// methods update, value in CVSyncInput are identical to those in CVSync, so, no tests here

TestCVSyncValue : UnitTest {

	test_update {
		var cv = CV([0, 10].asSpec);
		var view = NumberBox.new;
		var sync = CVSyncValue(cv, view);
		cv.value = 5;
		sync.update(what: \synch);
		this.assertEquals(view.value, 5, "The view's value should have been set to 5");
	}

	test_value {
		var cv = CV([0, 10].asSpec);
		var view = NumberBox.new;
		var sync = CVSyncValue(cv, view);
		view.value_(5);
		sync.value;
		this.assertEquals(cv.value, 5, "The CV's value should have been set to 5");
	}

}

TestCVSyncMulti : UnitTest {

	test_linkToView {
		var cv = CV([0!5, 1!5].asSpec);
		var view = MultiSliderView.new;
		var sync = CVSyncMulti(cv, view);
		this.assertEquals(view.size, cv.value.size, "The MultiSliderView's size should equal the CV's value size");
		this.assertFloatEquals(view.indexThumbSize, view.bounds.width - 16 / cv.value.size, "The MultiSliderView's index thumb size should be its width - 16 pixels, divided by the number of sliders");
		this.assert(CVSync.all[view] === sync, "CVSync.all at key identical to view should hold the CVSyncMulti instance");
		view.close;
		this.wait({ CVSync.all[view].isNil }, "Dependants were not removed within 0.2 seconds", 0.2);
		this.assert(CVSync.all[view].isNil, "Upon closing the connected MultiSliderView the CVSyncMulti instance held in CVSync.all at key identical to the MultiSliderView instance should be removed");
	}

}

TestCVSyncProperty : UnitTest {

	test_new {
		var cv1 = CV.new;
		var cv2 = CV.new;
		var sync1, sync2, view = Slider2D.new;
		cv1.input_(0.3);
		sync1 = CVSyncProperty(cv1, view, \xValue);
		sync2 = CVSyncProperty(cv2, view, \yValue);
		this.assert(sync1.cv === cv1 and:{ sync2.cv === cv2 }, "After instantiating a new CVSyncProperty the instance's cv instance var should hold the given CV");
		this.assert(sync1.view === view and:{ sync2.view === view }, "After instantiating a new CVSyncProperty the instance's view instance var should hold the given view");
		this.assert(sync1.property === \xValue and:{ sync2.property === \yValue }, "After instantiating a new CVSyncProperty the instance's property instance var should hold the given property");
		this.assertEquals(view.getProperty(\xValue), 0.3, "The Slider2D should return the initial value 0.3 on calling getProperty(\xValue)");
	}

	test_update {
		var cv1 = CV.new;
		var cv2 = CV.new;
		var view = RangeSlider.new;
		var sync1 = CVSyncProperty(cv1, view, \loValue);
		var sync2 = CVSyncProperty(cv2, view, \hiValue);
		cv1.input_(0.5);
		// NOTE: RangeSlider flips its loValue/hiValue internally if loValue gets set to a higher value than hiValue is currently set to!
		this.wait({ view.getProperty(\hiValue) == 0.5 }, "The view's lo value hasn't been set to 0.5 after 0.2 seconds", 0.2);
		this.assertEquals(view.getProperty(\hiValue), 0.5, "After setting one of the CV's inputs the view's correlated property should be set to the CV's input");
	}

	test_value {
		var cv1 = CV.new;
		var cv2 = CV.new;
		var view = Slider2D.new;
		var sync1 = CVSyncProperty(cv1, view, \xValue);
		var sync2 = CVSyncProperty(cv2, view, \yValue);
		view.setProperty(\xValue, 0.4);
		sync1.value;
		this.assertEquals(cv1.value, 0.4, "After calling sync1.value cv1.input should have been set to 0.4");
	}

}