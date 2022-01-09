TestSV : UnitTest {

	test_new {
		var sv = SV(#[a, b, c], 1);
		this.assertEquals(sv.spec, ControlSpec(0, 2, \lin, 1, 1), "The ControlSpec of the newly created SV should equal ControlSpec(0, 0, \lin, 1, 1)");
		this.assertEquals(sv.value, 1, "The value of the newly created SV should equal 1");
	}

	test_default {
		var sv = SV(#[a, b, c]);
		sv.default_(2);
		this.assertEquals(sv.value, 2, "the SV's value should have been set to 2");
		this.assertEquals(sv.spec.default, 2, "The SV's spec default should have been set to 2");
		sv.default_(\b);
		this.assertEquals(sv.value, 1, "The SV's value should have been set to 1");
		this.assertEquals(sv.spec.default, 1, "The SV's spec default should have been set to 1");
	}

	test_items {
		var sv = SV.new;
		this.assertEquals(sv.spec, ControlSpec(0, 0, \lin, 1, 0), "Creating an SV without items should create a ControlSpec(0, 0, \lin, 1, 0) for the SV");
		this.assertEquals(sv.items, [\nil], "Creating an SV without items should create a new Array with one item: ['nil']");
		sv.items_(#[a, b, c]);
		this.assertEquals(sv.items, [\a, \b, \c], "The SV's items should have been set to ['a', 'b', 'c']");
		this.assertEquals(sv.spec, ControlSpec(0, 2, \lin, 1, 0), "The SV's spec should have been set to COntrolSpec(0, 2, \lin, 1, 0)");
	}

	test_item {
		var sv = SV(#[a, b, c]);
		this.assertEquals(sv.item, \a, "SV:-item should return the item at the current value: 'a' at value 0");
		sv.item_(\c);
		this.assertEquals(sv.value, 2, "SV:-item_ with argument 'c' should have set value to 2");
	}

	test_getIndex {
		var sv = SV(#[a, b, c]);
		this.assertEquals(sv.getIndex(\b), 1, "SV:-getIndex for item 'b' should have returned 1");
	}

	test_sp {
		var sv = SV.new.sp(1, #[a, b, c]);
		this.assertEquals(sv.spec, ControlSpec(0, 2, \lin, 1, 1), "SV.new.sp(1, #[a, b, c]) should have created an SV with a ControlSpec(0, 2, \lin, 1, 1)");
		this.assertEquals(sv.value, 1, "sv.newsp(1, #[a, b, c]) should have created an SV with value 1");
	}

	test_next {
		var sv = SV.new;
		this.assertEquals(sv.next, \nil, "An SV created without items should return 'nil' on calling next");
		sv.items_(#[a, b, c]).value_(1);
		this.assertEquals(sv.next, \b, "After adding items #[a, b, c] and setting value to 1 the SV should return 'b' on calling next");
	}

}