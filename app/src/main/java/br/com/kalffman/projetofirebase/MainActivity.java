package br.com.kalffman.projetofirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth firebaseAuth;

    private GoogleApiClient googleApiClient;

    private ProgressDialog progressDialog;

    private EditText etEmailUserLogin, etSenhaUserLogin;
    private Button btUserLogin,btLogIn,btLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instancia do FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Efetuando Login. Por favor, aguarde...");

        etEmailUserLogin = findViewById(R.id.et_email_user_login);
        etSenhaUserLogin = findViewById(R.id.et_senha_user_login);
        btUserLogin = findViewById(R.id.bt_login_user);
        btLogIn = findViewById(R.id.bt_logIn);
        btLogOut = findViewById(R.id.bt_logOut);

        btUserLogin.setOnClickListener(this);
        btLogIn.setOnClickListener(this);
        btLogOut.setOnClickListener(this);

        //Config login com o Gmail
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder( GoogleSignInOptions.DEFAULT_SIGN_IN )
                .requestIdToken( getString( R.string.default_web_client_id ) )
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }

    private void singIn(){
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,1);
    }

    private void singOut(){
        progressDialog.setTitle("Atenção");
        progressDialog.setMessage("Realizando SingOut...");
        progressDialog.show();
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient)
            .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this,"SignOut Realizado com sucesso!!",Toast.LENGTH_SHORT).show();
                }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                fireBaseLoginGoogle(account);
            }
        }
    }

    private void fireBaseLoginGoogle(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        progressDialog.setTitle("LogIn");
        progressDialog.setMessage("Carregando...");
        progressDialog.show();
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()){
                        startActivity( new Intent(MainActivity.this,PrincipalActivity.class));
                    }else{
                        Toast.makeText(MainActivity.this,"Falha na autenticação de login com Gmail",Toast.LENGTH_SHORT).show();
                    }
                }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_login_user:
                String userEmail = etEmailUserLogin.getText().toString();
                String userPass = etSenhaUserLogin.getText().toString();
                Log.i("APP","email: "+userEmail+"\npass: "+userPass);
                if ( !userEmail.isEmpty() && !userPass.isEmpty() ) {
                    progressDialog.show();
                    firebaseAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Email e/ou senha inválido(s)", Toast.LENGTH_SHORT).show();
                        }
                        }
                    });
                }else{
                    Toast.makeText(this,"Campos vazios",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_logIn:
                singIn();
                break;
            case R.id.bt_logOut:
                singOut();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}