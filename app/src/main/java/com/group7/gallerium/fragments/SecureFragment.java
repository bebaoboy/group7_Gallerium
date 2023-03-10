package com.group7.gallerium.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.group7.gallerium.R;

public class SecureFragment extends Fragment {
    private View view;
    private Toolbar toolbar;
    private Context context;
    private GridLayout numGrid;
    private EditText txtPass;
    private MaterialButton btnClear, btnEnter;

    public SecureFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_secure, container, false);
        numGrid = view.findViewById(R.id.numpad_grid);
        txtPass = view.findViewById(R.id.txtPassword);
        btnClear = view.findViewById(R.id.secure_clear_button);
        btnEnter = view.findViewById(R.id.secure_enter_button);
        for(int i = 0; i < 10; i++)
        {
            var b = (MaterialButton) numGrid.getChildAt(i);
            b.setOnClickListener((view1 -> {
                txtPass.setText(txtPass.getText() + "" + b.getText());
            }));
        }

        btnClear.setOnClickListener((view1 -> txtPass.setText("")));
        context = getContext();
        toolbarSetting();
        return view;
    }

    void toolbarSetting(){
        toolbar = view.findViewById(R.id.toolbar_secure);
        toolbar.inflateMenu(R.menu.menu_secure);
        toolbar.setTitle(R.string.secured);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
    }
}
