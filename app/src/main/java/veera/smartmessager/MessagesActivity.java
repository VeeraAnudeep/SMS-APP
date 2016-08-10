package veera.smartmessager;

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
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send_msg:
                String message = editText.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(address, null, message, null, null);
                break;
        }
    }
}
