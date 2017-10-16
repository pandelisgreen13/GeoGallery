package pandelis.ga_testnavig;


import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import static pandelis.ga_testnavig.R.id.sum;


/**
 * A simple {@link Fragment} subclass.
 */
public class Statistics extends Fragment {

    FirebaseDatabase fbdatabase;
    DatabaseReference fbref;
    LocationVar locationVar;
    Geocoder geocoder;
    int i,results=0,onFoot=0,incar=0,motionless=0;
    long sum=0;
    TextView textView,textView2,textView3,textView4,textView5;
    ProgressBar progressBar;
    String result="";
    List<Address> addressList;
    Boolean end=false;
    private MyTask mTask;
    private static final String STATE_TASK_RUNNING = "taskRunning";

    ArrayList<String> Places = new ArrayList<>();
    ArrayList<Double> SpeedLocation = new ArrayList<>();
    public Statistics() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_statistics_, container, false);
        textView=(TextView)view.findViewById(R.id.loc);
        textView2=(TextView)view.findViewById(R.id.onfoot);
        textView3=(TextView)view.findViewById(R.id.incar);
        textView4=(TextView)view.findViewById(R.id.motionless);
        textView5=(TextView)view.findViewById(R.id.sum);


        //check the firebase
        String id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        fbdatabase = FirebaseDatabase.getInstance();
        fbref = fbdatabase.getReference(id);
         geocoder = new Geocoder(getActivity());

        progressBar=(ProgressBar)view.findViewById(R.id.progressBar3);

        Toast.makeText(getActivity(),"Downloading Data...",Toast.LENGTH_LONG).show();
        fbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child: dataSnapshot.getChildren()) {

                    locationVar = child.getValue(LocationVar.class);
                    //with gecoder and  we calculate the most visited location in background
                    new MyTask(locationVar.getLati(),locationVar.getLong(),locationVar.getSpeed()).execute();
                    results++;
                }
                sum=dataSnapshot.getChildrenCount();
            }
                        @Override
            public void onCancelled(DatabaseError databaseError) {
                            mTask.cancel(true);
            }
        });

        if(results==sum){
        end=true;
        progressBar.setProgress(50);
        }

        return view;
    }


    private boolean isTaskRunning() {
        return (mTask != null) && (mTask.getStatus() == AsyncTask.Status.RUNNING);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel the task if it's running
        if (isTaskRunning()) {
            mTask.cancel(true);
        }
    }

    class MyTask extends AsyncTask<Double,Void,String>
    {
        Double intValue;
        Double strValue;
        Double speed;

        public MyTask(Double intValue,Double strValue,Double speed){
            this.intValue = intValue;
            this.strValue = strValue;
            this.speed= speed;
        }


        @Override
        protected String doInBackground(Double... strings) {
            addressList=null;
            //from coordinates we get the locality of the photo
            try {
                addressList=geocoder.getFromLocation(strValue,intValue,1);
                Address address= addressList.get(0);
                Places.add(address.getLocality());
                SpeedLocation.add(speed);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //check for the most visited location and the status of speed
            int  count=0;
            if(Places.size()==results) {
                for (i = 0; i < Places.size(); i++) {

                    String temp = Places.get(i);
                    int temp_count = 0;

                    if (SpeedLocation.get(i) == 0) {
                        motionless++;
                    } else if (SpeedLocation.get(i) <= 7) {
                        onFoot++;
                    } else if (SpeedLocation.get(i) > 7) {
                        incar++;
                    }
                    for (int j = 0; j < Places.size(); j++) {
                        if (Places.get(j).equals(temp)) {
                            temp_count++;
                        }
                        if (temp_count > count) {
                            result = temp;
                            count = temp_count;
                        }
                    }
                    if(isCancelled()){
                        mTask.cancel(true);
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //show the results when the thread is done
            if(end=true) {
                progressBar.setProgress(100);
                textView.setText("" + result);
                textView2.setText(getResources().getString(R.string.motionless) + ": " + motionless);
                textView3.setText(getResources().getString(R.string.incar) + ": " + incar);
                textView4.setText(getResources().getString(R.string.onwalk) + ": " + onFoot);
                textView5.setText(getResources().getString(R.string.results) + ": " +results);
                progressBar.setVisibility(View.INVISIBLE);
                end=false;
            }
        }
    }


}





//        fbref.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                locationVar = dataSnapshot.getValue(LocationVar.class);
//
//                List<Address> addressList=null;
//
//                    try {
//                        addressList=geocoder.getFromLocation(locationVar.getLong(),locationVar.getLati(),1);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Address address= addressList.get(0);
//                    Places.add(address.getLocality());
//
//                }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });

//        int  count=0;
//        String result="";
//        for( i=0; i<=Places.size();i++){
//            String temp=Places.get(i);
//            int temp_count=0;
//
//            for(int j=0;j<=Places.size();j++){
//                if(Places.get(j).equals(temp)){
//                    temp_count++;
//                }
//                if(temp_count>count){
//                    result=temp;
//                    count=temp_count;
//                }
//
//
//            }
//        }


