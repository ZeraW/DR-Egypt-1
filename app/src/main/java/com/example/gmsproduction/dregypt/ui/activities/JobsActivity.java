package com.example.gmsproduction.dregypt.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.gmsproduction.dregypt.Data.localDataSource.EndlessRecyclerOnScrollListener;
import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.JobAdsRequests.SearchJobAdRequest;
import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.ProductAdsRequests.GetFavoriteProducts;
import com.example.gmsproduction.dregypt.Models.JobsModel;
import com.example.gmsproduction.dregypt.R;
import com.example.gmsproduction.dregypt.ui.adapters.JobAdsAdapter;
import com.example.gmsproduction.dregypt.ui.fragments.NoInternt_Fragment;
import com.example.gmsproduction.dregypt.utils.Constants;
import com.example.gmsproduction.dregypt.utils.Utils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JobsActivity extends BaseActivity {


    String MY_PREFS_NAME = "FiltersJob";

    private RecyclerView mRecyclerView;
    private JobAdsAdapter mAdapter;
    private ArrayList<JobsModel> modelArrayList=new ArrayList<>();
    String id, userId, title, description, salary, image, status, address, created_at, phone_1, phone_2, category, experience, education_level, employment_type;
    MaterialSearchView searchView;
    Map<String, String> body = new HashMap<>();
    String url = Constants.basicUrl + "/job-ads/search";
    String test;
    private FragmentManager fragmentManager;
    private ArrayList<Integer> favArray = new ArrayList<>();
    TextView txtFilter , txtSort,addJob;

    LinearLayoutManager LayoutManagaer;
    int page = 1;
    int last_page,mUSERid,language;
    private String lang;
    private String sortKey,sortValue;

    @Override
    protected void onStart() {
        super.onStart();
        favArray.clear();
        getFavID();
    }
    @Override
    protected void onCreate(Bundle  savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        fragmentManager = getSupportFragmentManager();

        SharedPreferences prefs = getSharedPreferences(Constants.USER_DETAILS, MODE_PRIVATE);
        mUSERid = prefs.getInt("User_id", 0);
        language = getIdLANG();
        localization(language);
        lang=checkLanguage(language);


        txtFilter = findViewById(R.id.filtering);
        txtSort = findViewById(R.id.sorting);
        addJob = findViewById(R.id.addJob);
        addJob.setText(R.string.addjob);
        txtFilter.setText(R.string.nameActivity_Filters);
        txtSort.setText(R.string.sorting);
        txtFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(JobsActivity.this, FiltersActivity.class);
                intent.putExtra("idFilter", 6);
                startActivity(intent);
            }
        });
        addJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (mUSERid==0){
                    intent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(intent);
                }else {
                    intent = new Intent(getApplicationContext(), AddItemActivity.class);
                    intent.putExtra("Add", 2002);
                    startActivity(intent);
                }
            }
        });

        txtSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sorting();
            }
        });


        //Recycle
        mRecyclerView = findViewById(R.id.Recycler_Jobs);

        getJobsPagentaion("");

        //Custom Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Jobs);
        setSupportActionBar(toolbar);

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(getBackArrow(language)));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (test != null && !test.isEmpty()) {
                    test = "";
                    modelArrayList = new ArrayList<>();
                    page = 1;
                    mAdapter = new JobAdsAdapter(JobsActivity.this, modelArrayList,favArray);
                    LayoutManagaer = new LinearLayoutManager(JobsActivity.this);
                    mRecyclerView.setLayoutManager(LayoutManagaer);
                    mRecyclerView.setAdapter(mAdapter);
                    getJobsPagentaion(test);
                } else {
                    finish();
                }
            }
        });

        //Search Related
        searchView = (MaterialSearchView) findViewById(R.id.search_view_Jobs);
        searchView.setVoiceSearch(false);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                test = query;
                getJobs(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getJobs(newText);
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                getJobs(test);
            }
        });

        LayoutManagaer = new LinearLayoutManager(JobsActivity.this);
        mRecyclerView.setLayoutManager(LayoutManagaer);
        mAdapter = new JobAdsAdapter(JobsActivity.this, modelArrayList,favArray);
        mRecyclerView.setAdapter(mAdapter);

        setActivityTitle("الوظائف","Jobs");
        getFavID();
    }

    //menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem itemAdd = menu.findItem(R.id.action_AddPro);
        //itemAdd.setVisible(true);
        searchView.setMenuItem(item);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int items = item.getItemId();

        switch (items) {
            case R.id.action_filters:
                Intent intent = new Intent(JobsActivity.this, FiltersActivity.class);
                intent.putExtra("idFilter", 6);
                startActivity(intent);
                break;
            case R.id.action_AddPro:
                if (mUSERid==0){
                    intent = new Intent(this, LogInActivity.class);
                    startActivity(intent);
                }else {
                    intent = new Intent(this, AddItemActivity.class);
                    intent.putExtra("Add", 2002);
                    startActivity(intent);
                }
                break;
        }
        return true;

    }
    //on back press
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }


    public void getJobs(String keyword) {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int city = prefs.getInt("city", 0); //0 is the default value.
        int area = prefs.getInt("area", 0); //0 is the default value.
        int rate = prefs.getInt("num_rate", 0); //0 is the default value.
        int category = prefs.getInt("category", 0); //0 is the default value.
        int type = prefs.getInt("type", 0); //0 is the default value.
        int expLevel = prefs.getInt("experienceLevel", 0); //0 is the default value.
        int eduLevel = prefs.getInt("educationLevel", 0); //0 is the default value.

        String sort_key= prefs.getString("order_by",""); //0 is the default value.
        String sort_value= prefs.getString("sortingValue",""); //0 is the default value.






        Log.e("CXAAAA", "city=" + city + "area=" + area + "rate=" + rate + "Category=" + category+"Type="+type+"ExperienceLevel="+expLevel+"educationLevel="+eduLevel);


       body.put("region", String.valueOf(city));
        body.put("city", String.valueOf(area));
        body.put("rate", String.valueOf(rate));
        body.put("category", String.valueOf(category));
        body.put("type", String.valueOf(type));
        body.put("experienceLevel", String.valueOf(expLevel));
        body.put("educationLevel", String.valueOf(eduLevel));
        body.put("keyword",keyword);

        body.put("orderBy",sort_key);
        body.put("sort", sort_value);




        final SearchJobAdRequest searchJobAdRequest = new SearchJobAdRequest(JobsActivity.this, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JobsResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    NoInternt_Fragment fragment = new NoInternt_Fragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("duck", 77);
                    fragment.setArguments(arguments);
                    final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.Jobs_Include, fragment, Utils.Error);
                    ft.commitAllowingStateLoss();

                } else if (error instanceof AuthFailureError) {
                    //TODO

                } else if (error instanceof ServerError) {
                    //TODO
                } else if (error instanceof NetworkError) {
                    //TODO
                } else if (error instanceof ParseError) {
                    //TODO

                }

            }
        },0);
        searchJobAdRequest.setBody((HashMap) body);
        searchJobAdRequest.start();
    }

    public void JobsResponse(String response) {
        modelArrayList = new ArrayList<>();

        Log.e("tagyyy", response);
        try {
            JSONObject object = new JSONObject(response);
            JSONArray dataArray = object.getJSONArray("data");
            for (int a = 0; a < dataArray.length(); a++) {
                JSONObject dataObject = dataArray.getJSONObject(a);
                id = dataObject.getString("id");
                userId = dataObject.getString("user_id");
                title = dataObject.getString("title");
                description = dataObject.getString("description");
                salary = dataObject.getString("salary");
                image = Constants.ImgUrl + dataObject.getString("img");
                address = dataObject.getString("address");
                created_at = dataObject.getString("created_at");
                try {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = (String) phoneArray.get(1);
                } catch (Exception e) {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = "No phone has been added";
                }
                JSONObject categoryObject = dataObject.getJSONObject("category");
                category = categoryObject.getString(lang+"_name");
                JSONObject experienceObject = dataObject.getJSONObject("experience_level");
                experience = experienceObject.getString(lang+"_name");
                JSONObject educationObject = dataObject.getJSONObject("education_level");
                education_level = educationObject.getString(lang+"_name");
                JSONObject employmentObject = dataObject.getJSONObject("employment_type");
                employment_type = employmentObject.getString(lang+"_name");


                modelArrayList.add(new JobsModel(id, userId, title, description, salary, image, status, address, created_at, phone_1, phone_2, category, experience, education_level, employment_type));
            }

        } catch (JSONException e) {
            e.printStackTrace();

        }
        LayoutManagaer = new LinearLayoutManager(JobsActivity.this);
        mRecyclerView.setLayoutManager(LayoutManagaer);
        mAdapter = new JobAdsAdapter(JobsActivity.this, modelArrayList,favArray);
        mRecyclerView.setAdapter(mAdapter);

    }

    public void PagenationResponse(String response) {

        Log.e("tagyyy", response);
        try {
            JSONObject object = new JSONObject(response);
            JSONArray dataArray = object.getJSONArray("data");
            for (int a = 0; a < dataArray.length(); a++) {
                JSONObject dataObject = dataArray.getJSONObject(a);
                id = dataObject.getString("id");
                userId = dataObject.getString("user_id");
                title = dataObject.getString("title");
                description = dataObject.getString("description");
                salary = dataObject.getString("salary");
                image = Constants.ImgUrl + dataObject.getString("img");
                address = dataObject.getString("address");
                created_at = dataObject.getString("created_at");
                try {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = (String) phoneArray.get(1);
                } catch (Exception e) {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = "No phone has been added";
                }
                JSONObject categoryObject = dataObject.getJSONObject("category");
                category = categoryObject.getString(lang+"_name");
                JSONObject experienceObject = dataObject.getJSONObject("experience_level");
                experience = experienceObject.getString(lang+"_name");
                JSONObject educationObject = dataObject.getJSONObject("education_level");
                education_level = educationObject.getString(lang+"_name");
                JSONObject employmentObject = dataObject.getJSONObject("employment_type");
                employment_type = employmentObject.getString(lang+"_name");


                modelArrayList.add(new JobsModel(id, userId, title, description, salary, image, status, address, created_at, phone_1, phone_2, category, experience, education_level, employment_type));
            }

            JSONObject meta = object.getJSONObject("meta");
            last_page = meta.getInt("last_page");

            Log.e("PageeeCurrent=", page + "");


        } catch (JSONException e) {
            e.printStackTrace();

        }
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(LayoutManagaer) {
            @Override
            public void onLoadMore(int current_page) {

                page++;

                Log.e("PageeeNext=", "" + page);
                if (page > last_page) {

                } else {
                    getJobsPagentaion("");

                }


            }
        });

        mAdapter.notifyItemRangeInserted(mAdapter.getItemCount(), modelArrayList.size());

    }

    public void getJobsPagentaion(String keyword) {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int city = prefs.getInt("city", 0); //0 is the default value.
        int area = prefs.getInt("area", 0); //0 is the default value.
        int rate = prefs.getInt("num_rate", 0); //0 is the default value.
        int category = prefs.getInt("category", 0); //0 is the default value.
        int type = prefs.getInt("type", 0); //0 is the default value.
        int expLevel = prefs.getInt("experienceLevel", 0); //0 is the default value.
        int eduLevel = prefs.getInt("educationLevel", 0); //0 is the default value.

        String sort_key= prefs.getString("order_by",""); //0 is the default value.
        String sort_value= prefs.getString("sortingValue",""); //0 is the default value.








        Log.e("CXAAAA", "city=" + city + "area=" + area + "rate=" + rate + "Category=" + category+"Type="+type+"ExperienceLevel="+expLevel+"educationLevel="+eduLevel+"SortKey="+sort_key+"SortValue="+sort_value);


        body.put("region", String.valueOf(city));
        body.put("city", String.valueOf(area));
        body.put("rate", String.valueOf(rate));
        body.put("category", String.valueOf(category));
        body.put("type", String.valueOf(type));
        body.put("experienceLevel", String.valueOf(expLevel));
        body.put("educationLevel", String.valueOf(eduLevel));

        body.put("orderBy",sort_key);
        body.put("sort", sort_value);



        body.put("keyword",keyword);


        final SearchJobAdRequest searchJobAdRequest = new SearchJobAdRequest(JobsActivity.this, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PagenationResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    NoInternt_Fragment fragment = new NoInternt_Fragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("duck", 77);
                    fragment.setArguments(arguments);
                    final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.Jobs_Include, fragment, Utils.Error);
                    ft.commitAllowingStateLoss();

                } else if (error instanceof AuthFailureError) {
                    //TODO

                } else if (error instanceof ServerError) {
                    //TODO
                } else if (error instanceof NetworkError) {
                    //TODO
                } else if (error instanceof ParseError) {
                    //TODO

                }

            }
        },page);
        searchJobAdRequest.setBody((HashMap) body);
        searchJobAdRequest.start();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.e("onStop","onStop");
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt("num_rate", 0);
        editor.putInt("city", 0);
        editor.putInt("area", 0);
        editor.putInt("speciality", 0);
        editor.putInt("type", 0);
        editor.putInt("experienceLevel", 0);
        editor.putInt("educationLevel", 0);

        editor.putString("order_by", "");
        editor.putString("sortingValue", "");



        editor.apply();

    }

    private void getFavID() {
        body.put("category", "job");
        GetFavoriteProducts getFavId = new GetFavoriteProducts(JobsActivity.this, mUSERid, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("FavPage", "res" + response);
                try {
                    JSONArray array = new JSONArray(response);
                    for (int a = 0; a < array.length(); a++) {
                        JSONObject object = array.getJSONObject(a);
                        int favourable_id = object.getInt("favourable_id");
                        favArray.add(favourable_id);
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        getFavId.setBody((HashMap) body);
        getFavId.start();

    }

    private void sorting(){
        MaterialDialog dialog = new MaterialDialog.Builder(JobsActivity.this)
                .title(R.string.sort)
                .titleColor(getResources().getColor(R.color.black))
                .customView(R.layout.sorting_jobs, true)
                .positiveText(R.string.aapply)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        Log.e("Sorting=",sortKey+"  "+sortValue);


                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("order_by",sortKey);
                        editor.putString("sortingValue",sortValue);
                        editor.apply();

                        getJobs("");


                    }
                })
                .show();
        View views = dialog.getCustomView();

        RadioGroup radioGroupType = views.findViewById(R.id.Job_rate_RadioGroup);

        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                switch (checkedId) {

                    case R.id.JobRadio_Oldest:
                        sortKey="updated_at";
                        sortValue="asc";

                        break;
                    case R.id.JobRadio_Newest:
                        sortKey="updated_at";
                        sortValue="desc";

                        break;

                    case R.id.JobSalaryRadio_LTH:
                        sortKey="salary";
                        sortValue="asc";

                        break;
                    case R.id.JobSalaryRadio_HTL:
                        sortKey="salary";
                        sortValue="desc";

                        break;

                    default:
                        sortKey="";
                        sortValue="";

                        break;
                }
            }
        });


    }
}
