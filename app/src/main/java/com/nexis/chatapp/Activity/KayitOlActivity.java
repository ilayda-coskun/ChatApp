package com.nexis.chatapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexis.chatapp.Model.Kullanici;
import com.nexis.chatapp.R;

public class KayitOlActivity extends AppCompatActivity {

    private ProgressDialog mProgress;
    private Kullanici mKullanici;
    private LinearLayout mLayout;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private TextInputLayout inputIsim;
    private TextInputLayout inputEmail;
    private TextInputLayout inputSifre;
    private TextInputLayout inputSifreTekrar;

    private EditText editIsim;
    private EditText editEmail;
    private EditText editSifre;
    private EditText editSifreTekrar;

    private String txtIsım;
    private String txtEmail;
    private String txtSifre;
    private String txtSifreTekrar;


    private void init() {

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mLayout = (LinearLayout) findViewById(R.id.kayitOl_linear);

        inputIsim = (TextInputLayout) findViewById(R.id.kayitOl_input_isim);
        inputEmail = (TextInputLayout) findViewById(R.id.kayitOl_input_email);
        inputSifre = (TextInputLayout) findViewById(R.id.kayitOl_input_sifre);
        inputSifreTekrar = (TextInputLayout) findViewById(R.id.kayitOl_input_sifreTekrar);

        editIsim = (EditText) findViewById(R.id.kayitOl_edit_isim);
        editEmail = (EditText) findViewById(R.id.kayitOl_edit_email);
        editSifre = (EditText) findViewById(R.id.kayitOl_edit_sifre);
        editSifreTekrar = (EditText) findViewById(R.id.kayitOl_edit_sifreTekrar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit_ol);
        init();
    }

    public void btnKayitOl(View view) {
        txtIsım = editIsim.getText().toString();
        txtEmail = editEmail.getText().toString();
        txtSifre = editSifre.getText().toString();
        txtSifreTekrar = editSifreTekrar.getText().toString();

        if (!TextUtils.isEmpty(txtIsım)) {
            if (!TextUtils.isEmpty(txtEmail)) {
                if (!TextUtils.isEmpty(txtSifre)) {
                    if (!TextUtils.isEmpty(txtSifreTekrar)) {
                        if (txtSifre.equals(txtSifreTekrar)) {
                            mProgress = new ProgressDialog(this);
                            mProgress.setTitle("Kayıt olunuyor...");
                            mProgress.show();

                            mAuth.createUserWithEmailAndPassword(txtEmail, txtSifre).addOnCompleteListener(KayitOlActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) { //veritabanına kaydetme
                                        mUser = mAuth.getCurrentUser();

                                        if (mUser != null) {
                                            mKullanici = new Kullanici(txtIsım, txtEmail, mUser.getUid(),"default");

                                            mFirestore.collection("Kullanicilar").document(mUser.getUid())
                                                    .set(mKullanici)
                                                    .addOnCompleteListener(KayitOlActivity.this, new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                progressKapatma();
                                                                Toast.makeText(KayitOlActivity.this, "Basariyla kayit oldunuz.", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                                startActivity(new Intent(KayitOlActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                            } else {
                                                                progressKapatma();
                                                                Snackbar.make(mLayout, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        progressKapatma();
                                        Snackbar.make(mLayout, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Snackbar.make(mLayout, "Şifreler Uyuşmuyor", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        inputSifreTekrar.setError("Sifre Tekrar Alanını Lütfen Doldurunuz!");
                    }
                } else {
                    inputSifre.setError("Sifre Alanını Lütfen Doldurunuz!");
                }
            } else {
                inputEmail.setError("Email Alanını Lütfen Doldurunuz!");
            }
        } else {
            inputIsim.setError("İsim Alanını Lütfen Doldurunuz!");
        }
    }

    private void progressKapatma(){
        if(mProgress.isShowing())
            mProgress.dismiss();
    }
}