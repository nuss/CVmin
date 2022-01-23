
CVSync {
	classvar <>all;
	var <>cv, <>view;

	*initClass { all = IdentityDictionary.new }

	*new { |cv, view| ^super.newCopyArgs(cv, view).init }

	init {
		this.linkToCV;
		this.linkToView;
		this.update(cv, \synch);
	}

	linkToCV {
		cv.addDependant(this); 		 	// when CV changes CVsync:update is called
	}

	linkToView {
		view.action = this;
		all.put(view, this);
		// will call value(view) on close
		view.onClose = this.class;
	}

	update { |changer, what ...moreArgs|	// called when CV changes
		switch( what,
			\synch, { defer { view.value = cv.input }; }
		);
	}

	value { cv.input = view.value }		// called when view changes

	// called onClose and
	// in CV:-disconnect
	*value { |view|
		all[view].do(_.remove);
		// NOTE: it's not sufficient to remove all CVSyncs connected to the CV
		// setting CVs to nil will automatically remove them from the dependantsDictionary!
		all[view] = nil;
	}

	remove { cv.removeDependant(this) }
}

CVSyncInput : CVSync {
	update { |changer, what ...moreArgs|	// called when CV changes
		switch( what,
			\synch, { defer { view.value = cv.input }; }
		);
	}

	value { cv.input = view.value }		// called when view changes
}

CVSyncValue : CVSync {				// used by NumberBox

	update { |changer, what ...moreArgs|
		switch( what,
			\synch, { defer { view.value = cv.value }; }
		);
	}

	value { cv.value = view.value }

}

CVSyncMulti : CVSync {

	linkToView {
		view.thumbSize = (view.bounds.width - 16 / cv.value.size);
		view.isFilled_(true);
		view.elasticMode_(true);
		view.xOffset = 0;
		view.valueThumbSize = 1;
		view.mouseUpAction = this;

		// CVSync.all[view] = CVSync.all[view].add(this);
		all.put(view, this);
		view.onClose = CVSync;
	}
}

// one view, many CV's.
// CVSyncProperty links one CV to a property of a view
// CVSyncProperties links the view to its CV's

CVSyncProperty : CVSync {
	var <>property;

	*new { |cv, view, property| ^super.newCopyArgs(cv, view, property).init }

	update { |changer, what ...moreArgs|
		switch( what,
			\synch, { defer { view.setProperty(property, cv.input) }; }
		);
	}

	value { cv.input = view.getProperty(property) }

	init {
		this.linkToCV;
		this.update(cv, \synch);
	}

}


CVSyncProperties : CVSync {
	var <>links, <>view;

	*new { |cvs, view, properties|
		^super.new(cvs, view)
			.view_(view)
			.links_(properties.collect { |p, i| CVSyncProperty(cvs[i], view, p) })
			.init
	}

 	init {
		this.linkToView;
	}

	value { links.do(_.value) }
	remove { links.do(_.remove) }

}

CVSyncProps {
	var <>props;
	*new { |props| ^super.newCopyArgs(props) }
	new { |cv, view| ^CVSyncProperties(cv, view, props) }
}

SVSync : CVSyncValue {
	init {
		this.update(cv, \items);
		super.init;
	}

	update { |changer, what ...moreArgs|
		switch( what,
			\synch, { defer { view.value = cv.value }; },
			\items, { defer { view.items = cv.items }; }
		);
	}

}

EVSync : CVSync {

	linkToView {
		view.action = this;
		all.put(view, this);
		view.onClose = CVSync
	}

	update { |changer, what ...moreArgs|	// called when CV changes
		switch( what,
			\synch, { defer { cv.evToView(view) } }
		);
	}

	value { cv.viewToEV(view) }		// called when view changes

}

// use TextFields, TextViews and StaticTexts in CVs
// pre-condition: texts must compile to arrays of numbers
CVSyncText : CVSync {
	classvar <>valRound=0.01;

	update { |changer, what ... moreArgs|
		switch( what,
			\synch, { defer { view.string = cv.value.collect(_.round(valRound)).asCompileString } }
		);
	}

	value {
		var arr = view.string.interpret;
		if (arr.isKindOf(SequenceableCollection) and:{
			arr.flat.every(_.isNumber)
		}, {
			cv.value = arr.flat;
		})
	}

}

