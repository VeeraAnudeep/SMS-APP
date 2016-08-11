package veera.smartmessager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by veera on 8/8/16.
 */
public class MessagesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.et_chat_msg)
    EditText editText;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.iv_send_msg)
    ImageView sendMessage;

    private MessagesAdapter messagesAdapter;
    private String address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_messages);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        messagesAdapter = new MessagesAdapter(this);
        recyclerView.setAdapter(messagesAdapter);
        sendMessage.setOnClickListener(this);
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        toolbar.setTitle(address);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportLoaderManager().initLoader(2, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Telephony.Sms.CONTENT_URI, null, "address=? ", new String[]{address}, Telephony.Sms.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        messagesAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean hasTelephony() {

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return !(telephonyManager == null || telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send_msg:
                    String message = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(message)) {
                        return;
                    }
                    SmsManager smsManager = SmsManager.getDefault();
                if (hasTelephony()) {
                    smsManager.sendTextMessage(address, null, message, null, null);
                } else {
                    Toast.makeText(this, "Make sure you have sim ready to send a message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
