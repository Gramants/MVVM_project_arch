package example.com.mvvmarchcomp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import example.com.mvvmarchcomp.App;
import example.com.mvvmarchcomp.entities.IssueDataModel;
import example.com.mvvmarchcomp.repositories.IssueRepository;
import example.com.mvvmarchcomp.repositories.preferences.PersistentStorageProxy;


public class ListIssuesViewModel extends AndroidViewModel {

    //https://stackoverflow.com/questions/44270577/android-lifecycle-library-viewmodel-using-dagger-2

    private MediatorLiveData<List<IssueDataModel>> mApiResponse;
    final MutableLiveData<String> livedatasavedstring = new MutableLiveData<>();

    @Inject
    IssueRepository mIssueRepository;

    @Inject
    PersistentStorageProxy mPersistentStorageProxy;

    public ListIssuesViewModel(Application application) {
        super(application);
        mApiResponse = new MediatorLiveData<>();
        ((App) application).getIssueRepositoryComponent().inject(this);

    }


    @NonNull
    public LiveData<List<IssueDataModel>> getApiResponse() {
        return mApiResponse;
    }


    public LiveData<List<IssueDataModel>> loadIssues(@NonNull String user, String repo, Boolean forceremote) {
        // https://stackoverflow.com/questions/45679896/android-mediatorlivedata-observer
        mApiResponse.addSource(
                mIssueRepository.getIssues(user, repo, forceremote),
                apiResponse -> mApiResponse.setValue(apiResponse)
        );


        //https://github.com/florent37/NewAndroidArchitecture-Component-Github
        // databinding
        return mApiResponse;
    }

    public void deleteRecordById(Integer id) {
        mIssueRepository.deleteRecordById(id);
    }

    public void saveSearchString(String searchstring) {
        mPersistentStorageProxy.setSearchString(searchstring);
    }

    public LiveData<String> getSearchString() {
        livedatasavedstring.setValue( mPersistentStorageProxy.getSearchString());
        return livedatasavedstring;
    }

}