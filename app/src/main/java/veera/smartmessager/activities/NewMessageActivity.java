package veera.smartmessager.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import veera.smartmessager.R;

/**
 * Created by veera on 13/8/16.
 */
public class NewMessageActivity extends MessagesBaseActivity implements View.OnClickListener {

    @BindView(R.id.phone)
    EditText phone;

    @BindView(R.id.et_chat_msg)
    EditText editText;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.iv_send_msg)
    ImageView sendMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_new_message);
        ButterKnife.bind(this);
        toolbar.setTitle("New Message");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sendMessage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send_msg:
                String message = editText.getText().toString().trim();
                String address = phone.getText().toString().trim();
                sendSms(address, message);
                editText.setText("");
                Toast.makeText(this, "Sending..", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
}
