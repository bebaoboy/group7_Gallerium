package com.group7.gallerium.fragments;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.group7.gallerium.R;
import com.group7.gallerium.utilities.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SecureFragment extends Fragment {
    private View view;
    private Toolbar toolbar;
    private Context context;
    private NestedScrollView scroll;
    private GridLayout numGrid;
    private EditText txtPass;
    private MaterialButton btnClear, btnEnter;

    private ArrayList<String> paths;

    boolean isLandscape = false;

    SharedPreferences sharedPreferences;

    FileUtils fileUtils;
    String password, question, answer;

    private  String secretPath;

    public SecureFragment() {}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLandscape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        fileUtils = new FileUtils();

        sharedPreferences = requireContext().getSharedPreferences("secured_list", Context.MODE_PRIVATE);
        password = sharedPreferences.getString("password", "");
        question = sharedPreferences.getString("question", "");
        answer = sharedPreferences.getString("answer", "");

        secretPath = Environment.getExternalStorageState() + "/" + ".secret";

        Log.d("answer", answer);
        Log.d("question", question);
        Log.d("password", password);
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
        btnEnter.setOnClickListener((view1) -> {
            Toast.makeText(this.getContext(), "Your pass: " + txtPass.getText(), Toast.LENGTH_SHORT).show();
            try {
                verifiedLogic();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        context = requireContext();

        showViewLogic();
        toolbarSetting();
        return view;
    }

    private void showViewLogic() {
        if(txtPass.getText().toString().equals("")){
            view.findViewById(R.id.secure_scrollview).setVisibility(View.VISIBLE);
            view.findViewById(R.id.main_secured_page).setVisibility(View.GONE);
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_questionaire, null);

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    var editQuestion = (EditText)dialogView.findViewById(R.id.question);
                    var editAnswer = (EditText)dialogView.findViewById(R.id.answer);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putString("question", editQuestion.getText().toString());
                    myEdit.putString("answer", editAnswer.getText().toString());
                    myEdit.apply();
                    showViewLogic();
                }).setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
                     dialogInterface.dismiss();
                }));
        builder.create();
        builder.show();
    }

    void verifiedLogic() throws FileNotFoundException {
        if(txtPass.getText().length() < 4){
            Toast.makeText(requireContext(), "Password not valid", Toast.LENGTH_SHORT).show();
        }else if(txtPass.getText().toString().equals(password)) {
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("password", txtPass.getText().toString());
            myEdit.apply();
            if (!question.isBlank() && !answer.isBlank()) {
                showAlertDialog();
                createSecuredDir();
            } else {
                view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
                view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
            }
        }
    }

    private void createSecuredDir() throws FileNotFoundException {
        File secureDir = new File(context.getFilesDir(), "secure-subfolder");
        Log.d("context-Filedir", context.getFilesDir().getAbsolutePath());
        if(!secureDir.exists()){
            secureDir.mkdirs();
            password = sharedPreferences.getString("password", "");
        }

        view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
        view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
        
       // createSecuredFile(new File(secureDir, "hello.txt"), "");
        getPaths(secureDir);
    }

    private void createSecuredFile(File fileName, String data) {
        String fileContents = "Hello world!";
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<String> getPaths(File file) throws FileNotFoundException {
        File[] files = file.listFiles();
        for(File f: files){
            Log.d("file-path", f.getAbsolutePath());
            FileInputStream fin = new FileInputStream(f);

        }
        return null;
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
