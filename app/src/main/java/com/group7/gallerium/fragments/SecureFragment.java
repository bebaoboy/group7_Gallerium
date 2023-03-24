package com.group7.gallerium.fragments;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.group7.gallerium.R;

public class SecureFragment extends Fragment {
    private View view;
    private Toolbar toolbar;
    private Context context;
    private NestedScrollView scroll;
    private GridLayout numGrid;
    private EditText txtPass;
    private MaterialButton btnClear, btnEnter;

    boolean isLandscape = false;

    public SecureFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLandscape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onResume() {
        super.onResume();
        view.invalidate();
    }

    public void changeOrientation() {
        view.invalidate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        if (isLandscape) {
//            view = inflater.inflate(R.layout.fragment_secure_land, container, false);
//        } else {
            view = inflater.inflate(R.layout.fragment_secure, container, false);
//        }
        scroll = view.findViewById(R.id.secure_scrollview);
        numGrid = view.findViewById(R.id.numpad_grid);
        txtPass = view.findViewById(R.id.txtPassword);
        btnClear = view.findViewById(R.id.secure_clear_button);
        btnEnter = view.findViewById(R.id.secure_enter_button);
        for(int i = 0; i < 10; i++)
        {
            var b = (MaterialButton) numGrid.getChildAt(i);
            b.setOnClickListener((view1 -> txtPass.setText(txtPass.getText() + "" + b.getText())));
        }

        btnClear.setOnClickListener((view1 -> txtPass.setText("")));
        btnEnter.setOnClickListener((view1) -> Toast.makeText(this.getContext(), "Your pass: " + txtPass.getText(), Toast.LENGTH_SHORT).show());
        context = getContext();
        toolbarSetting();
        return view;
    }

    void toolbarSetting(){
        toolbar = view.findViewById(R.id.toolbar_secure);
        toolbar.inflateMenu(R.menu.menu_top_secure);
        toolbar.setTitle(R.string.secured);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
        scroll.getViewTreeObserver().addOnScrollChangedListener(() -> toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        toolbar.setVisibility(View.INVISIBLE);
                        toolbar.animate().translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator())
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(@NonNull Animator animator) {
                                    }

                                    @Override
                                    public void onAnimationEnd(@NonNull Animator animator) {
                                        toolbar.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationCancel(@NonNull Animator animator) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(@NonNull Animator animator) {
                                    }
                                })
                                .start();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {
                    }
                })
                .start());

    }
}
