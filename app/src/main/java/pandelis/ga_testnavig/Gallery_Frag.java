package pandelis.ga_testnavig;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Gallery_Frag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Gallery_Frag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Gallery_Frag extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    FirebaseDatabase fbdatabase;
    DatabaseReference fbref;


    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;
    private static final String TAG = Gallery_Frag.class.getSimpleName();
    ImageAdapter imageAdapter;
    GridView gridView;
    public Boolean onclick=false;

    public Gallery_Frag() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Gallery_Frag newInstance(String param1) {

        Gallery_Frag fragment = new Gallery_Frag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get the sum of images
        imageAdapter= new ImageAdapter(getContext());
        imageAdapter.getCount();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_gallery_, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        //create gridview and display images
        imageAdapter.getCount();
        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setAdapter(new ImageAdapter(getActivity()));
        //set two OnClickListeners
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String data) {
        if (mListener != null) {
            //Send data back to main Activity
            mListener.onFragmentInteraction(data);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        onclick=true;
        //check  position of image that has clicked
        imageAdapter = new ImageAdapter(getContext());
        imageAdapter.getCount();
        String path= imageAdapter.imageList.get(position);
        //open a new activity to display the image that clicked in full screen
        Intent intent = new Intent(getActivity(), ActivityImage.class);
        intent.putExtra("str1", path);
        startActivity(intent);

    }

    //On long Click we can delete an image
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {

        // // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.Message))
                .setTitle(getResources().getString(R.string.delete_photo)).setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User delete the image from the device
                String path= imageAdapter.imageList.get(position);

                File targetDirector = new File(path);
                targetDirector.delete();
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.delete),
                        Toast.LENGTH_LONG).show();

                //refresh Gridview after delete photo
                imageAdapter.notifyDataSetChanged();
               // imageAdapter.getCount();
                gridView.invalidateViews();
                gridView.setAdapter(new ImageAdapter(getActivity()));
                onclick=false;


            }
        })
        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                onclick=true;
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        AlertDialog alertDialog= builder.create();
        alertDialog.show();
        return true;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String data);
    }

    public void onResume(){
        super.onResume();

        if(onclick==false ) {
            //refresh Gridview after a new photo has taken
            imageAdapter = new ImageAdapter(getContext());
//        imageAdapter.notifyDataSetChanged();
            imageAdapter.getCount();
            gridView.invalidateViews();
            gridView.setAdapter(new ImageAdapter(getActivity()));
        }
        onclick=false;
    }

}

