package com.example.gmsproduction.dregypt.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.CosmeticClinicsRequests.AddCosmeticClinicsToFavouriteRequest;
import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.CosmeticClinicsRequests.DeleteCosmeticClinicsFromFavouriteRequest;
import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.CosmeticClinicsRequests.RatingCosmeticClinicsRequest;
import com.example.gmsproduction.dregypt.Data.remoteDataSource.NetworkRequests.CosmeticClinicsRequests.ViewsIncrementForCosmeticClinicsRequest;
import com.example.gmsproduction.dregypt.R;
import com.example.gmsproduction.dregypt.Models.CosmeticModel;
import com.example.gmsproduction.dregypt.ui.activities.DetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class CosmeticClinicsAdapter extends RecyclerView.Adapter<CosmeticClinicsAdapter.MyViewHolder> {
    final String basicImgUrl = "http://gms-sms.com:89";
    Context context;
    private ArrayList<CosmeticModel> mArrayList;
    int LastPosition = -1;
    //RecyclerViewClickListener ClickListener ;

    ArrayList<Integer> favArray;

    public CosmeticClinicsAdapter() {
    }

    public CosmeticClinicsAdapter(Context context, ArrayList<CosmeticModel> mArrayList, ArrayList<Integer> favArray) {
        this.context = context;
        this.mArrayList = mArrayList;
        this.favArray = favArray;

    }

    /* public void setClickListener(RecyclerViewClickListener clickListener){
        this.ClickListener= clickListener;
    }*/
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cosmetic, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final CosmeticModel currentItem = mArrayList.get(position);
        // TODO set TextViews here
        String name = currentItem.getTitlez();
        holder.TXTname.setText(name);
        String adress = currentItem.getAddress();
        holder.TXTAdress.setText(adress);
        String email = currentItem.getEmail();
        holder.TXTemail.setText(email);
        String site = currentItem.getWebsite();
        holder.TXTwebsite.setText(site);
        int ratingcount = currentItem.getRating_counts();
        holder.TXTratingCount.setText(String.valueOf(ratingcount));
        final int favCheck;

        final int id = Integer.valueOf(currentItem.getIdz());


        Picasso.with(context).load(currentItem.getImage()).fit().centerInside().into(holder.IMGcover);

        if (favArray.contains(id)) {
            holder.toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_black_24dp_fill));
            favCheck = 1;
        } else {
            holder.toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_black_24dp));
            favCheck = 2;
        }


        holder.ratingBar.setRating(currentItem.getRating().floatValue());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("name", currentItem.getTitlez());
                intent.putExtra("address", currentItem.getAddress());
                intent.putExtra("email", currentItem.getEmail());
                intent.putExtra("website", currentItem.getWebsite());
                intent.putExtra("phone1", currentItem.getPhone_1());
                intent.putExtra("phone2", currentItem.getPhone_2());
                intent.putExtra("image", currentItem.getImage());
                intent.putExtra("note", currentItem.getDescription());
                intent.putExtra("id", id);
                intent.putExtra("type", 99101);
                intent.putExtra("fav",favCheck);

                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        if (mArrayList == null || mArrayList.size() == 0)
            return 0;
        return mArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

        TextView TXTname;
        TextView TXTratingCount;
        TextView TXTAdress;
        TextView TXTemail;
        TextView TXTwebsite;
        ImageView IMGcover;
        RatingBar ratingBar;
        ToggleButton toggleButton;
        boolean fav;

        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);
/*
            itemView.setOnClickListener(this);
*/
            TXTname = itemView.findViewById(R.id.Cosmetic_Title);
            TXTratingCount = itemView.findViewById(R.id.Cosmetic_ratingCounts);
            TXTAdress = itemView.findViewById(R.id.Cosmetic_adress);
            TXTemail = itemView.findViewById(R.id.Cosmetic_email);
            TXTwebsite = itemView.findViewById(R.id.Cosmetic_site);
            IMGcover = itemView.findViewById(R.id.Cosmetic_Image);
            toggleButton = itemView.findViewById(R.id.Cosmetic_ToggleButton);
            ratingBar = itemView.findViewById(R.id.Cosmetic_ratingBar);
            cardView = itemView.findViewById(R.id.Cosmetic_CardView);
        }

     /*   @Override
        public void onClick(View view) {
            if(ClickListener!=null)
                ClickListener.ItemClicked(view ,getAdapterPosition());
        }

        public void clearAnimation()
        {
            cardView.clearAnimation();
        }
    }

    public interface RecyclerViewClickListener
    {

        public void ItemClicked(View v, int position);
    }
*/
   /* private void setAnimation(View viewToAnimate, int position)
    {

        if (position > LastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            LastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.clearAnimation();
    }*/


        /************************* favourite requests ******************************/
        AddCosmeticClinicsToFavouriteRequest addBeautyCenterToFavouriteRequest;
        DeleteCosmeticClinicsFromFavouriteRequest deleteBeautyCenterFromFavouriteRequest;


        /************************* rating requests ******************************/
        RatingCosmeticClinicsRequest ratingBeautyCenterRequest;


        /************************* views requests ******************************/
        ViewsIncrementForCosmeticClinicsRequest viewsIncrementForBeautyCenterRequest;


    }
}

