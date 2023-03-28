package com.group7.gallerium.fragments;

<<<<<<< Updated upstream
=======
import static android.os.storage.StorageManager.ACTION_MANAGE_STORAGE;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.exifinterface.media.ExifInterface;
>>>>>>> Stashed changes
import androidx.fragment.app.Fragment;

<<<<<<< Updated upstream
public class SecureFragment extends Fragment {
=======
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.MediaAdapter;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.SelectMediaInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SecureFragment extends Fragment implements SelectMediaInterface {
    private View view;
    private Toolbar toolbar;
    private Context context;
    private NestedScrollView scroll;
    private GridLayout numGrid;
    private EditText txtPass;
    private MaterialButton btnClear, btnEnter;

    private MediaAdapter secureAdapter;

    private RecyclerView secureRecyclerView;

    private ArrayList<String> paths;

    private ArrayList<Media> mediaList;

    boolean isLandscape = false;

    SharedPreferences sharedPreferences;

    FileUtils fileUtils;
    String password, question, answer;

    Button createPassButton;

    ImageView test;
    private  String secretPath;
    private ActivityResultLauncher<IntentSenderRequest> launcher;


    // App needs 200 MB within internal storage.
    private static final long NUM_BYTES_NEEDED_FOR_MY_APP = 1024 * 1024 * 200L;

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

        paths = new ArrayList<>();
        mediaList = new ArrayList<>();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(context.getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();
                        AccessMediaFile.removeMediaFromAllMedia(paths.get(0));
                    }
                });

        //password = "";
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

        createPassButton = view.findViewById(R.id.create_folder_button);

        //test = view.findViewById(R.id.preview);
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

        createPassButton.setOnClickListener((v)->{
            try {
                createSecuredDir();
            } catch (FileNotFoundException e) {
                Log.d("tag", e.getMessage());
            }
        });

        showViewLogic();
        toolbarSetting();
        return view;
    }

    private void showViewLogic() {
        if(txtPass.getText().toString().equals("")){
            view.findViewById(R.id.secure_scrollview).setVisibility(View.VISIBLE);
            view.findViewById(R.id.main_secured_page).setVisibility(View.GONE);
            view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
        }
        if(password.equals("")){
            view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
            view.findViewById(R.id.main_secured_page).setVisibility(View.GONE);
            view.findViewById(R.id.create_pass_page).setVisibility(View.VISIBLE);
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
        password = sharedPreferences.getString("password", "");
        if(password.equals("")){
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("password", txtPass.getText().toString());
            myEdit.apply();
            if (question.isBlank() && answer.isBlank()) {
                showAlertDialog();
                view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
                view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
                view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
                view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
                view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
                setUpView();
            }
        }
        if(txtPass.getText().length() < 4){
            Toast.makeText(requireContext(), "Password not valid", Toast.LENGTH_SHORT).show();
        }else if(txtPass.getText().toString().equals(password)) {
            view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
            view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
            view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
            setUpView();
        }
    }

    void setUpView() throws FileNotFoundException {
        File secureDir = new File(context.getFilesDir(), "secure-subfolder");
        getPaths(secureDir);
        createMediaList();

        secureAdapter = new MediaAdapter(requireActivity().getApplicationContext(), this);

        secureRecyclerView = view.findViewById(R.id.secured_recycler_view);
        secureAdapter.setListImages(mediaList);
        secureAdapter.setListCategory(null);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        secureRecyclerView.setAdapter(secureAdapter);
        secureRecyclerView.setLayoutManager(layoutManager);
    }

    void checkForFreeSpace() throws IOException {

        StorageManager storageManager =
                context.getApplicationContext().getSystemService(StorageManager.class);
        UUID appSpecificInternalDirUuid = storageManager.getUuidForPath(new File(context.getFilesDir() + "/secure-folder"));
        long availableBytes =
                storageManager.getAllocatableBytes(appSpecificInternalDirUuid);
        if (availableBytes >= NUM_BYTES_NEEDED_FOR_MY_APP) {
            storageManager.allocateBytes(
                    appSpecificInternalDirUuid, NUM_BYTES_NEEDED_FOR_MY_APP);
        } else {
            // To request that the user remove all app cache files instead, set
            // "action" to ACTION_CLEAR_APP_CACHE.
            Intent storageIntent = new Intent();
            storageIntent.setAction(ACTION_MANAGE_STORAGE);
        }
    }

    private void createSecuredDir() throws FileNotFoundException {
        File secureDir = new File(context.getFilesDir(), "secure-subfolder");
        Log.d("context-Filedir", context.getFilesDir().getAbsolutePath());
        if(!secureDir.exists()){
            secureDir.mkdirs();
            try {
                checkForFreeSpace();
            }catch (Exception e){
                Log.d("tag", e.getMessage());
            }
        }

        view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
        view.findViewById(R.id.secure_scrollview).setVisibility(View.VISIBLE);
        view.findViewById(R.id.main_secured_page).setVisibility(View.GONE);

       // createSecuredFile(new File(secureDir, "hello.txt"), "");
    }

    private void createSecuredFile(File fileName, String data) {
        String fileContents = "Hello world!";
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPaths(File file) throws FileNotFoundException {
        File[] files = file.listFiles();
        for (File f : files) {
            Log.d("file-path", f.getAbsolutePath());
            paths.add(f.getAbsolutePath());
        }
        // paths.remove(0);
        // Glide.with(context).load(paths.get(0)).into(test);
        // moveFile(paths.get(0));
    }

    String getMimeType(String path){
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.d("mime-type", type
        );
        return type;
    }

    int getType(String mimeType){
        int mediaType = -1;
        if(mimeType.startsWith("image")){ mediaType = 1;}
        else mediaType = 3;
        return mediaType;
    }
    void moveFile(String path){
        String mimeType = getMimeType(path);
        int mediaType = getType(mimeType);

        // Log.d("media-type", ""+mediaType);
        if(mediaType == -1) return;
        fileUtils.moveFromInternal(path, launcher, "/storage/emulated/0/DCIM/Screenshots", mimeType, mediaType,   context);
        paths.remove(path);
    }


    void moveMultipleFile(String[] paths){

    }

    Long getDateModified(String filePath){
        File file = new File(filePath);
        if(file.exists()) //Extra check, Just to validate the given path
        {
            ExifInterface intf = null;
            try
            {
                intf = new ExifInterface(filePath);
                if(intf != null)
                {
                    SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
                    String dateString = intf.getAttribute(ExifInterface.TAG_DATETIME);
                    Log.i("PHOTO DATE", "Dated : "+ dateString); //Display dateString. You can do/use it your own way
                    Date lastModDate = f.parse(dateString);
                    return  lastModDate.getTime();
                }
            }
            catch (IOException e)
            {
            } catch (ParseException e) {
                Log.d("tag", e.getMessage());
            }
            if(intf == null)
            {
                Date lastModDate = new Date(file.lastModified());
                Log.i("PHOTO DATE", "Dated : "+ lastModDate.toString());//Dispaly lastModDate. You can do/use it your own way
                return lastModDate.getTime();
            }
        }
        return 0L;
    }

    void createMediaList(){
        for(String path: paths){
            String[] dirs = path.split("/");
            Media media = new Media();
            String mimeType = getMimeType(path);
            int mediaType = getType(mimeType);
            media.setMimeType(mimeType);
            media.setType(mediaType);
            media.setPath(path);
            media.setTitle(dirs[dirs.length-1]);
            media.setThumbnail(path);
            //media.setDateTaken(getDateModified(path));

            mediaList.add(media);
        }
    }

    void toolbarSetting(){
        toolbar = view.findViewById(R.id.toolbar_secure);
        toolbar.inflateMenu(R.menu.menu_top_secure);
        toolbar.setTitle(R.string.secured);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);

        toolbar.getMenu().findItem(R.id.change_pass_menu_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return false;
            }
        });

        toolbar.getMenu().findItem(R.id.remove_pass_menu_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm");
                builder.setMessage("Do you want to delete secured folder?");
                builder.setPositiveButton("YES", (dialog, which) -> {
                    File secureDir = new File(context.getFilesDir(), "secure-subfolder");
                    secureDir.delete();
                    dialog.dismiss();
                });

                builder.setNegativeButton("NO", (dialog, which) -> {
                    dialog.dismiss();
                });
                return false;
            }
        });

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

    @Override
    public void showAllSelect() {

    }

    @Override
    public void addToSelectedList(@NonNull Media media) {

    }

    @NonNull
    @Override
    public ArrayList<Media> getSelectedList() {
        return null;
    }

    @Override
    public void deleteFromSelectedList(@NonNull Media media) {

    }

    @Override
    public void moveMedia(@NonNull String albumPath) {

    }
>>>>>>> Stashed changes
}
