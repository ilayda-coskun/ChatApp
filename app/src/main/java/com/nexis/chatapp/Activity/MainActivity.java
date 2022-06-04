package com.nexis.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nexis.chatapp.Adapter.MessageRequestsAdapter;
import com.nexis.chatapp.Fragment.ChatFragment;
import com.nexis.chatapp.Fragment.ProfileFragment;
import com.nexis.chatapp.Fragment.UsersFragment;
import com.nexis.chatapp.Model.Kullanici;
import com.nexis.chatapp.Model.MessageRequest;
import com.nexis.chatapp.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomView;

    private UsersFragment usersFragment;
    private ChatFragment chatFragment;
    private ProfileFragment profileFragment;
    private FragmentTransaction transaction;
    private Toolbar mToolbar;
    private RelativeLayout mRelativeNotification;
    private TextView txtNotificationNumber;

    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private FirebaseUser mUser;
    private MessageRequest mMessageRequest;
    private ArrayList<MessageRequest> mMessageRequestsList;

    private Dialog messageRequestsDialog;
    private ImageView messageRequestsClose;
    private RecyclerView messageRequestsRecyclerView;
    private MessageRequestsAdapter messageRequestsAdapter;

    private DocumentReference mDocumentReference;
    private Kullanici mKullanici;

    private void init() {

        mFirestore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mDocumentReference = mFirestore.collection("Kullanicilar").document(mUser.getUid());
        mDocumentReference.get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    mKullanici = documentSnapshot.toObject(Kullanici.class);
                }
            }
        });

        mMessageRequestsList = new ArrayList<>();

        mBottomView = (BottomNavigationView) findViewById(R.id.mainActivity_bottomView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRelativeNotification = (RelativeLayout) findViewById(R.id.bar_layout_relative_notification);
        txtNotificationNumber = (TextView) findViewById(R.id.bar_layout_txt_notificationNumber);

        usersFragment = new UsersFragment();
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        fragmentSec(usersFragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        //Mesaj isteklerini aldığımız kısım
        mQuery = mFirestore.collection("MesajIstekleri").document(mUser.getUid()).collection("Istekler");
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    txtNotificationNumber.setText(String.valueOf(value.getDocuments().size()));
                    mMessageRequestsList.clear();

                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        mMessageRequest = snapshot.toObject(MessageRequest.class);
                        mMessageRequestsList.add(mMessageRequest);
                    }
                }
            }
        });


        mRelativeNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageRequestsDialog();
            }
        });


        mBottomView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.bottom_nav_ic_users:
                        mRelativeNotification.setVisibility(View.INVISIBLE);
                        fragmentSec(usersFragment);
                        return true;

                    case R.id.bottom_nav_ic_chat:
                        mRelativeNotification.setVisibility(View.VISIBLE);
                        fragmentSec(chatFragment);
                        return true;

                    case R.id.bottom_nav_ic_profile:
                        mRelativeNotification.setVisibility(View.INVISIBLE);
                        fragmentSec(profileFragment);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void fragmentSec(Fragment fragment) {
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainActivity_frameLayout, fragment);
        transaction.commit();
    }

    private void messageRequestsDialog() {

        messageRequestsDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        messageRequestsDialog.setContentView(R.layout.custom_dialog_gelen_message_request);

        messageRequestsClose = messageRequestsDialog.findViewById(R.id.custom_dialog_messageRequests_img_close);
        messageRequestsRecyclerView = messageRequestsDialog.findViewById(R.id.custom_dialog_messageRequests_recyclerView);

        messageRequestsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageRequestsDialog.dismiss();
            }
        });

        messageRequestsRecyclerView.setHasFixedSize(true);
        messageRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        if (mMessageRequestsList.size() > 0) {

            messageRequestsAdapter = new MessageRequestsAdapter(mMessageRequestsList, this, mKullanici.getKullaniciId(), mKullanici.getKullaniciIsmi(), mKullanici.getKullaniciProfil());
            messageRequestsRecyclerView.setAdapter(messageRequestsAdapter);
        }

        messageRequestsDialog.show();
    }
}