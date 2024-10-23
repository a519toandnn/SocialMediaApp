package com.mobile.catchy.fragments;



import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.GalleryAdapter;
import com.mobile.catchy.model.GalleryImages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Add1 extends Fragment {

    private EditText descET;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ImageButton backBtn, nextBtn;
    private List<GalleryImages> list;
    private GalleryAdapter adapter;
    private FirebaseUser user;

    Uri imageUri;
    String imageUrl;

//    private boolean isLoading = false;
//    private int currentPage = 0; // Bắt đầu từ trang 0
//    private int pageSize = 20; // Số lượng ảnh mỗi lần tải

    public Add1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);

        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);

        recyclerView.setAdapter(adapter);
//        // Thêm OnScrollListener để tải thêm ảnh khi cuộn
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
//                int visibleItemCount = layoutManager.getChildCount();
//                int totalItemCount = layoutManager.getItemCount();
//                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
//
//                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
//                        && firstVisibleItemPosition >= 0) {
//                    // Cuộn tới cuối danh sách, tải thêm ảnh
//                    loadMoreImages();
//                }
//            }
//        });

        clickListeners();


    }

//    private void loadMoreImages() {
//        isLoading = true;
//
//        // Giả lập việc tải ảnh với độ trễ nhỏ (có thể thay thế bằng truy vấn MediaStore thực tế)
//        new Handler().postDelayed(() -> {
//            List<GalleryImages> newImages = loadImagesFromMediaStore(currentPage, pageSize);
//            adapter.addImages(newImages);
//            currentPage++; // Tăng trang lên sau khi tải xong
//            isLoading = false; // Đặt lại cờ để cho phép tải thêm nếu cần
//        }, 1000);
//    }

//    private List<GalleryImages> loadImagesFromMediaStore(int page, int pageSize) {
//        List<GalleryImages> imageList = new ArrayList<>();
//        ContentResolver contentResolver = getContext().getContentResolver();
//        Uri collection;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
//        } else {
//            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        }
//
//        String[] projection = new String[]{
//                MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DISPLAY_NAME
//        };
//
//        String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
//        String[] selectionArgs = new String[]{"image/jpeg"};
//
//        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + pageSize + " OFFSET " + (page * pageSize);
//
//        try (Cursor cursor = contentResolver.query(
//                collection,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder)) {
//            if (cursor != null) {
//                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
//
//                while (cursor.moveToNext()) {
//                    long id = cursor.getLong(idColumn);
//                    Uri contentUri = ContentUris.withAppendedId(
//                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                    imageList.add(new GalleryImages(contentUri));
//                }
//            }
//        }
//
//        return imageList;
//    }

    private void clickListeners(){
        adapter.SendImage(picUri ->

                {
                    CropImageOptions options = new CropImageOptions();

                    options.guidelines = CropImageView.Guidelines.ON;
                    options.aspectRatioX = 4;
                    options.aspectRatioY = 3;


                    cropLauncher.launch(new CropImageContractOptions(picUri, options));


                    // CropImage.activity(picUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(4, 3).start(getContext(), Add.this);
                }


        );

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                final StorageReference storageReference = storage.getReference().child("Post Images/"+System.currentTimeMillis());
                storageReference.putFile(imageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageUrl = uri.toString();
                                            uploadData(uri.toString());
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    private void uploadData(String imageUrl) {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Post Images");

        String id = reference.document().getId();

        String description = descET.getText().toString();

        List<String> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("description", description);
        map.put("imageUrl", imageUrl);
        map.put("timestamp", FieldValue.serverTimestamp());

        map.put("userName", user.getDisplayName());
        map.put("profileImage", user.getPhotoUrl());
        map.put("likeCount", 0);
        map.put("comments","");
        map.put("uid", user.getUid());

        reference.document(id).set(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println();
                Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }


        });


    }


    private void init(View view) {

        descET = view.findViewById(R.id.descriptionET);
        imageView = view.findViewById(R.id.imageView);
        recyclerView = view.findViewById(R.id.recyclerView);
        backBtn = view.findViewById(R.id.backBtn);
        nextBtn = view.findViewById(R.id.nextBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Override
    public void onResume(){
        super.onResume();

        getActivity().runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void run() {
                Dexter.withContext(getContext())
                        .withPermissions(
                                // Thay thế quyền cũ bằng quyền mới tùy theo mục đích của bạn
                                android.Manifest.permission.READ_MEDIA_IMAGES,
                                android.Manifest.permission.READ_MEDIA_VIDEO,
                                android.Manifest.permission.READ_MEDIA_AUDIO,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
//                                if (report.areAllPermissionsGranted()) {
//                                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera");
//
//                                    if(file.exists()){
//                                        File[] files = file.listFiles();
//                                        assert files != null;
//                                        for (File file1 : files){
//                                            if(file1.getAbsolutePath().endsWith(".jpg") || file1.getAbsolutePath().endsWith(".png")){
//                                                list.add(new GalleryImages(Uri.fromFile(file1)));
//                                                adapter.notifyDataSetChanged();
//                                            }
//                                        }
//                                    }
//                                }
                                loadImagesFromMediaStoreAsync();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                // Hiển thị lý do cần thiết cho người dùng nếu họ từ chối quyền
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });
    }


    ActivityResultLauncher<CropImageContractOptions> cropLauncher = registerForActivityResult(new CropImageContract(), result -> {


        if (result.isSuccessful()) {
            imageUri = result.getUriContent();

            Glide.with(Add1.this).load(imageUri).into(imageView);

            imageView.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.VISIBLE);

        }

    });

    private void loadImagesFromMediaStoreAsync() {
        new AsyncTask<Void, Void, List<GalleryImages>>() {
            @Override
            protected List<GalleryImages> doInBackground(Void... voids) {
                List<GalleryImages> imageList = new ArrayList<>();
                ContentResolver contentResolver = getContext().getContentResolver();
                Uri collection;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                } else {
                    collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                String[] projection = new String[]{
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.RELATIVE_PATH // Dùng để lọc đường dẫn tương đối
                };

                // Lọc ảnh trong thư mục DCIM/Camera
                String selection = "(" + MediaStore.Images.Media.MIME_TYPE + "=? OR " +
                        MediaStore.Images.Media.MIME_TYPE + "=?) AND " +
                        MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
                String[] selectionArgs = new String[]{"image/jpeg", "image/png", "%AndroidNhat2%"};

                try (Cursor cursor = contentResolver.query(
                        collection,
                        projection,
                        selection,
                        selectionArgs,
                        MediaStore.Images.Media.DATE_ADDED + " DESC")) {
                    if (cursor != null) {
                        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                        while (cursor.moveToNext()) {
                            long id = cursor.getLong(idColumn);
                            Uri contentUri = ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                            imageList.add(new GalleryImages(contentUri));
                        }
                    }
                }

                return imageList;
            }

            @Override
            protected void onPostExecute(List<GalleryImages> result) {
                // Cập nhật danh sách ảnh trên UI thread
                list.clear();
                list.addAll(result);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }
}