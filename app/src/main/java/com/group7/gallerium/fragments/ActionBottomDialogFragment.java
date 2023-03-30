package com.group7.gallerium.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.group7.gallerium.R;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;

import java.io.File;

public class ActionBottomDialogFragment extends BottomSheetDialogFragment {
    Context context;
    View view;

    String titleText;
    String path, oldPath, name, ext;

    FileUtils fileUtils;
    TextView title;
    Button verifiedButton, cancelButton;
    EditText changeText;

    public static final String TAG = "ActionBottomDialog";

    private ActivityResultLauncher<IntentSenderRequest> launcher;
    public static ActionBottomDialogFragment newInstance() {
        return new ActionBottomDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(ActionBottomDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogThemeNoFloating);
        fileUtils = new FileUtils();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context =requireContext();
        view = LayoutInflater.from(context).inflate(R.layout.action_bottom_dialog, null);

        verifiedButton = view.findViewById(R.id.verifiedButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        changeText = view.findViewById(R.id.actionText);
        title = view.findViewById(R.id.actionName);

        title.setText(titleText);

        changeText.setText(name);

        cancelButton.setOnClickListener((v)->{
           this.onDestroyView();
        });

        verifiedButton.setOnClickListener((v)->{
            performAction();
        });
        return view;
    }

    public void performAction() {
        if(titleText.equals("Đổi tên") && changeText.getText().toString().length() > 0) {
            fileUtils.renameFile(changeText.getText().toString() + "." + ext, AccessMediaFile.getMediaWithPath(path).getType(), path, context, launcher);

            Toast.makeText(context, "renamed", Toast.LENGTH_SHORT).show();
            path = AccessMediaFile.renameMedia(path, changeText.getText().toString() + "." + ext);
            name = changeText.getText().toString();
        }else if(titleText.equals("Nhập tên album") && changeText.getText().toString().length() > 0){
            var name = changeText.getText().toString();
            if (name.startsWith(".")) return;
            if (name.isBlank()) return;
            if (name.equals("Thùng rác") || name.equals("Ảnh") || name.equals("Video")) return;
            var dir = Environment.getExternalStorageDirectory();
            String relativePath = "Pictures/owner/" + changeText.getText().toString();
            String path = dir.getPath() + File.separator + relativePath;
            try {
                fileUtils.createDir(context, path, changeText.getText().toString(), relativePath);
            } catch (Exception e) {
                Log.d("tag", e.getMessage());
                // Toast.makeText(context, "Needed permission. \nPress 'Choose this folder' to continue. ", Toast.LENGTH_LONG).show();
            }
            AccessMediaFile.addToYourAlbum(path);
        }
        dismiss();
    }

    public void renameAgain() {
        if(title.getText().equals("Đổi tên") && changeText.getText().toString().length() > 0){
            fileUtils.renameFile(changeText.getText().toString() + "." + ext, AccessMediaFile.getMediaWithPath(path).getType(), oldPath,  context, launcher);
        }
        Toast.makeText(context, "renamed", Toast.LENGTH_SHORT).show();
        path = AccessMediaFile.renameMedia(path, changeText.getText().toString() + "." + ext);
        name = changeText.getText().toString();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setTitle(String name){
        titleText = name;
        Log.d("change", "true");
    }

    public void setPath(String path){
        this.path = path;
        this.oldPath = path;
        String[] subDirs = path.split("/");
        var n = subDirs[subDirs.length -1];
        name = n.substring(0, n.lastIndexOf("."));
        ext = n.substring(n.lastIndexOf(".") + 1);
    }

    public void setLauncher(ActivityResultLauncher<IntentSenderRequest> launcher) {
        this.launcher = launcher;
    }
}
