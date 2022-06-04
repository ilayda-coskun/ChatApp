package com.nexis.chatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexis.chatapp.Model.MessageRequest;
import com.nexis.chatapp.R;
import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageRequestsAdapter extends RecyclerView.Adapter<MessageRequestsAdapter.MessageRequestsHolder> {

    private ArrayList<MessageRequest> messageRequestList;
    private Context mContext;
    private MessageRequest messageRequest;
    private MessageRequest newMessageRequest;
    private View view;
    private int mPos;

    private String mUId;
    private String mName;
    private String mProfilUrl;

    private FirebaseFirestore mFirestore;


    public MessageRequestsAdapter(ArrayList<MessageRequest> messageRequestList, Context mContext, String mUId, String mName, String mProfilUrl) {
        this.messageRequestList = messageRequestList;
        this.mContext = mContext;
        mFirestore = FirebaseFirestore.getInstance();
        this.mUId = mUId;
        this.mName = mName;
        this.mProfilUrl = mProfilUrl;
    }

    @NonNull
    @Override
    public MessageRequestsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.gelen_message_requests_item, parent, false);
        return new MessageRequestsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageRequestsHolder holder, int position) {
        messageRequest = messageRequestList.get(position);
        holder.txtMessage.setText(messageRequest.getKullaniciIsim() + "Kullanıcısı Sana Mesaj Göndermek İstiyor");

        if (messageRequest.getKullaniciProfil().equals("default")) {
            holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.get().load(messageRequest.getKullaniciProfil()).resize(77, 77).into(holder.imgProfile);
        }

        //Mesaj isteklerini onaylama ve reddetme

        //MEsaj isteği kabul etme
        holder.imgOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPos = holder.getAdapterPosition();

                if (mPos != RecyclerView.NO_POSITION) {

                    newMessageRequest = new MessageRequest(messageRequestList.get(mPos).getKanalId(),
                            messageRequestList.get(mPos).getKullaniciId(),
                            messageRequestList.get(mPos).getKullaniciIsim(),
                            messageRequestList.get(mPos).getKullaniciProfil());
                    mFirestore.collection("Kullanicilar").document(mUId).collection("Kanal").document(messageRequestList.get(mPos).getKullaniciId()).set(newMessageRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                newMessageRequest = new MessageRequest(messageRequestList.get(mPos).getKanalId(), mUId, mName, mProfilUrl);

                                mFirestore.collection("Kullanicilar").document(messageRequestList.get(mPos).getKullaniciId()).collection("Kanal").document(mUId).set(newMessageRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            messageRequestsDelete(messageRequestList.get(mPos).getKullaniciId(), "Mesaj İsteği Kabul Edildi !");
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
                }
            }
        });


        //Mesaj isteği reddetme
        holder.imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPos = holder.getAdapterPosition();
                if (mPos != RecyclerView.NO_POSITION)
                    messageRequestsDelete(messageRequestList.get(mPos).getKullaniciId(), "Mesaj İsteği Reddedildi !");
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageRequestList.size();
    }

    class MessageRequestsHolder extends RecyclerView.ViewHolder {

        CircleImageView imgProfile;
        TextView txtMessage;
        ImageView imgCancel;
        ImageView imgOkay;

        public MessageRequestsHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.gelen_messageRequest_item_imgProfil);
            txtMessage = itemView.findViewById(R.id.gelen_messageRequest_item_txtMessage);
            imgCancel = itemView.findViewById(R.id.gelen_messageRequest_item_imgCancel);
            imgOkay = itemView.findViewById(R.id.gelen_messageRequest_item_imgOkay);
        }
    }

    private void messageRequestsDelete(String hedefUId, final String messageContent) {
        mFirestore.collection("MesajIstekleri").document(mUId).collection("Istekler").document(hedefUId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    notifyDataSetChanged(); // ilgili içeriği değiştiği anda kontrol için kullanıldı
                    Toast.makeText(mContext, "Mesajlaşma Reddedildi ! ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
