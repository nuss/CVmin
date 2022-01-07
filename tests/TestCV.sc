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
		var cv1, cv2;
		var cv = CV([0, 5, \lin, 0, 2].asSpec);
		var view = Slider();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the Slider");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput before connecting the CV to a Slider");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncInput, "CVSync.all[view] should hold one CVSyncInput after connecting the Slider to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncInput after connecting the CV to a Slider");
		cv.input_(0.1);
		this.assertFloatEquals(view.value, cv.input, "A slider connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(0.5);
		this.assertEquals(view.value, cv.input, "A CV connected to a Slider should have its value and input set accordingly upon setting the Slider's valueAction");
		cv.disconnect(view);
		view = NumberBox();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the NumberBox");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncValue before connecting the CV to a NumberBox");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncValue, "CVSync.all[view] should hold one CVSyncValue after connecting the NumberBox to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncValue after connecting the CV to a NumberBox");
		cv.value_(4);
		this.assertFloatEquals(view.value, cv.value, "A NumberBox connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(3);
		this.assertEquals(view.value, cv.value, "A CV connected to a NumberBox should have its value and input set accordingly upon setting the NumberBox's valueAction");
		cv.disconnect(view);
		view = Knob();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the Knob");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput before connecting the CV to a Knob");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncInput, "CVSync.all[view] should hold one CVSyncInput after connecting the Knob to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncInput after connecting the CV to a Knob");
		cv.input_(0.3);
		this.assertFloatEquals(view.value, cv.input, "A Knob connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(0.3);
		this.assertEquals(view.value, cv.input, "A CV connected to a Knob should have its value and input set accordingly upon setting the Knob's valueAction");
		cv.disconnect(view);
		view = Button().states_({|i| i+1}!5);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the Button");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncValue before connecting the CV to a Button");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncValue, "CVSync.all[view] should hold one CVSyncValue after connecting the Button to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncValue after connecting the CV to a Button");
		cv.value_(3);
		this.assertEquals(view.value, cv.value, "A Button connected to a CV should have its value set accordingly upon setting the CV's value or input");
		view.valueAction_(2);
		this.assertEquals(view.value, cv.value, "A CV connected to a Button should have its value and input set accordingly upon setting the Button's valueAction");
		cv.disconnect(view);
		view = MultiSliderView();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the MultiSliderView");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncMulti }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncMulti before connecting the CV to a MultiSliderView");
		// instead of simply setting a new spec create a new CV and make sure the dependantsDictionary is clean
		// MultiSliderViews prefer fresh CVs, why ever....
		cv = CV([0!3, 5!3].asSpec);
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncMulti, "CVSync.all[view] should hold one CVSyncMulti after connecting the MultiSliderView to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncMulti }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncMulti after connecting the CV to a MultiSliderView");
		cv.input_(0.2!3);
		this.assertEquals(view.value, cv.input, "A MultiSliderView connected to a CV should have its value set accordingly upon setting the CV's value or input");
		// MultiSliderView needs mouseUp to finish
		view.valueAction_(0.1!3).mouseUp;
		this.assertEquals(cv.input, view.value, "A CV connected to a MultiSliderView should have its value and input set accordingly upon setting the MultiSliderView's valueAction_(val).mouseUp");
		cv.disconnect(view);
		view = TextView();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the TextView");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncText before connecting the CV to a TextView");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncText, "CVSync.all[view] should hold one CVSyncText after connecting the TextView to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncMulti after connecting the CV to a TextView");
		cv.value_([1, 2, 3, 4]);
		this.assertEquals(view.string.interpret, cv.value, "A TextView connected to a CV should have its string set accordingly upon setting the CV's value or input");
		view.string_("[4, 3, 2, 1]").doAction;
		this.assertEquals(cv.value, view.string.interpret, "A CV connected to a TextView should have its value and input set accordingly upon setting the TextView's string_(stringval).doAction");
		cv.disconnect(view);
		view = TextField();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the TextField");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncText before connecting the CV to a TextField");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncText, "CVSync.all[view] should hold one CVSyncText after connecting the TextField to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncMulti after connecting the CV to a TextField");
		cv.value_([5, 3, 2]);
		this.assertEquals(view.string.interpret, cv.value, "A TextField connected to a CV should have its string set accordingly upon setting the CV's value or input");
		view.valueAction_("[1, 5, 4]");
		this.assertEquals(cv.value, view.string.interpret, "A CV connected to a TextField should have its value and input set accordingly upon setting the TextView's string_(stringval).valueAction");
		cv.disconnect(view);
		view = StaticText();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the CV to the StaticText");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncText before connecting the CV to a StaticText");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncText, "CVSync.all[view] should hold one CVSyncText after connecting the StaticText to the CV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 1, "The dependantsDictionary at our CV should include one instance of CVSyncMulti after connecting the CV to a StaticText");
		cv.value_([5, 3, 2]);
		this.assertEquals(view.string.interpret, cv.value, "A StaticText connected to a CV should have its string set accordingly upon setting the CV's value or input");
		cv.disconnect(view);
		#cv1, cv2 = CV.new!2;
		view = RangeSlider();
		[cv1, cv2].connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncProperties, "CVSync.all[view] should hold one CVSyncProperties after connecting the RangeSlider to CV 1 and 2");
		this.assertEquals(dependantsDictionary[cv1].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at our CV 1 should include one instance of CVSyncMulti after connecting the CV to a RangeSlider");
		this.assertEquals(dependantsDictionary[cv2].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at our CV 2 should include one instance of CVSyncMulti after connecting the CV to a RangeSlider");
		cv1.value_(0.125);
		this.assertFloatEquals(view.lo, cv1.value, "The lo value of our RangeSlider should have been set to the value of CV 1");
		cv2.value_(0.3412);
		this.assertFloatEquals(view.hi, cv2.value, "The hi value of our RangeSlider should have been set to the value of CV 2");
		view.activeLo_(0.2);
		this.assertFloatEquals(cv1.value, view.lo, "The value of CV 1 should have been set to the lo value of the RangeSlider");
		view.activeHi_(0.9);
		this.assertFloatEquals(cv2.value, view.hi, "The value of CV 2 should have been set to the hi value of the RangeSlider");
		[cv1, cv2].disconnect(view);
		view = Slider2D();
		[cv1, cv2].connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncProperties, "CVSync.all[view] should hold one CVSyncProperties after connecting the Slider2D to CV 1 and 2");
		this.assertEquals(dependantsDictionary[cv1].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at our CV 1 should include one instance of CVSyncMulti after connecting the CV to a Slider2D");
		this.assertEquals(dependantsDictionary[cv2].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at our CV 2 should include one instance of CVSyncMulti after connecting the CV to a Slider2D");
		cv1.value_(0.8);
		this.assertFloatEquals(view.x, cv1.value, "The x value of our Slider2D should have been set to the value of CV 1");
		cv2.value_(0.1);
		this.assertFloatEquals(view.y, cv2.value, "The y value of our Slider2D should have been set to the value of CV 2");
		view.activex_(0.1);
		this.assertFloatEquals(cv1.value, view.x, "The value of CV 1 should have been set to the x value of the Slider2D");
		view.activey_(0.8);
		this.assertFloatEquals(cv2.value, view.y, "The value of CV 2 should have been set to the y value of the Slider2D");
		[cv1, cv2].disconnect(view);
		cv = SV(#[gfejhh, kjgfdjg, kjgkgd], 2);
		view = ListView();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the SV to the ListView");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, SVSync, "CVSync.all[view] should hold one SVSync after connecting the ListView to the SV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === SVSync }.size, 1, "The dependantsDictionary at our SV should include one instance of SVSync after connecting the SV to a ListView");
		this.assertEquals(view.items, #[gfejhh, kjgfdjg, kjgkgd], "The items in the ListView should equal the list passed in with SV.new");
		cv.value_(0);
		this.assertEquals(view.value, 0, "After setting the SV's value the ListView's value should be set to the SV's value as well");
		view.valueAction_(1);
		this.assertEquals(cv.value, view.value, "After setting the ListView's value the SV's value should have been set to the ListView's value");
		cv.disconnect(view);
		view = PopUpMenu();
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key before connecting the SV to the PopUpMenu");
		cv.connect(view);
		this.assertEquals(CVSync.all[view].class, SVSync, "CVSync.all[view] should hold one SVSync after connecting the PopUpMenu to the SV");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === SVSync }.size, 1, "The dependantsDictionary at our SV should include one instance of SVSync after connecting the SV to a PopUpMenu");
		this.assertEquals(view.items, #[gfejhh, kjgfdjg, kjgkgd], "The items in the ListView should equal the list passed in with SV.new");
		cv.value_(0);
		this.assertEquals(view.value, 0, "After setting the SV's value the PopUpMenu's value should be set to the SV's value as well");
		view.valueAction_(1);
		this.assertEquals(cv.value, view.value, "After setting the PopUpMenu's value the SV's value should have been set to the PopUpMenu's value");
		cv.disconnect(view);
	}

	test_disconnect {
		var cv1, cv2, cv = CV.new;
		var view = Slider();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the Slider");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput after disconnecting the CV from the Slider");
		view = NumberBox();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the NumberBox");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput after disconnecting the CV from the NumberBox");
		view = Knob();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the Knob");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncInput }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncInput after disconnecting the CV from the Knob");
		view = Button();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the Knob");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncValue }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncValue after disconnecting the CV from the Button");
		view = MultiSliderView();
		cv = CV([0!3, 5!3].asSpec);
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the MultiSliderView");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncMulti }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncMulti after disconnecting the CV from the MultiSliderView");
		view = TextView();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the TextView");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncText after disconnecting the CV from the TextView");
		view = TextField();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the TextField");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncText after disconnecting the CV from the TextField");
		view = StaticText();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the StaticText");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === CVSyncText }.size, 0, "The dependantsDictionary at our CV should include no instance of CVSyncText after disconnecting the CV from the StaticText");
		#cv1, cv2 = CV.new!2;
		view = RangeSlider();
		[cv1, cv2].connect(view);
		[cv1, cv2].disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV 1 and 2 from the RangeSlider");
		this.assertEquals(dependantsDictionary[cv1].select { |d| d.class === CVSyncProperty }.size, 0, "The dependantsDictionary at our CV 1 should include no instance of CVSyncProperty after disconnecting CV 1 from the RangeSlider");
		this.assertEquals(dependantsDictionary[cv2].select { |d| d.class === CVSyncProperty }.size, 0, "The dependantsDictionary at our CV 2 should include no instance of CVSyncProperty after disconnecting CV 2 from the RangeSlider");
		view = Slider2D();
		[cv1, cv2].connect(view);
		[cv1, cv2].disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV 1 and 2 from the Slider2D");
		this.assertEquals(dependantsDictionary[cv1].select { |d| d.class === CVSyncProperty }.size, 0, "The dependantsDictionary at our CV 1 should include no instance of CVSyncProperty after disconnecting CV 1 from the Slider2D");
		this.assertEquals(dependantsDictionary[cv2].select { |d| d.class === CVSyncProperty }.size, 0, "The dependantsDictionary at our CV 2 should include no instance of CVSyncProperty after disconnecting CV 2 from the Slider2D");
		cv = SV(#[gwrsd, ertfd], 0);
		view = ListView();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the ListView");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === SVSync }.size, 0, "The dependantsDictionary at our CV should include no instance of SVSync after disconnecting the CV from the ListView");
		view = PopUpMenu();
		cv.connect(view);
		cv.disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the PopUpMenu");
		this.assertEquals(dependantsDictionary[cv].select { |d| d.class === SVSync }.size, 0, "The dependantsDictionary at our CV should include no instance of SVSync after disconnecting the CV from the PopUpMenu");
	}

	test_asControlInput {
		var cv = CV.new;
		this.assertFloatEquals(cv.asControlInput, cv.value, "Calling asControlInput on a one-dimensional CV should return the CV's value");
	}

	test_asOSCArgEmbeddedArray {
		var cv = CV.new;
		this.assertEquals(cv.asOSCArgEmbeddedArray([1, 2, 3, 4]), [1, 2, 3, 4, 0], "Calling asOSCArgEmbeddedArray on the CV should return the given array appended by the CV's value");
		cv.value_([0.1, 0.3, 0.4]);
		this.assertEquals(cv.asOSCArgEmbeddedArray([1, 2, 3, 4]), [1, 2, 3, 4, $[, 0.1, 0.3, 0.4, $]], "Calling asOSCArgEmbeddedArray on a CV whose value is an Array should return the Array given in asOSCArgEmbeddedArray appended by the CV's value where value's opening and closing brackets are in included as Chars");
	}
}