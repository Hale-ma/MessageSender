package com.project.hale.messgaesender;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.project.hale.messgaesender.Wifi.SenderDevice;
import com.project.hale.messgaesender.Wifi.SenderWifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * The Fragment the provide GUI for Device list
 *
 */
public class DeviceListFragment extends ListFragment {
    public List<SenderDevice> slist = new ArrayList<SenderDevice>();
    private OnFragmentInteractionListener mListener;

    public DeviceListFragment() {
        // Required empty public constructor
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
