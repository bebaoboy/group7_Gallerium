package com.group7.gallerium.fragments;

import static android.os.storage.StorageManager.ACTION_MANAGE_STORAGE;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.group7.gallerium.R;
import com.group7.gallerium.activities.SettingsActivity;
import com.group7.gallerium.adapters.MediaAdapter;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;
import com.group7.gallerium.utilities.FileUtils;
import com.group7.gallerium.utilities.SelectMediaInterface;
import com.group7.gallerium.utilities.ToolbarScrollListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class SecureFragment extends Fragment implements SelectMediaInterface {
    private View view;
    private Toolbar toolbar;
    private Context context;
    private NestedScrollView scroll;
    private GridLayout numGrid;
    private EditText txtPass;
    private MaterialButton btnClear, btnEnter;

    private Button createFolder;

    private MediaAdapter secureAdapter;

    private RecyclerView secureRecyclerView;

    private ArrayList<String> paths;

    private ArrayList<Media> mediaList;

    boolean isLandscape = false, isReset = false, lockable = true;

    SharedPreferences sharedPreferences, sharedPref;

    FileUtils fileUtils;
    String password, question, answer;

    ImageView test;
    private  String secretPath;
    private ActivityResultLauncher<IntentSenderRequest> launcher;
    private int spanCount = 3;
    private int firstVisiblePosition;
    private int offset;


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

        Log.d("answer", answer);
        Log.d("question", question);
        Log.d("password", password);
    }

    CountDownTimer countDownTimer;
    @Override
    public void onPause() {
        super.onPause();
        countDownTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long l) {
                System.out.println(l);
            }

            @Override
            public void onFinish() {
                txtPass.setText("");
                showViewLogic();
            }
        };
        countDownTimer.start();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(countDownTimer != null)
            countDownTimer.cancel();
        view.invalidate();
        var sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        var numGridPref = sharedPref.getString(SettingsActivity.KEY_PREF_NUM_GRID, "3");
        var numGrid = 0;
        if(numGridPref.equals("5")){
            numGrid = 5;
        }else if(numGridPref.equals("4")){
            numGrid = 4;
        }else{
            numGrid = 3;
        }
        if (numGrid != spanCount) {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                changeOrientation(numGrid * 2);
            } else {
                changeOrientation(numGrid);
            }
        }
        refresh();

        lockable = sharedPref.getBoolean(SettingsActivity.KEY_PREF_LOCK_PRIVATE, false);
        showViewLogic();
    }

    public void changeOrientation(int spanCount) {
        view.invalidate();
        saveScroll();
        if (spanCount != this.spanCount) {
            this.spanCount = spanCount;
            refresh();
            ((LinearLayoutManager) Objects.requireNonNull(secureRecyclerView.getLayoutManager())).scrollToPositionWithOffset(firstVisiblePosition, offset);
//            callback.onDestroyActionMode(mode);
        }
    }

    private void saveScroll() {
        View firstChild = secureRecyclerView.getChildAt(0);
        if (firstChild != null) {
            firstVisiblePosition = secureRecyclerView.getChildAdapterPosition(firstChild);
            offset = firstChild.getTop();
        }
    }

    public void refresh() {
        File secureDir = new File(context.getFilesDir(), "secure-subfolder");
        try {
            paths.clear();
            getPaths(secureDir);
        }catch (Exception e){
            Log.d("tag", e.getMessage());
        }
        mediaList.clear();
        createMediaList();
        secureAdapter.setListImages(mediaList);
        secureAdapter.setListCategory(null);
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        secureRecyclerView.setAdapter(secureAdapter);
        secureRecyclerView.setLayoutManager(layoutManager);
        secureAdapter.setImageSize(calculateImageSize());
        Log.d("refresh", "");

    }

    public void refresh(boolean scroll) {
        Log.d("refresh with result", "");

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

        createFolder = view.findViewById(R.id.create_folder_button);
        secureAdapter = new MediaAdapter(requireActivity().getApplicationContext(), this);
        secureRecyclerView = view.findViewById(R.id.secured_recycler_view);

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
                verifiedLogic(password, isReset);
            } catch (Exception e){
                Log.d("tag", e.getMessage());
            }
        });

        createFolder.setOnClickListener((v)->{
            try {
                createSecuredDir();
            }catch (Exception e){
                Log.d("tag", e.getMessage());
            }
        });
        context = requireContext();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        lockable =  sharedPref.getBoolean(SettingsActivity.KEY_PREF_LOCK_PRIVATE, false);

        showViewLogic();
        toolbarSetting();
        return view;
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


    public int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private int calculateImageSize() {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenWidth -= dpToPx(10);
        int spacing = dpToPx(5);
        var imageSize = Math.max((screenWidth - spacing * (spanCount - 1)) / (double)spanCount, dpToPx(60));
        return (int)Math.floor(imageSize);
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

    void verifiedLogic(String pass, boolean isReset) {
        if(pass.equals("") && !isReset){
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
        if(txtPass.getText().length() < 4 && !isReset){
            Toast.makeText(requireContext(), "Password not valid", Toast.LENGTH_SHORT).show();
        }else if(txtPass.getText().toString().equals(password) && !isReset) {
            view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
            view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
            view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
            setUpView();
        }

        if(isReset && txtPass.getText().length() == 4){
            String tempPass = txtPass.getText().toString();
            resetPasswordLogic(tempPass);
        }

        password = sharedPreferences.getString("password", "");
    }

    void setUpView(){
        File secureDir = new File(context.getFilesDir(), "secure-subfolder");
        try {
            paths.clear();
            getPaths(secureDir);
        }catch (Exception e){
            Log.d("tag", e.getMessage());
        }

        mediaList.clear();
        createMediaList();

        secureAdapter = new MediaAdapter(requireActivity().getApplicationContext(), this);
        secureRecyclerView = view.findViewById(R.id.secured_recycler_view);
        secureAdapter.setListImages(mediaList);
        secureAdapter.setListCategory(null);
        secureAdapter.setImageSize(calculateImageSize());
        // secureRecyclerView.setOnScrollChangeListener(new ToolbarScrollListener(toolbar, null));
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        secureRecyclerView.setAdapter(secureAdapter);
        secureRecyclerView.setLayoutManager(layoutManager);
    }
    
     private void getPaths(File file) throws FileNotFoundException {
        File[] files = file.listFiles();
        for (File f : files) {
            Log.d("file-path", f.getAbsolutePath());
            paths.add(f.getAbsolutePath());
            //moveFile(f.getAbsolutePath());
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

        Log.d("media-type", ""+mediaType);
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

        if(!lockable){
            view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
            view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
            view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
        }
    }

    void resetPasswordLogic(String password){
        isReset = false;
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("password", password);
        myEdit.apply();
        view.findViewById(R.id.secure_scrollview).setVisibility(View.GONE);
        view.findViewById(R.id.main_secured_page).setVisibility(View.VISIBLE);
        view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
    }

    void showResetPasswordView(){
        isReset = true;
        view.findViewById(R.id.secure_scrollview).setVisibility(View.VISIBLE);
        view.findViewById(R.id.main_secured_page).setVisibility(View.GONE);
        view.findViewById(R.id.create_pass_page).setVisibility(View.GONE);
    }

    void requestForAnswer() {
        if (question.isBlank() && answer.isBlank()) {
            Toast.makeText(context.getApplicationContext(), "Bạn chưa tạo mật khẩu", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_questionaire, null);
            var editQuestion = (EditText) dialogView.findViewById(R.id.question);
            var editAnswer = (EditText) dialogView.findViewById(R.id.answer);
            editQuestion.setText(question + "?");
            builder.setView(dialogView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        certifiedAnswer(editAnswer.getText().toString());
                    }).setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }));
            builder.create();
            builder.show();
        }
    }

    private void certifiedAnswer(String toString) {
        if(answer.equals(toString)){
            showResetPasswordView();
        }
    }

    private void resetEveryhthing(){
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("question", "");
        myEdit.putString("answer", "");
        myEdit.putString("password","");
        myEdit.apply();
        password = sharedPreferences.getString("password", "");
        txtPass.setText("");
    }

    void toolbarSetting(){
        toolbar = view.findViewById(R.id.toolbar_secure);
        toolbar.inflateMenu(R.menu.menu_top_secure);
        toolbar.setTitle(R.string.secured);
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
        
         toolbar.getMenu().findItem(R.id.forget_pass_menu_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                requestForAnswer();
                return true;
            }
        });



        toolbar.getMenu().findItem(R.id.remove_pass_menu_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(!password.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm");
                    builder.setMessage("Do you want to delete secured folder?");
                    builder.setPositiveButton("YES", (dialog, which) -> {
                        File secureDir = new File(context.getFilesDir(), "secure-subfolder");
                        secureDir.delete();
                        resetEveryhthing();
                        dialog.dismiss();
                    });

                    builder.setNegativeButton("NO", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.show();
                }else{
                    Toast.makeText(context.getApplicationContext(), "Bạn chưa có tạo folder bảo mật", Toast.LENGTH_SHORT).show();
                }
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

    public void setLockable(boolean lockable) {
        this.lockable = lockable;
    }

    public boolean isLockable() {
        return lockable;
    }
}