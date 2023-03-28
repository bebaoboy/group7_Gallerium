package com.group7.gallerium.utilities;

import android.animation.Animator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

public class ToolbarScrollListener extends RecyclerView.OnScrollListener {

    final Toolbar toolbar;
    private LinearLayout bottomSheetDialog;

    public ToolbarScrollListener(@NonNull Toolbar t, @NonNull LinearLayout btm) {
        toolbar = t;
        bottomSheetDialog = btm;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animator) {
                            toolbar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animator) {
                        }
                    })
                    .start();
            bottomSheetDialog.animate().translationY(0).setInterpolator(new DecelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animator) {
                            toolbar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animator) {
                        }
                    })
                    .start();

            super.onScrollStateChanged(recyclerView, newState);
        } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            super.onScrollStateChanged(recyclerView, newState);
            toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animator) {
                            toolbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animator) {
                        }
                    })
                    .start();
            bottomSheetDialog.animate().translationY(bottomSheetDialog.getBottom()).setInterpolator(new AccelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animator) {
                            toolbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animator) {
                        }
                    })
                    .start();

        } else {
            super.onScrollStateChanged(recyclerView, newState);
            if (!recyclerView.canScrollVertically(-1) || !recyclerView.canScrollVertically(1)) {
                Log.d("gallerium", "cannot scroll vert");
                recyclerView.stopNestedScroll();
                recyclerView.stopScroll();
            }
        }

    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (!recyclerView.canScrollVertically(-1) || !recyclerView.canScrollVertically(1)) {
            Log.d("gallerium", "cannot scroll");
            recyclerView.stopNestedScroll();
            recyclerView.stopScroll();
        }

    }
}
