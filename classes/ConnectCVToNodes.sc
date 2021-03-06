/*
Utilities to connect ControlValues to Node controls, Control Buses, and Node Proxys.

Array-connect(connectFunc, disconnectFunc)

DEACTIVATED: The following methods neither seem to be used in CV (or one of its
subclasses) nor Conductor. So it it should be safe to deactivate them for now. The
methods implement interfaces strongly tied to Classes like Bus, Synth, Node, NodeProxy,
Event. If they turn out to be useful or add some convenience they may get reactivated
or redesigned.


Array-connectToBus	(server, index) - deactivated
the array consists of SimpleNumbers and ControlValues

Array-connectToNode (server, nodeID) - deactivated
Array-connectToNodeProxy (nodeProxy) - deactivated
the array consists of labels alternating with a SimpleNumber or ControlValues


Bus-setControls( arrayOfValuesAndControlValues) - deactivated
Node-setControls( [name, (control)Value, name, (control)Value...]) - deactivated

NodeProxy:-setControls is the only method not to be deactivated as it's used in
NodeProxyPlayer within Conductor
NodeProxy-setControls( [name, (control)Value, name, (control)Value...])
'setControls' acts like 'set', but replaces ControlValues with their
values in the array and creates the needed synchronization logic (see below).

implementation:
A SimpleController dependant on the ControlValue relays changes to the NC, CB, or NP.
OSCresponders are used to remove the SimpleController when the NC, CB or NP is freed.
This is implemented with a dummy OSC message when freeing a Control Bus.

*/

+Array {

	/*	parameters array items are either:
	1: [label, cv] or
	2: [label [cv, expr]] or
	3: [label [cvArray, expr]] or
	4: [label [

	buildCVConnections iterates an argument array, doing the right thing for each case.
	It is passed a function that

	*/

	connect { |view|
		CV.viewDictionary[view.class].new(this, view);
		// for some reason RangeSlider needs a little nudge
		// in order to sync properly
		// Also, if lo is a higher than hi
		// lo and hi seem to switch internally
		// ... shouldn't matter if set vie GUI slider
		if (view.class === RangeSlider) {
			view.activeLo_(0);
			view.activeHi_(1);
		}
	}

	disconnect { |view|
		CVSync.value(view);
	}

	// buildCVConnections { |connectFunc, disconnectFuncBuilder|
	// 	var parameters, cvLinks;
	// 	parameters = this.copy.clump(2);
	// 	cvLinks = Array(parameters.size);
	// 	parameters = parameters.collect { |p|
	// 		var label, cv, expr;
	// 		#label, cv = p;
	// 		if (cv.isKindOf(Function)) { cv = cv.value };
	// 		#cv, expr = cv.asArray;
	// 		[cv, expr].postln;
	// 		expr = expr ? cv;
	// 		if (expr.isNumber.not) {
	// 			cv.asArray.do { |cv|
	// 				cvLinks.add(cv.addController({ connectFunc.value(label, expr) }))
	// 			}
	// 		};
	// 		[label,expr.value]
	// 	};
	//
	// 	if (cvLinks.size > 0) { disconnectFuncBuilder.value(cvLinks) };
	// 	^parameters;
	// }
	//
	// connectToNode { |server, nodeID|
	// 	^this.buildCVConnections(
	// 		{ |label, expr|
	// 			var val, group, addAction, msg;
	// 			if (label != 'group') {
	// 				val = expr.value.asArray;
	// 				msg = ['/n_setn', nodeID, label, val.size] ++ val;
	// 			} {
	// 				val = expr.asArray;
	// 				group = val[0].value;
	// 				addAction = val[1].value ? 0;
	// 				msg = switch (addAction,
	// 					0, { ['/g_head', group, nodeID] },
	// 					1, { ['/g_tail', group, nodeID] },
	// 					2, { ['/n_before', nodeID, group ] },
	// 					3, { ['/n_after', nodeID, group ] }
	// 				);
	// 			};
	// 			server.sendBundle(server.latency, msg);
	// 		}, { |cvLinks|
	// 			// OSCpathResponder(server.addr, ["/n_end", nodeID],
	// 			// 	{ |time, resp, msg| cvLinks.do(_.remove); resp.remove }
	// 			// ).add;
	// 			OSCFunc({ |msg, time, addr, recvPort|
	// 				cvLinks.do(_.remove);
	// 			}, "/n_end", server.addr, argTemplate: [nodeID]).oneShot;
	// 		}
	// 	).flatten(1);
	// }
	//
	// buildUnlabeledCVConnections { |connectFunc, disconnectFunc|
	// 	var parameters, cvLinks;
	// 	parameters = this.copy;
	// 	cvLinks = Array(parameters.size);
	// 	parameters = parameters.collect { |cv, i|
	// 		var label, expr;
	// 		label = i;
	// 		#cv, expr = cv.asArray;
	// 		expr = expr ? cv;
	// 		if (expr.isNumber.not) {
	// 			cv.asArray.do { |cv|
	// 				cvLinks.add(cv.addController({ connectFunc.value(label, expr) }))
	// 			}
	// 		};
	// 		expr.value
	// 	};
	//
	// 	if (cvLinks.size > 0) { disconnectFunc.value(cvLinks) };
	// 	^parameters;
	// }
	//
	// connectToBus { |server, busIndex|
	// 	^this.buildUnlabeledCVConnections(
	// 		{ |label, expr|
	// 			var val;
	// 			val = expr.value.asArray;
	// 			server.sendBundle(server.latency,['/c_setn', busIndex + label, val.size] ++ val);
	// 		}, { |cvLinks|
	// 			// OSCpathResponder(server.addr, ["/c_end", busIndex],
	// 			// 	{ |time, resp, msg|
	// 			// 		cvLinks.do(_.remove);
	// 			// 		resp.remove;
	// 			// 	}
	// 			// ).add;
	// 			OSCFunc({ |msg, time, addr, recvPort|
	// 				cvLinks.do(_.remove);
	// 			}, "/c_end", server.addr, argTemplate: [busIndex]).oneShot;
	// 		}
	// 	);
	// }
	//
	// connectToBuffer { |server, bufferNumber|
	// 	^this.buildUnlabeledCVConnections(
	// 		{ |label, expr|
	// 			var val;
	// 			val = expr.value.asArray;
	// 			server.sendBundle(server.latency,['/b_setn', bufferNumber, val.size] ++ val);
	// 		}, { |cvLinks|
	// 			// OSCpathResponder(server.addr, ["/b_free", bufferNumber],
	// 			// 	{ |time, resp, msg|
	// 			// 		cvLinks.do(_.remove);
	// 			// 		resp.remove;
	// 			// 	}
	// 			// ).add;
	// 			OSCFunc({ |msg, time, addr, recvPort|
	// 				cvLinks.do(_.remove);
	// 			}, "/b_free", server.addr, argTemplate: [bufferNumber]).oneShot;
	// 		}
	// 	)
	// }
	//
	//
	// }
	//
	//
	// +Node {
	// setControls { | args |
	// 	server.sendBundle(server.latency, ["/n_set", nodeID] ++ args.connectToNode(server,nodeID))
	// }
	// }
	//
	// +Synth {
	// *controls { arg defName, args, target, addAction=\addToHead;
	// 	var synth, server, addNum, inTarget;
	// 	inTarget = target.asTarget;
	// 	server = inTarget.server;
	// 	addNum = addActions[addAction];
	// 	synth = this.basicNew(defName, server);
	// 	if((addNum < 2), { synth.group = inTarget; }, { synth.group = inTarget.group; });
	// 	server.sendMsg(9, defName, synth.nodeID, addNum, inTarget.nodeID,
	// 		*(args.connectToNode(synth.server, synth.nodeID) )
	// 	); //"s_new"
	// 	^synth
	//
	// }
}


// still active but not unit-tested
+NodeProxy {
	setControls { |args|
		args.buildCVConnections(
			{ |label, expr| this.set(label, expr.value)},
			{ |cvLinks|
				OSCpathResponder(group.server.addr, ["/n_end", group.nodeID],
					{ |time, resp, msg|
						cvLinks.do(_.remove);
						resp.remove;
					}
				).add;
				// OSCFunc({ |msg, time, addr, recvPort|
				// 	cvLinks.do(_.remove);
				// }, "/n_end", group.server.addr, argTemplate: [group.nodeID]).oneShot;
			}
		).do { |pair| this.set(pair[0], pair[1]) }
	}
}

	// +Event {
	// 	setControls { |args|
	// 		args.buildCVConnections(
	// 			{ |label, expr| this.put(label, expr.value)},
	// 			{ |cvLinks| this.put(\cvLinks, cvLinks) }
	// 		).do { |pair| this.put(pair[0], pair[1]) }
	// 	}
	// }

	// +Bus {
	//
	// 	*controls { |args, server|
	// 		var bus, size;
	// 		size = args.size;
	// 		bus = Bus.control(server, size);
	// 		bus.setControls(args);
	// 		^bus;
	// 	}
	//
	// 	setControls { |arrayOfControlValues|
	// 		server.sendBundle(
	// 			server.latency, ["/c_setn", index] ++ arrayOfControlValues.size ++ arrayOfControlValues.connectToBus(server,index)
	// 		)
	// 	}
	//
	// 	free {
	// 		if (index.isNil) {
	// 			(this.asString + " has already been freed").warn;
	// 			^this
	// 		};
	// 		if (rate == \audio) {
	// 			server.audioBusAllocator.free(index);
	// 		} {
	// 			server.removeBusLinks(index);
	// 			server.controlBusAllocator.free(index);
	// 		};
	// 		index = nil;
	// 		numChannels = nil;
	// 	}
	// }
	//
	// +Server {
	// 	removeBusLinks { |busIndex|
	// 		OSCresponder.respond(0, this.addr, ["/c_end", busIndex]);
	// 	}
	//
	// 	removeControlLinks { |nodeID|
	// 		OSCresponder.respond(0, this.addr, ["/n_end", nodeID]);
	// 	}
	// }

