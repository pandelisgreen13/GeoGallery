package pandelis.ga_testnavig;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class Language extends Fragment {
    Button button1,button2,button3;
    Bundle tempBundle;

    public Language() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.language, container, false);

        button1=(Button)view.findViewById(R.id.english);
        button2=(Button)view.findViewById(R.id.spanish);
        button3=(Button)view.findViewById(R.id.default_la);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Locale locale1 = new Locale("en");
                Locale.setDefault(locale1);
                Configuration config = new Configuration();
                config.locale = locale1;
                getActivity().getResources().updateConfiguration(config,getActivity().getResources().getDisplayMetrics());
                tempBundle = new Bundle();
                onCreate(tempBundle);

            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Locale locale1 = new Locale("es");
                Locale.setDefault(locale1);
                Configuration config = new Configuration();
                config.locale = locale1;
                getActivity().getResources().updateConfiguration(config,
                        getActivity().getResources().getDisplayMetrics());
                tempBundle = new Bundle();
                onCreate(tempBundle);

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Locale locale1 = new Locale("");
                Locale.setDefault(locale1);
                Configuration config = new Configuration();
                config.locale = locale1;
                getActivity().getResources().updateConfiguration(config,
                        getActivity().getResources().getDisplayMetrics());
                tempBundle = new Bundle();
                onCreate(tempBundle);
            }
        });
        return view;
    }
}
