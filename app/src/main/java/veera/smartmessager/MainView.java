package veera.smartmessager;

import android.database.Cursor;

/**
 * Created by veera on 8/8/16.
 */
public interface MainView extends ClickListener {
    void setItems(Cursor cursor);
}
