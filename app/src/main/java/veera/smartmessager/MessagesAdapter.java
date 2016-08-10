package veera.smartmessager;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by veera on 8/8/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Cursor cursor;
    private LayoutInflater inflater;
    final int MESSAGE_SENT = 2;
    final int MESSAGE_RECEIVED = 1;


    public MessagesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        cursor.moveToPosition(position);
        switch (cursor.getInt(cursor.getColumnIndex("type"))) {
            case 1:
                return MESSAGE_RECEIVED;
            case 2:
                return MESSAGE_SENT;
            default:
                return super.getItemViewType(position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = inflater.inflate(R.layout.p_chat_receive_item, parent, false);
                return new MessageHolder(view);
            case 2:
                view = inflater.inflate(R.layout.p_chat_send_item, parent, false);
                return new MessageHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageHolder) {
            MessageHolder viewHolder = (MessageHolder) holder;
            viewHolder.message.setText(cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)));
            viewHolder.timestamp.setText(Utils.getDate(cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE)), "dd MMM"));
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public class MessageHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.message)
        TextView message;

        @BindView(R.id.timestamp)
        TextView timestamp;

        public MessageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
