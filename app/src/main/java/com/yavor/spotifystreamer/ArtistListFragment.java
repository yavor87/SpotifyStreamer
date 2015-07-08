package com.yavor.spotifystreamer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ArtistListFragment extends ListFragment {
    private static final String TAG = ArtistListFragment.class.getSimpleName();
    private SpotifyService mSpotify;
    private ArtistArrayAdapter mAdapter;

    public ArtistListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_list, container, false);

        EditText editText = (EditText) view.findViewById(R.id.search_bar);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ArtistArrayAdapter(getActivity(), new ArrayList<Artist>());
        setListAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        SpotifyApi api = new SpotifyApi();
        mSpotify = api.getService();
    }

    void performSearch(String text) {
        Log.v(TAG, "Doing a search for " + text);
        mSpotify.searchArtists(text, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                if (artistsPager.artists.total > 0) {
                    for (Artist artist : artistsPager.artists.items) {
                        mAdapter.add(artist);
                    }
                } else {
                    Toast t = Toast.makeText(getActivity(), getString(R.string.no_artists_message), Toast.LENGTH_SHORT);
                    t.show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Failure: " + error.getMessage(), error);
            }
        });
    }

    private class ArtistArrayAdapter extends ArrayAdapter<Artist> {
        private final Context mContext;
        private final ArrayList<Artist> mItemsArrayList;

        public ArtistArrayAdapter(Context context, ArrayList<Artist> itemsArrayList) {

            super(context, R.layout.artist_view, itemsArrayList);

            mContext = context;
            mItemsArrayList = itemsArrayList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.artist_view, parent, false);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.artist_image);
            TextView textView = (TextView) rowView.findViewById(R.id.artist_name);

            Artist currentArtist = mItemsArrayList.get(position);
            if (currentArtist.images.size() > 0) {
                Image image = currentArtist.images.get(currentArtist.images.size() - 1);
                Picasso.with(mContext).load(image.url).into(imageView);
            }
            textView.setText(currentArtist.name);

            return rowView;
        }
    }
}
