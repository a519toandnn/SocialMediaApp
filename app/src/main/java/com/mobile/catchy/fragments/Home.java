package com.mobile.catchy.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.ReplacerActivity;
import com.mobile.catchy.adapter.HomeAdapter;
import com.mobile.catchy.adapter.StoriesAdapter;
import com.mobile.catchy.model.HomeModel;
import com.mobile.catchy.model.StoriesModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Home extends Fragment {

    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();

    RecyclerView storiesRecyclerView;
    StoriesAdapter storiesAdapter;
    List<StoriesModel> storiesModelList;
    public Home() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);


        //reference = FirebaseFirestore.getInstance().collection("Posts").document(user.getUid());
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likeList, boolean isChecked) {
                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if(likeList.contains(user.getUid())) {
                    likeList.remove(user.getUid()); // unlike
                } else {
                    likeList.add(user.getUid()); // like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likeList);
                reference.update(map);
            }

            @Override
            public void setCommentCount(TextView textView) {

//                commentCount.observe((LifecycleOwner) getContext(), integer -> {
//                    if( integer == 0){
//                        textView.setVisibility(View.GONE);
//                    }else{
//                        textView.setVisibility(View.VISIBLE);
//                        textView.setText("See all + " + integer + " comments");
//                    }
//
//                });

            }
        });
    }



    private void loadDataFromFirestore() {
        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());
        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");


        reference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }

            if (value == null) {
                return;
            }

            List<String> uidList = (List<String>) value.get("following");

            if (uidList == null || uidList.isEmpty()) {
                return;
            }

            collectionReference.whereIn("uid", uidList)
                    .addSnapshotListener((value1, error1) -> {
                        if (error1 != null) {
                            Log.d("Error: ", error.getMessage());
                        }
                        if (value1 == null) {
                            Log.e("Error: ", "No data found");
                            return;
                        }

                        for (QueryDocumentSnapshot snapshot : value1) {

                            snapshot.getReference().collection("Post Images")
                                    .addSnapshotListener((value11, error11) -> {
                                        if (error11 != null) {
                                            Log.d("Error: ", error11.getMessage());
                                        }
                                        if (value11 == null) {
                                            Log.e("Error: ", "No data found");
                                            return;
                                        }

                                        list.clear();

                                        for (final QueryDocumentSnapshot snapshot1 : value11) {
                                            if (!snapshot1.exists()) {
                                                Log.e("Error: ", "No data found");
                                                return;
                                            }
                                            HomeModel model = snapshot1.toObject(HomeModel.class);

//                                            list.add(new HomeModel(
//                                                    model.getName(),
//                                                    model.getProfileImage(),
//                                                    model.getImageUrl(),
//                                                    model.getUid(),
//                                                    model.getDescription(),
//                                                    model.getId(),
//                                                    model.getTimestamp(),
//                                                    model.getLikes()
//                                            ));

                                            snapshot1.getReference().collection("Comments").get()
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful() && task.getResult() != null) {
                                                            int count = task.getResult().size();
                                                            model.setCommentCount(count); // Cập nhật số lượng bình luận cho từng model
                                                            adapter.notifyDataSetChanged(); // Thông báo thay đổi cho adapter
                                                        } else {
                                                            Log.e("FirestoreError", "Failed to get comments", task.getException());
                                                            model.setCommentCount(0); // Đặt giá trị bằng 0 nếu có lỗi
                                                        }
                                                    });
                                            list.add(model);
                                        }
                                    });


                        }
                    });
            loadStoriess(uidList);
        });

    }

    void loadStoriess(List<String> followingList) {
        Query query = FirebaseFirestore.getInstance().collection("Stories");
        query.whereIn("uid",followingList).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    Log.d("Error", error.getMessage());
                }

                if(value !=  null) {
                    return;
                }

                for(QueryDocumentSnapshot snapshot : value) {
                    if(value.isEmpty()) {
                        StoriesModel model = snapshot.toObject(StoriesModel.class);
                        storiesModelList.add(model);
                    }
                }
                storiesAdapter.notifyDataSetChanged();
            }
        });
    }

    private void init(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        storiesRecyclerView = view.findViewById(R.id.storiesRecylerView);
        storiesRecyclerView.setHasFixedSize(true);
        storiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        storiesModelList= new ArrayList<>();
        storiesModelList.add(new StoriesModel("","","",""));
        storiesAdapter = new StoriesAdapter( storiesModelList, getActivity());
        storiesRecyclerView.setAdapter(storiesAdapter);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }
}