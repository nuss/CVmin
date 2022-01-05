TestCV : UnitTest {

	test_new {
		var cv = CV.new;
		cv.addController({});
		cv = CV.new;
		this.assert(dependantsDictionary[cv].isNil, "With the creation of a new CV there should be no entry for this CV in the dependantsDictionary");
		this.assertEquals(cv.spec, ControlSpec(0, 1, 'linear', 0.0, 0, ""), "The default CV ControlSpec should be ControlSpec(0, 1, 'linear', 0.0, 0, ""). Actual value: %\n".format(cv.spec));
		this.assertEquals(cv.value, 0, "The CV's value should be 0");
		cv = CV([0, 5].asSpec);
		this.assertEquals(cv.spec, ControlSpec(0, 5, 'linear', 0.0, 0, ""), "An Array, explicitely cast to a ControlSpec by calling asSpec on it, should return a new CV with a valid ControlSpec");
		cv = CV(value: 2);
		this.assertEquals(cv.value, 1.0, "The value of a new CV should be constrained by minval/maxval of its given ControlSpec");
		cv = CV(\freq);
		this.assertEquals(cv.spec, ControlSpec(20, 20000, 'exp', 0, 440, " Hz"), "A ControlSpec given as Symbol should compile to a ControlSpec");
		this.assertFloatEquals(cv.value, 440.0, "The CV's value should automatically have been set to the ControlSpec's default");
		cv = CV(\gfsuu);
		this.assertEquals(cv.spec, ControlSpec(0, 1, 'linear', 0.0, 0, ""), "An invalid spec Symbol should compile to a default \unipolar ControlSpec");
	}

	test_sp {
		var cv = CV.new.sp;
		this.assertEquals(cv.spec, ControlSpec(0, 0, \lin, 0, 0), "The ControlSpec for a CV created from method sp with no arguments given should be: ControlSpec(0, 0, \lin, 0, 0). Actual value: %\n".format(cv.spec));
		cv.sp(default: 56, hi: 100, step: 2, lo: 2, warp: \exp);
		this.assertEquals(cv.spec, ControlSpec(2, 100, \exp, 2, 56), "The ControlSpec for the CV should have been changed through arguments given in CV:-sp");
	}

	test_split {
		var cv = CV([[0, 0, 0], [5, 5, 5], \lin, 0, [2]].asSpec);
		var splitCV = cv.split;
		this.assertEquals(cv.value, [2.0, 2.0, 2.0], "The CV is expected to have a value of [2.0, 2.0, 2.0]");
		this.assertEquals(splitCV.collect(_.value), [2.0, 2.0, 2.0], "The split CV's values should equal 2.0 each");
		this.assertEquals(splitCV.collect(_.spec), [
			ControlSpec(0, 5, \lin, 0, 2),
			ControlSpec(0, 5, \lin, 0, 2),
			ControlSpec(0, 5, \lin, 0, 2)
		], "The split CV's ControlSpecs should result in 3 equal ControlsSpecs");
	}

	test_add_remove_controllers {
		var cv = CV([0, 5, \lin, 0, 0].asSpec);
		var ctrl = cv.addController({ |cv| });
		this.assertEquals(cv.numControllers, 1, "The CV should have 1 action added in a SimpleController");
		this.assertEquals(ctrl.class, SimpleController, "The CV's controller should be of class SimpleController");
		cv.addController({ |cv| });
		this.assertEquals(cv.numControllers, 2, "The CV should have 2 actions added in 2 SimpleControllers");
		cv.removeAllControllers;
		this.assertEquals(cv.numControllers, 0, "All actions and controllers should have been removed from the CV");
	}

	test_value {
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		cv.addController({ |cv| ~test = cv.value });
		this.assertEquals(cv.value, 2, "The CV's initial value should be 2");
		this.assert(~test.isNil, "~test should be uninitialized: %\n".format(~test));
		cv.value_(4.3);
		this.assertFloatEquals(cv.value, 4.3, "The CV's value should have been set to 4.3");
		this.assertFloatEquals(~test, 4.3, "The value of ~test, addressed in the CV's dependedent action, should be 4.3");
		cv.value_([1, 2, 3]);
		this.assertEquals(cv.value, [1, 2, 3], "Setting a CV's value to an array of numbers will multichannel-expand the CV");
		cv.removeAllControllers;
		this.assertEquals(cv.numControllers, 0, "The CV's dependents should have been removed");
		~test = nil;
	}

	test_input {
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		cv.addController({ |cv| ~test = cv.input });
		this.assertFloatEquals(cv.input, 2/5, "The CV's initial input should be 2");
		this.assert(~test.isNil, "~test should be uninitialized : %\n".format(~test));
		cv.input_(0.231);
		this.assertFloatEquals(cv.input, 0.231, "The CV's input should have been set to 0.231");
		this.assertFloatEquals(~test, 0.231, "The value of ~test, addressed in the CV's dependedent action, should be 0.231");
		cv.removeAllControllers;
		this.assertEquals(cv.numControllers, 0, "The CV's dependents should have been removed");
		~test = nil;
	}

	test_asInput {
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		var input = cv.asInput(4);
		this.assertFloatEquals(input, cv.spec.unmap(4), "Calling asInput on the CV should return its unmapped equivalent determined by the CV's ControlSpec")
	}

	test_default {
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		cv.default_(3);
		this.assertEquals(cv.value, 3, "Calling default_ on the CV should set its value");
		this.assertEquals(cv.spec.default, 3, "Calling default_ on the CV should set its ControlSpec's default");
		cv.default_(-9);
		this.assertEquals(cv.value, 0.0, "Calling default_ with a value outside the ControlSpec's constraints should set the value to the ControlSpec's minval or maxval");
		this.assertEquals(cv.spec.default, 3, "Calling default_ with a value outside the ControlSpec's constraints should leave the ControlSpec's default untouched")
	}

	test_next {
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		this.assertFloatEquals(cv.next, 2.0, "Calling next on a CV should return its value")
	}

	test_embedInStream {
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		var stream;
		// tests are running in a Routine themselves :\
		// this.assertException({ cv.embedInStream }, PrimitiveFailedError, "Trying to call embedInStream outside a Routine should throw a PrimitiveFailedError");
		stream = Routine { cv.embedInStream };
		this.assertFloatEquals(stream.next, 2.0, "embedInStream should yield the CV's value");
		cv.value_(3.421);
		this.assertEquals(stream.next, nil, "embedInStream can only yield once");
	}

	test_buildViewDictionary {
		this.assertEquals(CV.viewDictionary.class, IdentityDictionary, "Class CV's viewDictionary should be an IdentityDictionary");
		this.assert(CV.viewDictionary.notEmpty, "Class CV's viewDictionary should not be empty");
	}

	test_connect {
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		var view = Slider();
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput before connecting the CV to a Slider");
		cv.connect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncInput after connecting the CV to a Slider");
		cv.input_(0.1);
		this.assertFloatEquals(view.value, cv.input, "A slider connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(0.5);
		this.assertEquals(view.value, cv.input, "A CV connected to a Slider should have its value and input set accordingly upon setting the Slider's valueAction");
		cv.disconnect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput after disconnecting the CV from");
		view = NumberBox();
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncValue before connecting the CV to a NumberBox");
		cv.connect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncValue after connecting the CV to a NumberBox");
		cv.value_(4);
		this.assertFloatEquals(view.value, cv.value, "A NumberBox connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(3);
		this.assertEquals(view.value, cv.value, "A CV connected to a NumberBox should have its value and input set accordingly upon setting the NumberBox's valueAction");
		cv.disconnect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncValue after disconnecting the CV from the NumberBox");
		view = Knob();
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput before connecting the CV to a Knob");
		cv.connect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncInput after connecting the CV to a Knob");
		cv.input_(0.3);
		this.assertFloatEquals(view.value, cv.input, "A Knob connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(0.3);
		this.assertEquals(view.value, cv.input, "A CV connected to a Knob should have its value and input set accordingly upon setting the Knob's valueAction");
		cv.disconnect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput after disconnecting the CV from the Knob");
		view = Button().states_({|i| i+1}!5);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncValue before connecting the CV to a Button");
		cv.connect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncValue after connecting the CV to a Button");
		cv.value_(3);
		this.assertEquals(view.value, cv.value, "A Button connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(2);
		this.assertEquals(view.value, cv.value, "A CV connected to a Button should have its value and input set accordingly upon setting the Button's valueAction");
		cv.disconnect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncValue after disconnecting the CV from the Button");
		view = MultiSliderView();
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncMulti }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncMulti before connecting the CV to a MultiSliderView");
		// instead of simply setting a new spec create a new CV and make sure the dependantsDictionary is clean
		// MultiSliderViews prefer fresh CVs, why ever....
		cv = CV([0!3, 5!3].asSpec);
		cv.connect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncMulti }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncMulti after connecting the CV to a MultiSliderView");
		cv.input_(0.2!3);
		this.assertEquals(view.value, cv.input, "A MultiSliderView connected to a CV should have its value set accordingly upon setting the CV's value or input");
		// MultiSliderView needs mouseUp to finish
		view.valueAction_(0.1!3).mouseUp;
		this.assertEquals(cv.input, view.value, "A CV connected to a MultiSliderView should have its value and input set accordingly upon setting the MultiSliderView's valueAction_(val).mouseUp");
		cv.disconnect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncMulti }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncMulti after disconnecting the CV from the MultiSliderView");
		view = TextView();
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncText before connecting the CV to a TextView");
		cv.connect(view);
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncMulti after connecting the CV to a TextView");
		cv.value_([1, 2, 3, 4]);
		this.assertEquals(view.string.interpret, cv.value, "A TextView connected to a CV should have its string set accordingly upon setting the CV's value or input");
		// FIXME: What keyDown is needed?
		view.string_("[4, 3, 2, 1]");
		this.assertEquals(cv.value, view.string.interpret, "A CV connected to a TextView should have its value and input set accordingly upon setting the TextView's string_(stringval).keyUp");
	}

}