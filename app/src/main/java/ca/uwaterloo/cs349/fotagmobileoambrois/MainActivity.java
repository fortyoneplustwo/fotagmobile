package ca.uwaterloo.cs349.fotagmobileoambrois;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.Rating;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private GridViewAdapter myAdapter = null;
    private ArrayList<ImageData> originalDataSet = null; // pointer to original list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get toolbar ref and bind
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set adapter on orientation switch
        GridView gridView = findViewById(R.id.gridView);
        if(savedInstanceState != null) {
            ArrayList<ImageData> imageDataList = savedInstanceState.getParcelableArrayList("imageDataList");
            if(imageDataList != null) {
                myAdapter = new GridViewAdapter(this, imageDataList);
                gridView.setAdapter(myAdapter);
            }
        }

        // get filter widget and disable it if adapter doesn't already exist
        RatingBar filter = findViewById(R.id.filter);
        if(myAdapter == null)  filter.setEnabled(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if(myAdapter != null) {
            state.putParcelableArrayList("imageDataList", myAdapter.getImageDataList());
        }
    }


    /* onClick Load Button */
    public void loadImages(View view) {
        // fetch images from URL, add to grid view and update
        String siteURL = "https://www.student.cs.uwaterloo.ca/~cs349/w19/assignments/images/";
        new ImageDownloader(this).execute(siteURL);

        // get ref to filter widget and set listener
        RatingBar filter = findViewById(R.id.filter);
        filter.setEnabled(true);
        filter.setRating(0);
    }


    /* onClick Clear Button */
    public void clearImages(View view) {
        // clear rating and detach listener of filter widget in toolbar
        RatingBar filter = findViewById(R.id.filter);
        filter.setOnRatingBarChangeListener(null);
        filter.setRating(0);
        filter.setEnabled(false);

        // clear grid view adapter
        GridView grid = findViewById(R.id.gridView);
        grid.setAdapter(null);
        myAdapter = null; // nullify global reference to the adapter
    }


    /* listener for filter widget */
    private class filterListener implements RatingBar.OnRatingBarChangeListener {
        Context context;
        GridViewAdapter adapter;



        // constructor
        private filterListener(Context context, GridViewAdapter adapter) {
            this.context = context;
            this.adapter = adapter;
        }

        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            if (true) {
                if (rating != 0) {
                    // filter data set
                    ArrayList<ImageData> filteredDataSet = new ArrayList<>();
                    for (int i = 0; i < originalDataSet.size(); ++i) {
                        if (originalDataSet.get(i).rating >= rating) {
                            filteredDataSet.add(filteredDataSet.size(), originalDataSet.get(i));
                            filteredDataSet.get(filteredDataSet.size() - 1).setPointerToOriginal(originalDataSet.get(i));

                        }
                    }
                    adapter.changeDataSet(filteredDataSet);
                    adapter.notifyDataSetChanged();
                } else {
                    // restore original data set
                    adapter.changeDataSet(originalDataSet);
                    adapter.notifyDataSetChanged();
                }

            }
        }
    }


    /* async task to handle image download */
    private class ImageDownloader extends AsyncTask<String, Void, ArrayList<ImageData>> {
        Context context;
        ProgressDialog progressDialog;

        // constructor
        private ImageDownloader(Context c) {
            this.context = c;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(this.context);
            progressDialog.setTitle("Loading images");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(10);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected ArrayList<ImageData> doInBackground(String... url) {
            String[] imgNames = {"bunny.jpg", "chinchilla.jpg", "doggo.jpg", "fox.jpg",
                    "hamster.jpg", "husky.jpg", "kitten.png", "loris.jpg",
                    "puppy.jpg", "sleepy.png"};

            ArrayList<ImageData> imageDataList = new ArrayList<>();
            try {
                for (int i = 0; i < 10; i++) {
                    String u = url[0] + imgNames[i];
                    Bitmap bitmap;
                    URL imageURL = new URL(u);
                    bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                    if (bitmap != null) {
                        ImageData id = new ImageData(bitmap, 0);
                        imageDataList.add(id);
                        // increment progress after an image is downloaded
                        progressDialog.incrementProgressBy(1);
                    }
                }
            } catch (Exception e) {
                    e.printStackTrace();
                }
            return imageDataList;
        }

        @Override
        protected void onPostExecute(ArrayList<ImageData> imageDataList) {
            // get ref to grid view and set adapter
            GridView gridView = findViewById(R.id.gridView);
            GridViewAdapter gridViewAdapter = new GridViewAdapter(this.context, imageDataList);
            myAdapter = gridViewAdapter; //
            gridView.setAdapter(gridViewAdapter);
            // dismiss progress dialogue
            progressDialog.dismiss();
        }
    }


    /* grid view adapter */
    private class GridViewAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<ImageData> imageDataList;

        // constructor
        private GridViewAdapter(Context context, ArrayList<ImageData> imageDataList) {
            this.context = context;
            this.imageDataList = imageDataList;
            originalDataSet = this.imageDataList;

            // set listener for filter
            RatingBar filter = findViewById(R.id.filter);
            filter.setOnRatingBarChangeListener(new filterListener(context, this));
        }

        private void changeDataSet(ArrayList<ImageData> newDataSet) {
            this.imageDataList = newDataSet;

        }


        private ArrayList<ImageData> getImageDataList() { return this.imageDataList; }

        @Override
        public int getCount() {
            return imageDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return imageDataList.get(i);
        }


        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            ImageView imageView;
            RatingBar imageRating;

            LayoutInflater inflater = (LayoutInflater)
                    this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.grid_slot, parent, false);
            }

            // set image and it's listener
            imageView = convertView.findViewById(R.id.imageView);
            imageView.setImageBitmap(this.imageDataList.get(i).getBitmap());
            imageView.setOnClickListener(new OnClickImageListener(i, this.imageDataList));

            final ImageData image = imageDataList.get(i);
            // set rating and it's listener
            imageRating = convertView.findViewById(R.id.imageRating);
            imageRating.setRating(image.getRating());
            imageRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                   if(fromUser){
                       image.setRating((int) rating);
                       // reflect rating change in original list
                       if(image.original != null) { image.original.setRating((int) rating); }
                       notifyDataSetChanged();
                       // filter data set once more
                       RatingBar filter = findViewById(R.id.filter);
                       filterListener listener = new filterListener(context, myAdapter);
                       listener.onRatingChanged(filter, filter.getRating(), true );
                   }
                }
            });

            return convertView;
        }
    }


    /* listener for image rating */
    private class imageRatingListener implements RatingBar.OnRatingBarChangeListener {
        Context context;
        ArrayList<ImageData> imageDataList;
        GridViewAdapter adapter;
        int position;

        // constructor
        private imageRatingListener(int position, Context context, GridViewAdapter adapter, ArrayList<ImageData> imageDataList) {
            this.context = context;
            this.imageDataList = imageDataList;
            this.position = position;
            this.adapter = adapter;
        }

        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            // change rating in our image data list and notify adapter
            imageDataList.get(position).setRating((int) ratingBar.getRating());
            adapter.notifyDataSetChanged();
        }
    }

    /* listener to display image on click */
    private class OnClickImageListener implements View.OnClickListener {
        private int position;
        private ArrayList<ImageData> imageDataList;

        private OnClickImageListener(int position, ArrayList<ImageData> imageDataList) {
            this.position = position;
            this.imageDataList = imageDataList;
        }

        @Override
        public void onClick(View view) {
            Bitmap bitmap = imageDataList.get(position).getBitmap();
            try { // write to file
                FileOutputStream stream = openFileOutput("bitmap.jpg", Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.close();

                // create intent and start activity to display image
                Intent intent = new Intent(MainActivity.this, DisplayImageActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Stores relevant data of an image (bitmap + rating) */
    private class ImageData implements Parcelable {
        private Bitmap bitmap;
        private Integer rating;
        private ImageData original = null; // pointer to duplicate in original data set

        // constructor
        private ImageData(Bitmap bitmap, Integer rating) {
            this.bitmap = bitmap;
            this.rating = rating;
        }

        // getters and setters
        private Bitmap getBitmap() { return this.bitmap; }
        private Integer getRating() { return this.rating; }
        private void setRating(int r) { rating = r; }
        private void setPointerToOriginal(ImageData original) { this.original = original; }

        private ImageData(Parcel in) {
            bitmap = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
            rating = in.readByte() == 0x00 ? null : in.readInt();
        }

        /* code below was generated from http://www.parcelabler.com */
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(bitmap);
            if (rating == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeInt(rating);
            }
        }

        @SuppressWarnings("unused")
        public final Parcelable.Creator<ImageData> CREATOR = new Parcelable.Creator<ImageData>() {
            @Override
            public ImageData createFromParcel(Parcel in) {
                return new ImageData(in);
            }

            @Override
            public ImageData[] newArray(int size) {
                return new ImageData[size];
            }
        };
    }
}