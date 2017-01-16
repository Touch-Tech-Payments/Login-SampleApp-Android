package com.touchtechpayments.androidloginsdk;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.touchtechpayments.loginsdk.TTLogin;
import com.touchtechpayments.loginsdk.data.model.AuthPin;
import com.touchtechpayments.loginsdk.data.model.Method;
import com.touchtechpayments.loginsdk.data.model.Token;
import com.touchtechpayments.loginsdk.data.realm.TTLAccount;
import com.touchtechpayments.loginsdk.interfaces.TTLoginCallback;
import com.touchtechpayments.loginsdk.interfaces.TTLoginFingerprintCallback;

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
        //Add QR Code Scanner here or paste a token below
        String token = "5olfCqi5-s0tPGnv_v_MciO4DlJqQQxv_aqvMKqZRBhCrB_BTfORrPnhxQh4KuTY";
        resultFromQRScanner = new Token(token);

        tokenEditor.setText(token);
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
        } else {
            Toast.makeText(LoginActivity.this, "Fallback here.", Toast.LENGTH_SHORT).show();
            //TODO: Add Fallback here
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TTLogin.setIsDev(true);
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
        Toast.makeText(this, "Please scan your fingerprint now...", Toast.LENGTH_SHORT).show();
        ttLogin.authenticateFingerprint(ttlAccount, new TTLoginFingerprintCallback() {
            @Override
            public void onError(Throwable e) {
                genericError(e);
            }

            @Override
            public void onFallback() {
                Toast.makeText(LoginActivity.this, "Fallback here. Tried 3 times.", Toast.LENGTH_SHORT).show();
                //TODO: Add Fallback here
            }

            @Override
            public void onSuccess(AuthPin authPin) {
                Toast.makeText(LoginActivity.this, "Fingerprint recognized, please wait...", Toast.LENGTH_SHORT).show();
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
