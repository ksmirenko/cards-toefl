/*
 * Copyright 2012 The Android Open Source Project
 * Modifications copyright 2016 Kirill Smirenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ksmirenko.toeflcards.layout

import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.*

import io.github.ksmirenko.toeflcards.R
import kotlinx.android.synthetic.main.fragment_card.view.*

/**
 * Fragment that represents a single viewed card.
 */
class CardContainerFragment : Fragment() {
    companion object {
        // arguments
        val ARG_FRONT_CONTENT = "front"
        val ARG_BACK_CONTENT = "back"
        val ARG_IS_BACK_FIRST = "backfirst"

        val dummyCallbacks = DummyCallbacks()
    }

    private val HINT_PREFS_NAME = "ShouldShowHint"
    private val HINT_FRAGMENT_TAG = "HintFragment"

    private var isShowingBack = false
    private var isShowingHint = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_cards_container, container, false)
        val args = arguments
        isShowingBack = args.getBoolean(ARG_IS_BACK_FIRST, false)

        if (savedInstanceState == null) {
            // if launched for the first time, show "tap to flip" hint fragment
            val prefs = activity.getSharedPreferences(HINT_PREFS_NAME, 0)
            if (prefs.getBoolean(HINT_PREFS_NAME, true)) {
                showHintFragment()
                prefs.edit().putBoolean(HINT_PREFS_NAME, false).commit()
            }
            else {
                // put card fragment for the first time
                val newFragment = CardFragment()
                newFragment.arguments = arguments
                newFragment.arguments.putBoolean(CardFragment.ARG_IS_FRONT, !isShowingBack)
                childFragmentManager
                    .beginTransaction()
                    .replace(R.id.layout_card_container, newFragment)
                    .commit()
            }
        }

        // adding event handler
        val gestureDetector = GestureDetector(activity, CardGestureDetector(flip))
        val layout = rootView.findViewById(R.id.layout_card_container)
        layout.setOnTouchListener { view, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
        }

        return rootView
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isShowingBack = savedInstanceState.getBoolean("isShowingBack")
        }
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean("isShowingBack", isShowingBack)
        super.onSaveInstanceState(outState)
    }

    private fun showHintFragment() {
        val hintFragment = TapHintFragment()
        childFragmentManager
            .beginTransaction()
            .replace(R.id.layout_card_container, hintFragment, HINT_FRAGMENT_TAG)
            .commit()
        isShowingHint = true
    }

    private val flip: () -> Unit = {
        if (isShowingHint) {
            // if flipping from hint fragment, first show the side that
            // would have been shown instead of the hint
            isShowingBack = !isShowingBack
            isShowingHint = false
        }
        // flip the card
        isShowingBack = !isShowingBack
        val newFragment = CardFragment()
        newFragment.arguments = arguments
        newFragment.arguments.putBoolean(CardFragment.ARG_IS_FRONT, !isShowingBack)
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                R.animator.card_flip_left_in, R.animator.card_flip_left_out
            )
            .replace(R.id.layout_card_container, newFragment)
            .commit()
    }

    class CardFragment() : Fragment() {
        /**
         * CardActivity callbacks for Know/NotKnow and Quit buttons.
         */
        private var callbacks: Callbacks = dummyCallbacks
        private var isFrontFragment = false

        @TargetApi(23)
        override fun onAttach(context: Context) {
            callbacks = context as Callbacks
            super.onAttach(context)
        }

        // this is needed to support lower APIs
        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun onAttach(activity: Activity) {
            callbacks = activity as Callbacks
            super.onAttach(activity)
        }

        override fun onDetach() {
            super.onDetach()
            callbacks = dummyCallbacks
        }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            if (savedInstanceState == null) {
                isFrontFragment = arguments.getBoolean(ARG_IS_FRONT)
            }
            else {
                isFrontFragment = savedInstanceState.getBoolean(ARG_IS_FRONT)
            }

            val rootView = inflater!!.inflate(R.layout.fragment_card, container, false)
            val bgColorId = if (isFrontFragment) R.color.background else R.color.backgroundDark
            rootView.setBackgroundColor(ContextCompat.getColor(CardActivity.appContext, bgColorId))
            val textView = rootView.textview_cardview_mainfield
            textView.text = arguments.getString(
                if (isFrontFragment) ARG_FRONT_CONTENT else ARG_BACK_CONTENT
            )
            rootView.button_cardview_know.setOnClickListener { callbacks.onCardButtonClicked(true) }
            rootView.button_cardview_notknow.setOnClickListener { callbacks.onCardButtonClicked(false) }
            val iconQuit = rootView.icon_cardview_quit
            iconQuit.setOnClickListener { callbacks.onQuitButtonClicked() }
            return rootView
        }

        override fun onSaveInstanceState(outState: Bundle?) {
            outState?.putBoolean(ARG_IS_FRONT, isFrontFragment)
            super.onSaveInstanceState(outState)
        }

        companion object {
            val ARG_IS_FRONT = "isFrontFragment"
        }
    }

    class TapHintFragment() : Fragment() {
        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_tap_hint, container, false)
            return rootView
        }
    }

    class DummyCallbacks() : Callbacks {
        override fun onCardButtonClicked(knowIt: Boolean) {
        }

        override fun onQuitButtonClicked() {
        }
    }

    private class CardGestureDetector(val onTapAction: () -> Unit) : GestureDetector.OnGestureListener {
        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

        override fun onShowPress(p0: MotionEvent?) {
        }

        override fun onLongPress(p0: MotionEvent?) {
        }

        /*
          If set to true, this nasty thing won't let me scroll textviews.
          If set to false, card flipping won't work.
          Have no idea what to do with this thing.
        */
        override fun onDown(e: MotionEvent) = true

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            onTapAction()
            return true
        }
    }

    interface Callbacks {
        fun onCardButtonClicked(knowIt: Boolean)
        fun onQuitButtonClicked()
    }
}
