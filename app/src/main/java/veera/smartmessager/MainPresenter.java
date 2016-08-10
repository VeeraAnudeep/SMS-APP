package veera.smartmessager;

/**
 * Created by veera on 8/8/16.
 */
public interface MainPresenter {
    void setItems();
    void onDestroy();
    void onItemClicked(int position);
}
