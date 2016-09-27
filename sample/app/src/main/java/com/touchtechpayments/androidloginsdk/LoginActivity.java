package com.touchtechpayments.androidloginsdk;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.touchtechpayments.loginsdk.TTLogin;
import com.touchtechpayments.loginsdk.data.model.AuthPin;
import com.touchtechpayments.loginsdk.data.model.Token;
import com.touchtechpayments.loginsdk.data.realm.TTLAccount;
import com.touchtechpayments.loginsdk.interfaces.TTLoginCallback;

import org.apache.commons.codec.binary.Base64;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class LoginActivity extends AppCompatActivity {

    private static final String STATUS_KEY = "STATUS";

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
        String base64String = new String(Base64.encodeBase64(r)).substring(0, 64);
        resultFromQRScanner = new Token(base64String);
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(int position) {
        AuthPin authPin = new AuthPin(pinEditor.getText().toString());

        if(resultFromQRScanner == null){
            updateMessage("You must 'scan' the QR code first. Click the box to simulate.");
            return;
        }

        ttLogin.authenticate(ttlAccounts.get(position), resultFromQRScanner, authPin, new TTLoginCallback() {
            @Override
            public void onError(Throwable e) {
                updateMessage(e.toString());
                e.printStackTrace();
                refreshAccounts();
            }

            @Override
            public void onSuccess() {
                updateMessage("Logged In!");
            }
        });
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

    public void refreshAccounts() {
        ttlAccounts.clear();
        ttlAccounts.addAll(Arrays.asList(ttLogin.getAccounts()));
        adapter.notifyDataSetChanged();
    }
}
