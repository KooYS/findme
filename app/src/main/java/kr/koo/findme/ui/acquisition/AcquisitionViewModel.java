package kr.koo.findme.ui.acquisition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AcquisitionViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AcquisitionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is send fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}