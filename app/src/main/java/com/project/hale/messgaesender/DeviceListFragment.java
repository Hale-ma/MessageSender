package com.project.hale.messgaesender;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.project.hale.messgaesender.Wifi.SenderCore;
import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.SenderWifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceListFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public List<SenderDevice> slist = new ArrayList<SenderDevice>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DeviceListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceListFragment.
     */
    public static DeviceListFragment newInstance(String param1, String param2) {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set list adapter with row layout to adapter data
        this.setListAdapter(new SenderDeviceListAdapter(getActivity(), R.layout.row_devices, SenderCore.getsInstance().getDeviceList()));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_list, null);
        TextView myName = (TextView) view.findViewById(R.id.my_name);
        TextView myDetail = (TextView) view.findViewById(R.id.my_detail);
        myName.setText("My Mac: " + SenderWifiManager.getMacAddr());
        myDetail.setText("Free");
        return view;
    }

    public void onButtonPressed(SenderDevice senderDevice) {
        if (mListener != null) {
            mListener.onFragmentInteraction(senderDevice);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        SenderDevice device = (SenderDevice) getListAdapter().getItem(position);
        mListener.onFragmentInteraction(device);
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

    public void updateUI() {
        if(getActivity()==null){
            Log.d("updateUI", "faild");
            return;
        }
        this.setListAdapter(new SenderDeviceListAdapter(getActivity(), R.layout.row_devices, SenderCore.getsInstance().getDeviceList()));
        //  this.setListAdapter(new SenderDeviceListAdapter(getActivity(), R.layout.row_devices, slist));
        Log.d("updateUI", "updateUI");
    }

    public void addDevice(SenderDevice s) {
        slist.add(s);
        this.updateUI();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(SenderDevice device);
    }

    private class SenderDeviceListAdapter extends ArrayAdapter<SenderDevice> {

        private List<SenderDevice> items;

        public SenderDeviceListAdapter(Context context, int textViewResourceId,
                                       List<SenderDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            SenderDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView time = (TextView) v.findViewById(R.id.device_time);
                TextView newMsg=(TextView)v.findViewById(R.id.msg_count);
                TextView bt=(TextView) v.findViewById(R.id.device_bt);
                TextView nearest=(TextView) v.findViewById(R.id.device_nearest);
                if (top != null) {
                    top.setText(device.toString() + " dis:" + device.distance);
                }
                if(time!=null){
                    time.setText(device.time);
                }
                if (bt != null) {
                    bt.setText(device.btaddress);
                }
                if(nearest!=null){
                    nearest.setText("NEAREST:"+device.nearestaddress);
                }
                if (newMsg != null) {

                    if(device.newMsg==0){
                        newMsg.setBackgroundResource(R.drawable.nomegicon);
                        //hide the hint
                    }else {
                        newMsg.setText(device.newMsg+"");
                        newMsg.setBackgroundResource(R.drawable.newmegicon);

                    }
                }
            }

            return v;
        }


    }


}
