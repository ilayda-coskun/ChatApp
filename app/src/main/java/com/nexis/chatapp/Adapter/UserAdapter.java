package com.nexis.chatapp.Adapter;

import android.app.Dialog;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.nexis.chatapp.Activity.ChatActivity;
import com.nexis.chatapp.Model.Kullanici;
import com.nexis.chatapp.Model.MessageRequest;
import com.nexis.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private ArrayList<Kullanici> mKullaniciList;
    private Context mContext;
    private View view;
    private Kullanici mKullanici;
    private int kPos;

    private Dialog messageDialog;
    private ImageView imgClose;
    private LinearLayout linearSend;
    private CircleImageView imgProfil;
    private EditText editMessage;
    private String txtMessage;
    private Window messageWindow;
    private TextView txtName;

    private FirebaseFirestore mFirestore;
    private DocumentReference mDocumentReference;
    private String mUId;
    private String mName;
    private String mProfilUrl;
    private String kanalId;
    private String messageDocID;
    private MessageRequest messageRequest;
    private HashMap<String, Object> mData;

    private Intent chatIntent;

    public UserAdapter(ArrayList<Kullanici> mKullaniciList, Context mContext, String mUId, String mName, String mProfilUrl) {
        this.mKullaniciList = mKullaniciList;
        this.mContext = mContext;
        mFirestore = FirebaseFirestore.getInstance();
        this.mUId = mUId;
        this.mName = mName;
        this.mProfilUrl = mProfilUrl;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        mKullanici = mKullaniciList.get(position);
        holder.userName.setText(mKullanici.getKullaniciIsmi());

        if (mKullanici.getKullaniciProfil().equals("default")) {
            holder.userProfile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.get().load(mKullanici.getKullaniciProfil()).resize(70, 70).into(holder.userProfile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kPos = holder.getAdapterPosition();

                //İlgili kullanıcı ile daha önceden etkileşim gerçekleşip gerçekleşmediği kontrol edilmekte
                if (kPos != RecyclerView.NO_POSITION) {
                    mDocumentReference = mFirestore.collection("Kullanicilar").document(mUId).collection("Kanal").document(mKullaniciList.get(kPos).getKullaniciId()); //döküman kontrolü
                    mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                //Mesajlaşma aktivite
                                chatIntent = new Intent(mContext, ChatActivity.class);
                                chatIntent.putExtra("kanalId", documentSnapshot.getData().get("kanalId").toString());
                                chatIntent.putExtra("hedefId", mKullaniciList.get(kPos).getKullaniciId());
                                chatIntent.putExtra("hedefProfil", mKullaniciList.get(kPos).getKullaniciProfil());
                                chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mContext.startActivity(chatIntent);
                            } else {
                                mesajGonderDialog(mKullaniciList.get(kPos));
                            }
                        }
                    });
                }
            }
        });
    }

    private void mesajGonderDialog(final Kullanici kullanici) {
        messageDialog = new Dialog(mContext);
        messageDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        messageWindow = messageDialog.getWindow();
        messageWindow.setGravity(Gravity.CENTER);
        messageDialog.setContentView(R.layout.custom_dialog_message_send);

        imgClose = messageDialog.findViewById(R.id.customDialog_messageSend_img_iptal);
        linearSend = messageDialog.findViewById(R.id.customDialog_messageSend_linear_send);
        imgProfil = messageDialog.findViewById(R.id.customDialog_messageSend_img_userProfile);
        editMessage = messageDialog.findViewById(R.id.customDialog_messageSend_edit_message);
        txtName = messageDialog.findViewById(R.id.custom_dialog_messageSend_txt_userName);

        txtName.setText(kullanici.getKullaniciIsmi());

        if (kullanici.getKullaniciProfil().equals("default")) {
            imgProfil.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.get().load(kullanici.getKullaniciProfil()).resize(120, 120).into(imgProfil);
        }

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageDialog.dismiss();
            }
        });

        linearSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtMessage = editMessage.getText().toString();

                if (!TextUtils.isEmpty(txtMessage)) {
                    kanalId = UUID.randomUUID().toString();

                    messageRequest = new MessageRequest(kanalId, mUId, mName, mProfilUrl);
                    mFirestore.collection("MesajIstekleri").document(kullanici.getKullaniciId()).collection("Istekler").document(mUId).set(messageRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                messageDocID = UUID.randomUUID().toString();
                                mData = new HashMap<>();
                                mData.put("mesajIcerigi", txtMessage);
                                mData.put("gonderen", mUId);
                                mData.put("alici", kullanici.getKullaniciId());
                                mData.put("mesajTipi", "text");
                                mData.put("mesajTarihi", FieldValue.serverTimestamp());
                                mData.put("docId", messageDocID);

                                mFirestore.collection("ChatKanallari").document(kanalId).collection("Mesajlar").document(messageDocID).set(mData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(mContext, "Mesaj İsteği İletildi!", Toast.LENGTH_SHORT).show();
                                            if (messageDialog.isShowing())
                                                messageDialog.dismiss();
                                        } else {
                                            Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(mContext, "Boş Mesaj Gönderemezsiniz!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        messageWindow.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        messageDialog.show();
    }

    @Override
    public int getItemCount() {
        return mKullaniciList.size();
    }

    class UserHolder extends RecyclerView.ViewHolder {

        TextView userName;
        CircleImageView userProfile;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_item_txtUserName);
            userProfile = itemView.findViewById(R.id.user_item_imgUserProfile);
        }
    }
}
