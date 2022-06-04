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
import com.nexis.chatapp.R;

public class GirisYapActivity extends AppCompatActivity {

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private LinearLayout mLinear;
    private FirebaseUser mUser;

    private TextInputLayout inputEmail;
    private TextInputLayout inputSifre;

    private EditText editEmail;
    private EditText editSifre;

    private String txtEmail;
    private String txtSifre;

    private void init() {
        mLinear = (LinearLayout) findViewById(R.id.girisYap_linear);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        inputEmail = (TextInputLayout) findViewById(R.id.girisYap_input_email);
        inputSifre = (TextInputLayout) findViewById(R.id.girisYap_input_sifre);

        editEmail = (EditText) findViewById(R.id.girisYap_edit_email);
        editSifre = (EditText) findViewById(R.id.girisYap_edit_sifre);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_yap);

        init();

        if (mUser != null) {
            finish();
            startActivity(new Intent(GirisYapActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    public void btnGirisYap(View view) {
        txtEmail = editEmail.getText().toString();
        txtSifre = editSifre.getText().toString();

        if (!(TextUtils.isEmpty(txtEmail))) {
            if (!(TextUtils.isEmpty(txtSifre))) {
                mProgress = new ProgressDialog(this);
                mProgress.setTitle("Giriş Yapılıyor...");
                mProgress.show();

                mAuth.signInWithEmailAndPassword(txtEmail, txtSifre).addOnCompleteListener(GirisYapActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressKapatma();
                            Toast.makeText(GirisYapActivity.this, "Giriş Başarılı", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(GirisYapActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        } else {
                            progressKapatma();
                            Snackbar.make(mLinear, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                inputSifre.setError("Şifre Geçerli Değil");
            }
        } else {
            inputEmail.setError("Email Geçerli Değil");
        }
    }

    public void btnGitKayitOl(View view) {
        startActivity(new Intent(GirisYapActivity.this, KayitOlActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void progressKapatma() {
        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }
}