package com.nexis.chatapp.Fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nexis.chatapp.Adapter.MessagesAdapter;
import com.nexis.chatapp.Model.MessageRequest;
import com.nexis.chatapp.R;

import java.util.ArrayList;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link ChatFragment#newInstance} factory method to
// * create an instance of this fragment.
// *
// */
public class ChatFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment ChatFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static ChatFragment newInstance(String param1, String param2) {
//        ChatFragment fragment = new ChatFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    public ChatFragment() {
//        // Required empty public constructor
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

    private View view;
    private RecyclerView mRecyclerView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private ArrayList<MessageRequest> messageRequestArrayList;
    private ArrayList<String> mEndMessageList;
    private MessageRequest messageRequest;
    private FirebaseUser mUser;
    private MessagesAdapter messagesAdapter;

    private Query endMsgQuery;
    private int endMsgInd = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_chat, container, false);

        mFirestore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        messageRequestArrayList = new ArrayList<>();
        mEndMessageList = new ArrayList<>();

        mRecyclerView = view.findViewById(R.id.chat_fragment_messages_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));

        mQuery = mFirestore.collection("Kullanicilar").document(mUser.getUid()).collection("Kanal");
        mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(view.getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    messageRequestArrayList.clear();
                    endMsgInd = 0;

                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        messageRequest = snapshot.toObject(MessageRequest.class);

                        if( messageRequest != null){// mesaj isteği boş değilse
                            messageRequestArrayList.add(messageRequest); // mesaj isteğini arraylist içine ekle


                            endMsgQuery = mFirestore.collection("ChatKanallari").document(messageRequest.getKanalId()).collection("Mesajlar").orderBy("mesajTarihi", Query.Direction.DESCENDING).limit(1);

                            endMsgQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                                    if(error == null && value2 != null){
                                        mEndMessageList.clear();

                                        for(DocumentSnapshot snapshot1 : value2.getDocuments()){
                                            mEndMessageList.add(snapshot1.getData().get("mesajIcerigi").toString());
                                            endMsgInd++ ;
                                        }

                                        if(endMsgInd == value.getDocuments().size()){
                                            messagesAdapter = new MessagesAdapter(messageRequestArrayList, view.getContext(),mEndMessageList);
                                            mRecyclerView.setAdapter(messagesAdapter);
                                            endMsgInd = 0;
                                        }

                                    }
                                }
                            });
                        }
                    }

                }
            }
        });

        return view;
    }
}