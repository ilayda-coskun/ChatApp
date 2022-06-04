package com.nexis.chatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexis.chatapp.Model.Chat;
import com.nexis.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.PrimitiveIterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {

    //bu değerlerin değişmsi istenmediğinde büyük yazıldı
    private static final int MSG_LEFT = 1;
    private static final int MSG_RIGHT = 0;

    private ArrayList<Chat> mChatArrayList;
    private Context mContext; //verilerin görünümü vs ayarlamak için
    private View view;
    private Chat mChat;
    private String mUId;
    private String hedefProfil;

    public ChatAdapter(ArrayList<Chat> mChatArrayList, Context mContext, String mUId, String hedefProfil) {
        this.mChatArrayList = mChatArrayList;
        this.mContext = mContext;
        this.mUId = mUId;
        this.hedefProfil = hedefProfil;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_LEFT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        } else if (viewType == MSG_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        }

        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        mChat = mChatArrayList.get(position);
        holder.txtMessage.setText(mChat.getMesajIcerigi());

        if (!mChat.getGonderen().equals(mUId)) {
            if (hedefProfil.equals("default")) {
                holder.imgProfil.setImageResource(R.mipmap.ic_launcher);
            } else {
                Picasso.get().load(hedefProfil).resize(60, 60).into(holder.imgProfil);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChatArrayList.size();
    }

    class ChatHolder extends RecyclerView.ViewHolder {

        CircleImageView imgProfil;
        TextView txtMessage;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            imgProfil = itemView.findViewById(R.id.chat_item_imgProfil);
            txtMessage = itemView.findViewById(R.id.chat_item_txtMessage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mChatArrayList.get(position).getGonderen().equals(mUId)) { //gönderen kişinin uId si kendi uId mize eşitse geriye mesaj_sağ gönder
            return MSG_RIGHT;
        } else {
            return MSG_LEFT;
        }
    }
}
