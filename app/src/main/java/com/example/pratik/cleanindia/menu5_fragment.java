package com.example.pratik.cleanindia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Pratik on 04-Mar-15.
 */
public class menu5_fragment extends Fragment
{
    View rootview;
Button chksts;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.menu5_layout,container,false);
        chksts = (Button) rootview.findViewById(R.id.chkStatusThankYou);
        chksts.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Fragment objFrag = null;
                objFrag = new menu2_fragment();

                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container,objFrag);
                ft.commit();
            }
        });
        return rootview;
    }
}
