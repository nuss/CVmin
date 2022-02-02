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

TestCVSyncProperties : UnitTest {

	test_new {
		var cv1 = CV.new;
		var cv2 = CV.new;
		var view = Slider2D.new;
		var sync = CVSyncProperties([cv1, cv2], view, #[xValue, yValue]);
		this.assertEquals(sync.view, view, "The CVSyncProperties instance var view should hold the view (a Slider2D)");
		this.assertEquals(sync.links.collect(_.class), [CVSyncProperty, CVSyncProperty], "The CVSyncProperties instance var links should hold an array of two CVSyncProperty instances");
		this.assertEquals(sync.links[0].cv, cv1, "The CVSyncProperties' instance var links at position 0, a CVSyncProperty, should hold the CV cv1 in its instance var cv");
		this.assertEquals(sync.links[1].cv, cv2, "The CVSyncProperties' instance var links at position 1, a CVSyncProperty, should hold the CV cv2 in its instance var cv");
		this.assertEquals(sync.links[0].property, \xValue, "The CVSyncProperty at position 0 in links should hold the property \xValue");
		this.assertEquals(sync.links[1].property, \yValue, "The CVSyncProperty at position 0 in links should hold the property \yValue");
		this.assertEquals(CVSync.all[view], sync, "CVSync.all at key identical to the view should hold the CVSyncProperties instance");
		view.close;
		this.wait({ CVSync.all[view].isNil }, "Dependants were not removed within 0.2 seconds", 0.2);
		this.assert(CVSync.all[view].isNil, "Upon closing the connected Slider2D the CVSyncProperties instance held in CVSync.all at key identical to the Slider2D instance should be removed");
	}

}

TestCVSyncProps : UnitTest {

	test_new {
		var cvs = [CV.new, CV.new];
		var view = Slider2D.new;
		var sync, syncProps = CVSyncProps(#[xValue, yValue]);
		this.assertEquals(syncProps.props, #[xValue, yValue], "The instance var props should be an array [\xValue, yValue]");
		sync = syncProps.new(cvs, view);
		this.assertEquals(sync.class, CVSyncProperties, "Calling syncProps.new with arguments cv, view should have created a new CVSyncProperties instance");
	}

}

TestSVSync : UnitTest {

	test_new {
		var sv = SV(#[aaa, bbb, ccc], 1);
		var view = ListView.new;
		var sync = SVSync(sv, view);
		this.assertEquals(CVSync.all[view], sync, "After instantiating a new SVSync CVSync.all should hold the SVSync at the key identical to the view");
		this.assertEquals(sv.item, \bbb, "The sv's item should be the item at the index denoted by argument default");
	}

	test_update {
		var sv = SV(#[aaa, bbb, ccc], 1);
		var view = ListView.new;
		var sync = SVSync(sv, view);
		sv.value_(2);
		sync.update(\synch);
		this.assertEquals(view.value, 2, "After setting the SV's value and calling update with argument \synch the view's value should have been set to the SV's value");
		sv.items_(#[sss, eee, vvv]);
		sync.update(\items);
		this.assertEquals(view.items, #[sss, eee, vvv], "After setting the SV's items and calling update with argument \items the views items should have been set to the SV's items");
	}

}

