package com.serveroverload.recorder.customview


/**
 * The listener interface for receiving simpleSwipe events.
 * The class that is interested in processing a simpleSwipe
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's `addSimpleSwipeListener` method. When
 * the simpleSwipe event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SimpleSwipeEvent
`` */
class SimpleSwipeListener : SwipeLayout.SwipeListener {

    override fun onStartOpen(layout: SwipeLayout) {}

    override fun onOpen(layout: SwipeLayout) {}

    override fun onStartClose(layout: SwipeLayout) {}

    override fun onClose(layout: SwipeLayout) {}

    override fun onUpdate(layout: SwipeLayout, leftOffset: Int, topOffset: Int) {}

    override fun onHandRelease(layout: SwipeLayout, xvel: Float, yvel: Float) {}
}
