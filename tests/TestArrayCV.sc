TestArrayCV : UnitTest {

	test_connect {
		var cv1 = CV.new;
		var cv2 = CV.new;
		var view = RangeSlider.new;
		[cv1, cv2].connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncProperties, "After connecting an array of two CVs to a RangeSlider CVSync.all should hold an instance of CVSyncProperties at the key identical to the RangeSlider instance");
		this.assertEquals(dependantsDictionary[cv1].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at cv1 should include one instance of CVSyncProperty after connecting the CV to the RangeSlider");
		this.assertEquals(dependantsDictionary[cv2].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at cv2 should include one instance of CVSyncProperty after connecting the CV to the RangeSlider");
		cv1.input_(0.5);
		cv2.input_(0.0);
		this.assertFloatEquals(view.hi, 0.5, "After calling cv1.input_(0.5) and cv2.input_(0.0) the view's hi value should be 0.5 as hi and low are internally switched if lo is hiegher than lo");
		[cv1, cv2].disconnect(view);
		view = Slider2D.new;
		[cv1, cv2].connect(view);
		this.assertEquals(CVSync.all[view].class, CVSyncProperties, "After connecting an array of two CVs to a RangeSlider CVSync.all should hold an instance of CVSyncProperties at the key identical to the RangeSlider instance");
		this.assertEquals(dependantsDictionary[cv1].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at cv1 should include one instance of CVSyncProperty after connecting the CV to the RangeSlider");
		this.assertEquals(dependantsDictionary[cv2].select { |d| d.class === CVSyncProperty }.size, 1, "The dependantsDictionary at cv2 should include one instance of CVSyncProperty after connecting the CV to the RangeSlider");
		cv1.input_(0.5);
		cv2.input_(0.2);
		this.assertFloatEquals(view.x, 0.5, "After calling cv1.input_(0.5) view's x value should be 0.5");
		this.assertFloatEquals(view.y, 0.2, "After calling cv2.input_(0.2) view's x value should be 0.2");
		[cv1, cv2].disconnect(view);
	}

	test_disconnect {
		var cv1 = CV.new;
		var cv2 = CV.new;
		var view = RangeSlider.new;
		[cv1, cv2].connect(view);
		[cv1, cv2].disconnect(view);
		this.assert(CVSync.all[view].isNil, "CVSync.all should hold no reference under view as key after disconnecting the CV from the RangeSlider");
		this.assertEquals(dependantsDictionary[cv1].select { |d| d.class === CVSyncProperty }.size, 0, "The dependantsDictionary at cv1 should include no instance of CVSyncProperty after disconnecting the CV from the RangeSlider");
		this.assertEquals(dependantsDictionary[cv2].select { |d| d.class === CVSyncProperty }.size, 0, "The dependantsDictionary at cv2 should include no instance of CVSyncProperty after disconnecting the CV from the RangeSlider");
	}

}