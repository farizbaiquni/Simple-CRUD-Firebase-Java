package com.example.crudfirebaseandroidjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    //Deklarasi Variabel dan Element
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private EditText editTextNIm, editTextNama, editTextJurusan;
    private Button buttonSimpan, buttonTampilData, buttonLogin, buttonLogout, buttonBlockLayout;


    //Kode Permintaan
    private  int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instansiasi Firebase Autentication
        auth = FirebaseAuth.getInstance();

        //Inisiasi Elemen
        progressBar = findViewById(R.id.progressBar);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonSimpan = findViewById(R.id.buttonSimpan);
        buttonBlockLayout = findViewById(R.id.buttonBlockLayout);
        buttonTampilData = findViewById(R.id.buttonTampilData);
        editTextNIm = findViewById(R.id.editTextNim);
        editTextNama = findViewById(R.id.editTextNama);
        editTextJurusan = findViewById(R.id.editTextJurusan);

        progressBar.setVisibility(View.GONE);

        //Event Listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            //Memilih Provider atau Method masuk yang akan kita gunakan
                            .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .setIsSmartLockEnabled(false)
                            .build()
                        , RC_SIGN_IN);

                progressBar.setVisibility(View.VISIBLE);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this, "Logout Berhasil", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
            }
        });

        buttonSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Mendapatkan user id dari pengguna yang telah terautentikasi / masuk
                String userId = auth.getCurrentUser().getUid();

                //Instansiasi database
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                //Instansiasi referensi
                DatabaseReference getReferene = database.getReference();

                //Mendapatkan data dari inputan user
                int nim = Integer.parseInt(editTextNIm.getText().toString());
                String nama = editTextNama.getText().toString();
                String jurusan = editTextJurusan.getText().toString();

                Toast.makeText(MainActivity.this, nim + nama + jurusan, Toast.LENGTH_SHORT).show();

                //Melakukan pengecekan jika ada bagian inputan yang kosong
                if( isEmpty(Integer.toString(nim)) || isEmpty(nama) || isEmpty(jurusan)){
                    Toast.makeText(MainActivity.this, "Input wajib diisi semua", Toast.LENGTH_SHORT).show();
                } else {

                    buttonBlockLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    editTextNIm.setCursorVisible(false);
                    editTextNama.setCursorVisible(false);
                    editTextJurusan.setCursorVisible(false);
                    hideKeyboard(v);

                    getReferene
                            .child("Admin")
                            .child(userId)
                            .child("Mahasiswa")
                            .push()
                            .setValue(new Mahasiswa(nim, nama, jurusan))
                            .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    editTextNIm.setText(null);
                                    editTextNama.setText("");
                                    editTextJurusan.setText("");
                                    buttonBlockLayout.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                }
                            });
                }


                //
            }
        });

        buttonTampilData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        /*
         * Mendeteksi apakah ada user yang masuk, Jika tidak, maka setiap komponen UI akan dinonaktifkan
         * Kecuali Tombol Login. Dan jika ada user yang terautentikasi, semua fungsi/komponen
         * didalam User Interface dapat digunakan, kecuali tombol Logout
         */
        if(auth.getCurrentUser() == null){
            defaultUI();
        }else {
            updateUI();
        }

    }



    //Tampilan Default pada Activity jika user belum terautentikasi / belum login
    public void defaultUI(){
        buttonLogin.setEnabled(true);
        buttonLogout.setEnabled(false);
        buttonSimpan.setEnabled(false);
        buttonTampilData.setEnabled(false);
        editTextNIm.setEnabled(false);
        editTextNama.setEnabled(false);
        editTextJurusan.setEnabled(false);
    }

    //Tampilan User Interface pada Activity setelah user Terautentikasi / Login
    public void updateUI(){
        buttonLogin.setEnabled(false);
        buttonLogout.setEnabled(true);
        buttonSimpan.setEnabled(true);
        buttonTampilData.setEnabled(true);
        editTextNIm.setEnabled(true);
        editTextNama.setEnabled(true);
        editTextJurusan.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    //Mengecek data yang kosong
    private boolean isEmpty(String s){
        return TextUtils.isEmpty(s);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN adalah kode permintaan yang Anda berikan ke startActivityForResult,
        // saat memulai masuknya arus.

        if(requestCode == RC_SIGN_IN){
            //Berhasil mmasuk
            if(resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
                updateUI();
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Login Gagal", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}