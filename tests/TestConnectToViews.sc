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
		// hmmm...
		0.1.wait;
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