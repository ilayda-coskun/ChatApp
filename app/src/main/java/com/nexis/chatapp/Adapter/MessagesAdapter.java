package com.nexis.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexis.chatapp.Activity.ChatActivity;
import com.nexis.chatapp.Model.MessageRequest;
import com.nexis.chatapp.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesHolder> {

    private ArrayList<MessageRequest> messageRequestArrayList;
    private Context mContext;
    private MessageRequest messageRequest;
    private View view;
    private int kPos;
    private ArrayList<String> mEndMessageList;

    private Intent chatIntent;

    public MessagesAdapter(ArrayList<MessageRequest> messageRequestArrayList, Context mContext, ArrayList<String> mEndMessageList) {
        this.messageRequestArrayList = messageRequestArrayList;
        this.mContext = mContext;
        this.mEndMessageList = mEndMessageList;
    }

    @NonNull
    @Override
    public MessagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.chat_item, parent, false);

        return new MessagesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesHolder holder, int position) {
        messageRequest = messageRequestArrayList.get(position);
        holder.userName.setText(messageRequest.getKullaniciIsim());
        holder.endMessage.setText(mEndMessageList.get(position));

        if (messageRequest.getKullaniciProfil().equals("default")) {
            holder.userProfile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.get().load(messageRequest.getKullaniciProfil()).resize(70, 70).into(holder.userProfile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kPos = holder.getAdapterPosition();

                if (kPos != RecyclerView.NO_POSITION) {
                    chatIntent = new Intent(mContext, ChatActivity.class);
                    chatIntent.putExtra("kanalId", messageRequestArrayList.get(kPos).getKanalId());
                    chatIntent.putExtra("hedefId", messageRequestArrayList.get(kPos).getKullaniciId());
                    chatIntent.putExtra("hedefProfil", messageRequestArrayList.get(kPos).getKullaniciProfil());
                    chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(chatIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageRequestArrayList.size();
    }


    class MessagesHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView endMessage;
        CircleImageView userProfile;

        public MessagesHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.chat_item_txtUserName);
            userProfile = itemView.findViewById(R.id.chat_item_imgUserProfile);
            endMessage = itemView.findViewById(R.id.chat_item_txtEndMessage);
        }
    }
}
