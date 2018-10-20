package com.dslg.app.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.view.View;
import android.view.ViewGroup;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class SlideX extends Visibility {
	@Override
	public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues,
			TransitionValues endValues) {
		return createSlideAnimator(view, true);
	}
	
	@Override
	public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues,
			TransitionValues endValues) {
		return createSlideAnimator(view, false);
	}
	
	private Animator createSlideAnimator(View view, boolean appear) {
		int slideTo = appear ? 0 : view.getWidth();
		int slideFrom = appear ? view.getWidth() : 0;
		int alphaTo = appear ? 1 : 0;
		int alphaFrom = appear ? 0 : 1;
		
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, slideFrom, slideTo);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, alphaFrom, alphaTo);
		set.playTogether(slide, alpha);
		
		return set;
	}
}
