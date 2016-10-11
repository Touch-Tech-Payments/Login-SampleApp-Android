package com.touchtechpayments.androidloginsdk;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.touchtechpayments.loginsdk.TTLogin;
import com.touchtechpayments.loginsdk.data.model.AuthPin;
import com.touchtechpayments.loginsdk.data.model.Method;
import com.touchtechpayments.loginsdk.data.model.Token;
import com.touchtechpayments.loginsdk.data.realm.TTLAccount;
import com.touchtechpayments.loginsdk.interfaces.TTLoginAuthFingerprintCallback;
import com.touchtechpayments.loginsdk.interfaces.TTLoginCallback;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class LoginActivity extends AppCompatActivity {

    private static final String STATUS_KEY = "STATUS";

    @Bind(R.id.token_editor)
    TextInputEditText tokenEditor;

    @Bind(R.id.pin_editor)
    TextInputEditText pinEditor;

    @Bind(R.id.text_view)
    TextView status;

    @Bind(R.id.listview)
    ListView accountList;

    private TTLogin ttLogin;

    private Token resultFromQRScanner;

    private ArrayList<TTLAccount> ttlAccounts = new ArrayList<>();
    private ArrayAdapter<TTLAccount> adapter = null;

    @OnClick(R.id.qrcode_scanner)
    public void getToken(){
        byte[] r = new byte[64];
        SecureRandom random = new SecureRandom();
        random.nextBytes(r);
        String base64String = Base64.encodeToString(r, Base64.DEFAULT).substring(0, 64);
        resultFromQRScanner = new Token(base64String);

        tokenEditor.setText(base64String);
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(int position) {

        if(resultFromQRScanner == null){
            updateMessage("You must 'scan' the QR code first. Click the box to simulate.");
            return;
        }

        final TTLAccount ttlAccount = ttlAccounts.get(position);

        if(ttlAccount.getMethod().equals(Method.FINGERPRINT.getServerFormat())){
            checkFingerprint(ttlAccount);
        } else { //use PIN they have entered
            AuthPin authPin = new AuthPin(pinEditor.getText().toString());
            authenticate(ttlAccount, authPin);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ttLogin = new TTLogin(Constants.API_KEY, this);

        setupView();

        if (savedInstanceState != null) {
            status.setText(savedInstanceState.getString(STATUS_KEY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATUS_KEY, status.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupView() {
        status.setMovementMethod(new ScrollingMovementMethod());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ttlAccounts);
        accountList.setAdapter(adapter);

        refreshAccounts();
    }

    private void updateMessage(String text) {
        status.setText(text);
        status.scrollTo(0, 0);
    }

    private void refreshAccounts() {
        ttlAccounts.clear();
        ttlAccounts.addAll(Arrays.asList(ttLogin.getAccounts()));
        adapter.notifyDataSetChanged();
    }

    private void checkFingerprint(final TTLAccount ttlAccount){
        Toast.makeText(this, "Please scan your fingerprint now...", Toast.LENGTH_LONG).show();
        ttLogin.authenticateFingerprint(ttlAccount, new TTLoginAuthFingerprintCallback() {
            @Override
            public void onError(Throwable e) {
                genericError(e);
            }

            @Override
            public void onFail() {
                Toast.makeText(LoginActivity.this, "Fingerprint not recognized, try again!", Toast.LENGTH_LONG).show();
                checkFingerprint(ttlAccount); //I would advise to check for 3 times max here
            }

            @Override
            public void onSuccess(AuthPin authPin) {
                Toast.makeText(LoginActivity.this, "Fingerprint recognized, please wait...", Toast.LENGTH_LONG).show();
                authenticate(ttlAccount, authPin);
            }
        });
    }

    private void authenticate(TTLAccount ttlAccount, AuthPin authPin){
        ttLogin.authenticate(ttlAccount, resultFromQRScanner, authPin, new TTLoginCallback() {
            @Override
            public void onError(Throwable e) {
                genericError(e);
            }

            @Override
            public void onSuccess() {
                updateMessage("Logged In!");
            }
        });
    }

    private void genericError(Throwable e){
        updateMessage(e.toString());
        e.printStackTrace();
        refreshAccounts();
    }
}
