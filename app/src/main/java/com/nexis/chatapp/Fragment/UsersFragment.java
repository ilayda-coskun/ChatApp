package com.nexis.chatapp.Fragment;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.nexis.chatapp.Adapter.UserAdapter;
import com.nexis.chatapp.Model.Kullanici;
import com.nexis.chatapp.R;

import java.util.ArrayList;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link UsersFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class UsersFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public UsersFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment UsersFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static UsersFragment newInstance(String param1, String param2) {
//        UsersFragment fragment = new UsersFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    private RecyclerView mRecyclerView;
    private View view;
    private UserAdapter mAdapter;
    private ArrayList<Kullanici> mKullaniciList;
    private Kullanici mKullanici;

    private FirebaseUser mUser;
    private Query mQuery;
    private FirebaseFirestore mFirestore;

    private DocumentReference mDocumentReference;
    private Kullanici kullanici;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mKullaniciList = new ArrayList<>();

        view = inflater.inflate(R.layout.fragment_users, container, false);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();

        mRecyclerView = view.findViewById(R.id.usersFragment_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));

        mDocumentReference=mFirestore.collection("Kullanicilar").document(mUser.getUid());
        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    kullanici = documentSnapshot.toObject(Kullanici.class);

                    mQuery = mFirestore.collection("Kullanicilar");
                    mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(view.getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (value != null) {
                                mKullaniciList.clear();

                                for (DocumentSnapshot snapshot : value.getDocuments()) {
                                    mKullanici = snapshot.toObject(Kullanici.class);

                                    assert mKullanici != null;
                                    if (!mKullanici.getKullaniciId().equals(mUser.getUid())) {
                                        mKullaniciList.add(mKullanici);
                                    }
                                }
                                mRecyclerView.addItemDecoration(new LinearDecoration(20, mKullaniciList.size()));
                                mAdapter = new UserAdapter(mKullaniciList, view.getContext(),kullanici.getKullaniciId(),kullanici.getKullaniciIsmi(),kullanici.getKullaniciProfil());
                                mRecyclerView.setAdapter(mAdapter);
                            }
                        }
                    });

                }
            }
        });

        return view;
    }

    class LinearDecoration extends RecyclerView.ItemDecoration {
        private int boslukMiktari;
        private int veriSayisi;

        public LinearDecoration(int boslukMiktari, int veriSayisi) {
            this.boslukMiktari = boslukMiktari;
            this.veriSayisi = veriSayisi;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);

            if (pos != veriSayisi - 1) {
                outRect.bottom = boslukMiktari;
            }
        }
    }
}