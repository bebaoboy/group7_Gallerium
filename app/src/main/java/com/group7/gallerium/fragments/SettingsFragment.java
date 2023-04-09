package com.group7.gallerium.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.group7.gallerium.R;

import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsFragment extends PreferenceFragmentCompat {
    Toolbar toolbar;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Context context = this.getContext();
        Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(preference.getKey().equals("lockPrivateKey")){
                    if(!(boolean)newValue){
                        String password = requireContext()
                                .getSharedPreferences("secured_list", Context.MODE_PRIVATE)
                                .getString("password", "");

                        if(!password.equals("")){
                            AtomicBoolean result = new AtomicBoolean(false);
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

                            LayoutInflater inflater = requireActivity().getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_questionaire, null);
                            var editQuestion = (EditText) dialogView.findViewById(R.id.question);
                            var editAnswer = (EditText) dialogView.findViewById(R.id.answer);
                            editQuestion.setHint("Nhập mật khẩu của bạn");
                            editAnswer.setHint("Nhập lại mật khẩu");

                            builder.setTitle("Xác nhận mật khẩu của bạn để tắt khóa bảo mật");
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                        if(!password.equals(editAnswer.getText().toString())){
                                            Toast.makeText(context, "Mật khẩu của bạn sai rồi", Toast.LENGTH_SHORT).show();
                                        }else if(!editQuestion.getText().toString().equals(editAnswer.getText().toString())){
                                            Toast.makeText(context, "Mật khẩu nhập lại của bạn sai r", Toast.LENGTH_SHORT).show();
                                        }else{
                                            ((SwitchPreferenceCompat)preference).setChecked(false);
                                        }
                                    }).setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
                                        dialogInterface.dismiss();
                                    }));
                            builder.create();
                            builder.show();
                        }
                    }else{
                        return true;
                    }
                }
                return false;
            }
        };

        SwitchPreferenceCompat privateSwitchCompat = (SwitchPreferenceCompat) findPreference("lockPrivateKey");
        privateSwitchCompat.setOnPreferenceChangeListener(changeListener);
        return super.onCreateView(inflater, container, savedInstanceState);

    }
}