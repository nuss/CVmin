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

}