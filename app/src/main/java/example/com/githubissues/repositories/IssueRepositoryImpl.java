package example.com.githubissues.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import example.com.githubissues.App;
import example.com.githubissues.entities.IssueDataModel;
import example.com.githubissues.entities.pojos.Issue;
import example.com.githubissues.repositories.api.GithubApiService;
import example.com.githubissues.repositories.database.DbAsyncOp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class IssueRepositoryImpl implements IssueRepository {

    public static final String BASE_URL = "https://api.github.com/";
    private GithubApiService mApiService;

    public IssueRepositoryImpl() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        mApiService = retrofit.create(GithubApiService.class);
    }




    public LiveData<List<IssueDataModel>> getIssues(String owner, String repo, Boolean forceRemote) {
        final MutableLiveData<List<IssueDataModel>> liveData = new MutableLiveData<>();

        if (forceRemote)
        {
        Call<List<Issue>> call = mApiService.getIssues(owner, repo);
        call.enqueue(new Callback<List<Issue>>() {
            @Override
            public void onResponse(Call<List<Issue>> call, Response<List<Issue>> response) {

                ArrayList<IssueDataModel> transformed=new ArrayList();
                transformed=LinearizeIssue(response);
                deleteTableAndSaveDataToLocal(transformed);
                liveData.setValue(transformed);
            }

            @Override
            public void onFailure(Call<List<Issue>> call, Throwable t) {
                liveData.setValue(null);
            }
        });


            return liveData;
        }
        else
        {
            // pick from the DB
            Log.e("STEFANO","carico da tabella locale!");
            return App.get().getDB().issueDao().getAllIssue();

        }

    }

    private ArrayList<IssueDataModel> LinearizeIssue(Response<List<Issue>> issues) {
        ArrayList<IssueDataModel> transformed=new ArrayList();

        for (Issue issue : issues.body()) {
            transformed.add(new IssueDataModel(issue.getId(),issue.getUrl(),issue.getRepositoryUrl(),issue.getNumber(),issue.getTitle(),issue.getState(),issue.getCreatedAt(),issue.getBody(),issue.getUser().getLogin(),issue.getUser().getUrl()));
        }

        return transformed;
    }

    @Override
    public LiveData<IssueDataModel> getIssueFromDb(int id) {
        return App.get().getDB().issueDao().getIssueById(id);

    }

    @Override
    public void deleteRecordById(int id) {
        new DbAsyncOp.DeleteIssueByIdAsyncTask(App.get().getDB()).execute(id);
    }


    private void deleteTableAndSaveDataToLocal(ArrayList<IssueDataModel> issues) {
        new DbAsyncOp.AddIssueAsyncTask(App.get().getDB()).execute(issues);
    }





}