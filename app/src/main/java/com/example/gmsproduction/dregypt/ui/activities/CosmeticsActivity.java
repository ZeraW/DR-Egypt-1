package com.example.gmsproduction.dregypt.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.CosmeticClinicsRequests.SearchCosmeticClinicsRequest;
import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.ProductAdsRequests.GetFavoriteProducts;
import com.example.gmsproduction.dregypt.R;
import com.example.gmsproduction.dregypt.ui.adapters.CosmeticClinicsAdapter;
import com.example.gmsproduction.dregypt.ui.fragments.NoInternt_Fragment;
import com.example.gmsproduction.dregypt.utils.Constants;
import com.example.gmsproduction.dregypt.Models.CosmeticModel;
import com.example.gmsproduction.dregypt.utils.Utils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CosmeticsActivity extends BaseActivity {


    String MY_PREFS_NAME = "FiltersCos";

    private RecyclerView mRecyclerView;
    private CosmeticClinicsAdapter mAdapter;
    private ArrayList<CosmeticModel> modelArrayList=new ArrayList<>();
    String id, title, description, price, image, status, address, created_at, phone_1, phone_2, rating_count, email, website;
    Double rating_read;
    int rating_counts;
    MaterialSearchView searchView;
    Map<String, String> body = new HashMap<>();
    String url = Constants.basicUrl + "/cosmetic-clinics/search";
    String test;
    ProgressBar progressBar;
    private ArrayList<Integer> favArray = new ArrayList<>();

    TextView txtFilter , txtSort;
    LinearLayoutManager linearLayoutManager;
    int page = 1;
    int last_page,language,userID;
    private String lang;
    private String sortKey,sortValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cosmetics);
        language = getIdLANG();
        localization(language);
        lang = checkLanguage(language);
        SharedPreferences prefs = getSharedPreferences(Constants.USER_DETAILS, MODE_PRIVATE);
        userID = prefs.getInt("User_id", 0);

        //fragmentManager = getSupportFragmentManager();

        //Request for all Main products

        //recycdler View
        mRecyclerView = findViewById(R.id.Recycler_Cosmetic);
        progressBar = (ProgressBar) findViewById(R.id.cosmetic_Progressbar);
        progressBar.setVisibility(View.VISIBLE);

        txtFilter = findViewById(R.id.filtering);
        txtSort = findViewById(R.id.sorting);
        txtFilter.setText(R.string.nameActivity_Filters);
        txtSort.setText(R.string.sorting);
        txtFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CosmeticsActivity.this, FiltersActivity.class);
                intent.putExtra("idFilter", 5);
                startActivity(intent);
            }
        });


        txtSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sorting();
            }
        });



        getCosmeticsPagenatin("");

        //Custom Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarxx_cosmetic);
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
                    mAdapter = new CosmeticClinicsAdapter(CosmeticsActivity.this, modelArrayList,favArray);
                    linearLayoutManager = new LinearLayoutManager(CosmeticsActivity.this);
                    mRecyclerView.setLayoutManager(linearLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);
                    getCosmeticsPagenatin(test);
                } else {
                    finish();
                }
            }
        });

        //Search Related
        searchView = (MaterialSearchView) findViewById(R.id.search_view_cosmetic);
        searchView.setVoiceSearch(false);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                test = query;
                getCosmetics(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getCosmetics(newText);
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                getCosmetics(test);
            }
        });


        linearLayoutManager = new LinearLayoutManager(CosmeticsActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new CosmeticClinicsAdapter(CosmeticsActivity.this, modelArrayList,favArray);
        mRecyclerView.setAdapter(mAdapter);

        setActivityTitle("عيادات التجميل","Cosmetics");
        getFavID();
    }




    @Override
    protected void onStart() {
        super.onStart();
        favArray.clear();
        getFavID();
    }

    //menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int items = item.getItemId();

        switch (items) {
            case R.id.action_filters:
                Intent intent = new Intent(CosmeticsActivity.this, FiltersActivity.class);
                intent.putExtra("idFilter", 5);
                startActivity(intent);
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

    //still don't know how it's work
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void CosmeticResponse(String response) {
        modelArrayList = new ArrayList<>();

        Log.e("tagyyy", response);
        try {
            JSONObject object = new JSONObject(response);
            JSONArray dataArray = object.getJSONArray("data");
            for (int a = 0; a < dataArray.length(); a++) {
                JSONObject dataObject = dataArray.getJSONObject(a);
                id = dataObject.getString("id");
                title = dataObject.getString(lang+"_name");
                image = Constants.ImgUrl + dataObject.getString("img");
                address = dataObject.getString(lang+"_address");
                created_at = dataObject.getString("created_at");
                description = dataObject.getString(lang+"_note");
                email = dataObject.getString("email");
                website = dataObject.getString("website");
                try {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = (String) phoneArray.get(1);
                } catch (Exception e) {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = "No phone has been added";
                }
                JSONObject rateObject = dataObject.getJSONObject("rate");
                rating_read = rateObject.getDouble("rating");
                rating_counts = rateObject.getInt("count");

                modelArrayList.add(new CosmeticModel(id, title, description, image, address, email, website, created_at, phone_1, phone_2, rating_read, rating_counts));
            }

        } catch (JSONException e) {
            e.printStackTrace();


        }
        linearLayoutManager = new LinearLayoutManager(CosmeticsActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new CosmeticClinicsAdapter(CosmeticsActivity.this, modelArrayList,favArray);
        mRecyclerView.setAdapter(mAdapter);
    }

    //filters will be added here
    public void getCosmetics(String keyword) {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int city = prefs.getInt("city", 0); //0 is the default value.
        int area = prefs.getInt("area", 0); //0 is the default value.
        int rate = prefs.getInt("num_rate", 0); //0 is the default value.
        int speciality = prefs.getInt("speciality", 0); //0 is the default value.

        String sort_key= prefs.getString("order_by",""); //0 is the default value.
        String sort_value= prefs.getString("sortingValue",""); //0 is the default value.

        Log.e("CXAAAA", "city=" + city + "area=" + area + "rate=" + rate + "Specialty=" + speciality);


        body.put("region", String.valueOf(city));
        body.put("city", String.valueOf(area));
        body.put("rate", String.valueOf(rate));
        body.put("speciality", String.valueOf(speciality));
        body.put("keyword", keyword);

        body.put("orderBy",sort_key);
        body.put("sort", sort_value);



        final SearchCosmeticClinicsRequest searchProductAdRequest = new SearchCosmeticClinicsRequest(CosmeticsActivity.this, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CosmeticResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    progressBar.setVisibility(View.GONE);

                    /*fragmentManager
                            .beginTransaction()
                            .add(R.id.Cosmetic_Include, new NoInternt_Fragment(),
                                    Utils.Error).commit();*/
                    NoInternt_Fragment fragment = new NoInternt_Fragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("duck", 66);
                    fragment.setArguments(arguments);
                    final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.Cosmetic_Include, fragment, Utils.Error);
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
        }, 0);
        searchProductAdRequest.setBody((HashMap) body);
        searchProductAdRequest.start();
    }

    public void getCosmeticsPagenatin(String keyword) {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int city = prefs.getInt("city", 0); //0 is the default value.
        int area = prefs.getInt("area", 0); //0 is the default value.
        int rate = prefs.getInt("num_rate", 0); //0 is the default value.
        int speciality = prefs.getInt("speciality", 0); //0 is the default value.

        Log.e("CXAAAA", "city=" + city + "area=" + area + "rate=" + rate + "Specialty=" + speciality);


        body.put("region", String.valueOf(city));
        body.put("city", String.valueOf(area));
        body.put("rate", String.valueOf(rate));
        body.put("speciality", String.valueOf(speciality));
        body.put("keyword", keyword);

        final SearchCosmeticClinicsRequest searchProductAdRequest = new SearchCosmeticClinicsRequest(CosmeticsActivity.this, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PagenationResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    /*fragmentManager
                            .beginTransaction()
                            .add(R.id.Cosmetic_Include, new NoInternt_Fragment(),
                                    Utils.Error).commit();*/
                    NoInternt_Fragment fragment = new NoInternt_Fragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("duck", 66);
                    fragment.setArguments(arguments);
                    final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.Cosmetic_Include, fragment, Utils.Error);
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
        }, page);
        searchProductAdRequest.setBody((HashMap) body);
        searchProductAdRequest.start();
    }

    public void PagenationResponse(String response) {
        progressBar.setVisibility(View.GONE);

        Log.e("tagyyy", response);
        try {
            JSONObject object = new JSONObject(response);
            JSONArray dataArray = object.getJSONArray("data");
            for (int a = 0; a < dataArray.length(); a++) {
                JSONObject dataObject = dataArray.getJSONObject(a);
                id = dataObject.getString("id");
                title = dataObject.getString(lang+"_name");
                image = Constants.ImgUrl + dataObject.getString("img");
                address = dataObject.getString(lang+"_address");
                created_at = dataObject.getString("created_at");
                description = dataObject.getString(lang+"_note");
                email = dataObject.getString("email");
                website = dataObject.getString("website");
                try {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = (String) phoneArray.get(1);
                } catch (Exception e) {
                    JSONArray phoneArray = dataObject.getJSONArray("phone");
                    phone_1 = (String) phoneArray.get(0);
                    phone_2 = "No phone has been added";
                }
                JSONObject rateObject = dataObject.getJSONObject("rate");
                rating_read = rateObject.getDouble("rating");
                rating_counts = rateObject.getInt("count");

                modelArrayList.add(new CosmeticModel(id, title, description, image, address, email, website, created_at, phone_1, phone_2, rating_read, rating_counts));
            }

            JSONObject meta = object.getJSONObject("meta");
            last_page = meta.getInt("last_page");


            Log.e("laaaast_page=", last_page + "");



        } catch (JSONException e) {
            e.printStackTrace();




        }
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                page++;

                Log.e("PageeeNext=", "" + page);
                if (page > last_page) {

                } else {
                    getCosmeticsPagenatin("");

                }


            }
        });

        mAdapter.notifyItemRangeInserted(mAdapter.getItemCount(), modelArrayList.size());


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("onStop", "onStop");
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt("num_rate", 0);
        editor.putInt("city", 0);
        editor.putInt("area", 0);
        editor.putInt("speciality", 0);

        editor.putString("order_by", "");
        editor.putString("sortingValue", "");

        editor.apply();

    }

    private void getFavID() {
        body.put("category", "cosmetic");
        GetFavoriteProducts getFavId = new GetFavoriteProducts(CosmeticsActivity.this, userID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("FavPage", "cos" + response);
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
        MaterialDialog dialog = new MaterialDialog.Builder(CosmeticsActivity.this)
                .title(R.string.sort)
                .titleColor(getResources().getColor(R.color.black))
                .customView(R.layout.sorting_medical, true)
                .positiveText(R.string.aapply)
                .positiveColor(getResources().getColor(R.color.colorPrimary))

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {

                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("order_by",sortKey);
                        editor.putString("sortingValue",sortValue);
                        editor.apply();

                        getCosmetics("");
                    }
                })
                .show();
        View views = dialog.getCustomView();

        RadioGroup radioGroupType = views.findViewById(R.id.Cosmetic_rate_RadioGroup);
        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                switch (checkedId) {

                    case R.id.CosmeticRadio_LTH:
                        sortKey="rate";
                        sortValue="asc";
                        break;
                    case R.id.CosmeticRadio_HTL:
                        sortKey="rate";
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
