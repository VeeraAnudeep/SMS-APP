package veera.smartmessager;

/**
 * Created by veera on 8/8/16.
 */
public class MainPresenterImpl implements MainPresenter {


    MainView mainView;

    public MainPresenterImpl(MainView mainView) {
        this.mainView = mainView;
    }

    @Override
    public void setItems() {

    }

    @Override
    public void onDestroy() {
        mainView = null;
    }

    @Override
    public void onItemClicked(int position) {

    }
}
