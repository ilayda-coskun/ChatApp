package com.nexis.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nexis.chatapp.Adapter.ChatAdapter;
import com.nexis.chatapp.Model.Chat;
import com.nexis.chatapp.Model.Kullanici;
import com.nexis.chatapp.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private HashMap<String, Object> mData;

    private RecyclerView mRecyclerView;
    private EditText editMessage;
    private String txtMessage;
    private String docId;
    private CircleImageView hedefProfil;
    private TextView hedefName;
    private Intent gelenIntent;
    private String hedefId;
    private String kanalId;
    private String hedefProfilUrl;
    private Kullanici hedefKullanici;
    private DocumentReference hedefDocumentReference;
    private FirebaseFirestore mFirestore;

    private Query chatQuery;
    private ArrayList<Chat> mChatArrayList;
    private Chat mChat;
    private ChatAdapter chatAdapter;

    public void init() {
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mRecyclerView = (RecyclerView) findViewById(R.id.chat_activity_recyclerView);
        editMessage = (EditText) findViewById(R.id.chat_activity_editMessage);
        hedefProfil = (CircleImageView) findViewById(R.id.chat_activity_imgHedefProfil);
        hedefName = (TextView) findViewById(R.id.chat_activity_txtHedefName);

        mFirestore = FirebaseFirestore.getInstance();
        gelenIntent = getIntent();
        hedefId = gelenIntent.getStringExtra("hedefId");
        kanalId = gelenIntent.getStringExtra("kanalId");
        hedefProfilUrl = gelenIntent.getStringExtra("hedefProfil");

        mChatArrayList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.nexis.chatapp.R.layout.activity_chat);

        init();

        hedefDocumentReference = mFirestore.collection("Kullanicilar").document(hedefId);
        hedefDocumentReference.addSnapshotListener(this, (value, error) -> {
            if (error != null) {
                Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null && value.exists()) {
                hedefKullanici = value.toObject(Kullanici.class); //hedef kullanıcı bilgisi atılmakta

                if (hedefKullanici != null) {
                    hedefName.setText(hedefKullanici.getKullaniciIsmi());

                    if (hedefKullanici.getKullaniciProfil().equals("default")) { //hedef kullanıcı defaultlt ise
                        hedefProfil.setImageResource(R.mipmap.ic_launcher); // resmi default olarak getir
                    } else { //hedef kullanıcı default değil ise
                        Picasso.get().load(hedefKullanici.getKullaniciProfil()).resize(70, 70).into(hedefProfil);// resim boyutu 70 70 yapıldı
                    }
                }
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        chatQuery = mFirestore.collection("ChatKanallari").document(kanalId).collection("Mesajlar").
                orderBy("mesajTarihi", Query.Direction.ASCENDING);
        chatQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    mChatArrayList.clear(); // liste anlık olarak çekildiğinden aynı mesajalı almaması için temizleniyor

                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        mChat = snapshot.toObject(Chat.class);

                        if (mChat != null)
                            mChatArrayList.add(mChat);
                    }

                    chatAdapter = new ChatAdapter(mChatArrayList, ChatActivity.this, mUser.getUid(), hedefProfilUrl);
                    mRecyclerView.setAdapter(chatAdapter);
                }
            }
        });
    }

    public void btnMesajGonder(View view) {
        txtMessage = editMessage.getText().toString();

        if (!TextUtils.isEmpty(txtMessage)) {
            docId = UUID.randomUUID().toString();

            mData = new HashMap<>();
            mData.put("mesajIcerigi", txtMessage);
            mData.put("gonderen", mUser.getUid());
            mData.put("alici", hedefId);
            mData.put("mesajTipi", "text");
            mData.put("mesajTarihi", FieldValue.serverTimestamp());
            mData.put("docId", docId);

            mFirestore.collection("ChatKanallari").document(kanalId).collection("Mesajlar").document(docId).set(mData).addOnCompleteListener(ChatActivity.this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        editMessage.setText("");
                    } else {
                        Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(ChatActivity.this, "Boş Mesaj Gönderemezsiniz !", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnChatKapat(View view) {
        finish();
    }
}