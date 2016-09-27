package com.touchtechpayments.androidloginsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.touchtechpayments.loginsdk.TTLogin;
import com.touchtechpayments.loginsdk.data.model.AuthPin;
import com.touchtechpayments.loginsdk.data.model.Method;
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

public class MainActivity extends AppCompatActivity implements TTLoginCallback {

    private static final String STATUS_KEY = "STATUS";

    @Bind(R.id.token_editor)
    TextInputEditText tokenEditor;

    @Bind(R.id.pin_editor)
    TextInputEditText pinEditor;

    @Bind(R.id.fabSend)
    FloatingActionButton fabSend;

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

        tokenEditor.setText(base64String);
    }

    @OnClick(R.id.fabSend)
    public void fabSendClick() {
        updateMessage("LOADING");

        if(resultFromQRScanner == null){
            updateMessage("You must 'scan' the QR code first. Click the box to simulate.");
            return;
        }

        try {
            fabSend.setClickable(false);

            String pinInput = pinEditor.getText().toString();

            TTLAccount ttlAccount = new TTLAccount(resultFromQRScanner, Method.PIN);
            AuthPin authPin = new AuthPin(pinInput);

            ttLogin.register(ttlAccount, authPin, this);
        } catch (Exception e) {
            onError(e);
        }
    }

    @OnItemClick(R.id.listview)
    public void onItemClick(int position) {
        updateMessage("Removing " + ttlAccounts.get(position).getAccountToken());

        try {
            ttLogin.unregister(ttlAccounts.get(position), this);
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onError(Throwable e) {
        fabSend.setClickable(true);
        updateMessage(e.toString());
        e.printStackTrace();

        refreshAccounts();
    }

    @Override
    public void onSuccess() {
        fabSend.setClickable(true);
        updateMessage("Success!");

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
