package com.nexis.chatapp.Fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ComponentActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nexis.chatapp.Model.Kullanici;
import com.nexis.chatapp.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link ProfileFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class ProfileFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public ProfileFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment ProfileFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static ProfileFragment newInstance(String param1, String param2) {
//        ProfileFragment fragment = new ProfileFragment();
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

    private static final int PERMISSON_CODE = 0;
    private static final int PERMISSION_ALLOWED = 1;

    private EditText editUserName;
    private EditText editUserEmail;
    private View v;
    private CircleImageView imgUserProfil;
    private ImageView imgNewPhoto;

    private FirebaseFirestore mFirestore;
    private DocumentReference mDocumentReference;
    private FirebaseUser mUser;
    private Kullanici user;

    private Intent galleryIntent;

    private Uri mUri;
    private Bitmap comingPhoto;
    private ImageDecoder.Source imgSource;
    private ByteArrayOutputStream outputStream;
    private byte[] imgByte;
    private StorageReference storageReference;
    private StorageReference newStorageReference;
    private StorageReference sStorageReference;
    private String recordSpace;
    private String downloadLink;
    private HashMap<String, Object> mData;

    private Query mQuery;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        v = inflater.inflate(R.layout.fragment_profile, container, false);

        editUserName = v.findViewById(R.id.profile_fragment_editUserName);
        editUserEmail = v.findViewById(R.id.profile_fragment_editUserEmail);
        imgUserProfil = v.findViewById(R.id.profile_fragment_imgUserProfile);
        imgNewPhoto = v.findViewById(R.id.profile_fragment_imgNewPhoto);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        mDocumentReference = mFirestore.collection("Kullanicilar").document(mUser.getUid());
        mDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(v.getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null && value.exists()) {
                    user = value.toObject(Kullanici.class);

                    if (user != null) {
                        editUserName.setText(user.getKullaniciIsmi());
                        editUserEmail.setText(user.getKullaniciEmail());

                        if (user.getKullaniciProfil().equals("default")) {
                            imgUserProfil.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Picasso.get().load(user.getKullaniciProfil()).resize(170, 170).into(imgUserProfil);
                        }
                    }
                }
            }
        });

        imgNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { // izin verilmemeişse
                    ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSON_CODE);
                } else {
                    goGallery();
                }
            }
        });

        return v;
    }

    private void goGallery() {
        galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //telefon hafızasına ulaşmak için
        startActivityForResult(galleryIntent, PERMISSION_ALLOWED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //izin alma gerçekleşirse diye

        if (requestCode == PERMISSON_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goGallery();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PERMISSION_ALLOWED) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                mUri = data.getData();

                try {
                    if (Build.VERSION.SDK_INT >= 28) {
                        imgSource = ImageDecoder.createSource(v.getContext().getContentResolver(), mUri);
                        comingPhoto = ImageDecoder.decodeBitmap(imgSource);
                    } else {
                        comingPhoto = MediaStore.Images.Media.getBitmap(v.getContext().getContentResolver(), mUri);
                    }

                    outputStream = new ByteArrayOutputStream();
                    comingPhoto.compress(Bitmap.CompressFormat.PNG, 75, outputStream);
                    imgByte = outputStream.toByteArray();

                    recordSpace = "Kullanicilar/" + user.getKullaniciEmail() + "/profil.png";
                    sStorageReference = storageReference.child(recordSpace);
                    sStorageReference.putBytes(imgByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            newStorageReference = FirebaseStorage.getInstance().getReference(recordSpace);
                            newStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadLink = uri.toString();

                                    mData = new HashMap<>();
                                    mData.put("kullaniciProfil", downloadLink);

                                    mFirestore.collection("Kullanicilar").document(mUser.getUid()).update(mData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                communicationProfilUpdate(downloadLink);
                                            } else {
                                                Toast.makeText(v.getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void communicationProfilUpdate(final String link) {
        mQuery = mFirestore.collection("Kullanicilar").document(mUser.getUid()).collection("Kanal");
        mQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.getDocuments().size() > 0) {

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        mData = new HashMap<>();
                        mData.put("kullaniciProfil", link);
                        mFirestore.collection("Kullanicilar").document(snapshot.getData().get("kullaniciId").toString()).collection("Kanal").document(mUser.getUid()).update(mData);
                    }

                    Toast.makeText(v.getContext(), "Profil Resmi Güncellendi.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}