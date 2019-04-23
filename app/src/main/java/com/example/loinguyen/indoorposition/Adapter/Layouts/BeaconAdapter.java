package com.example.loinguyen.indoorposition.Adapter.Layouts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.loinguyen.indoorposition.R;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class BeaconAdapter extends BaseAdapter {
    private Context mcontext;

    private LayoutInflater inflater;

    public static ArrayList<Beacon> beacons;

    public BeaconAdapter(Context context){
        this.mcontext = context;
        this.inflater = LayoutInflater.from(context);
        this.beacons = new ArrayList<Beacon>();
    }

    public void initAll(Collection<Beacon> newBeacons){
        this.beacons.clear();
        this.beacons.addAll(newBeacons);
        notifyDataSetChanged();
        Collections.sort(this.beacons, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon b1, Beacon b2) {
                double rssi1 = b1.getRssi();
                double rssi2 = b2.getRssi();
                return rssi1 > rssi2 ? -1 : 1;
            }
        });
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Beacon getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.beacon_list_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        this.bind(getItem(position), position, convertView);
        return convertView;
    }

    private void bind(Beacon beacon, int position, View view){
        ViewHolder holder = (ViewHolder) view.getTag();
//        holder.macTextView.setText("MAC: " + beacon.getBluetoothAddress());
//        holder.uudiTextView.setText("UUDI: " + beacon.getId1());
        holder.majorTextView.setText("MAJOR: " + beacon.getId2());
//        holder.minor.setText("Minor: " + beacon.getId3());
//        holder.txpower.setText("TxPower: " + beacon.getTxPower() + " dBm");
        holder.rssi.setText("RSSI: " + beacon.getRssi());
//        holder.distance.setText("Distance: " + computeDistance(beacon) + " m");
    }

    public static double computeDistance(Beacon beacon){
        double n = 1;
        if (beacon.getRssi() == 0) {
            return -1.0;
        } else {
            double rssi = beacon.getRssi();
            if(rssi >= (-65) && rssi <= 0)              n = 1;
            else if(rssi >= (-70) && rssi < (-65))      n = 2.5;
            else if(rssi >= (-75) && rssi <= (-71))     n = 3;
            else if(rssi >= (-80) && rssi <= (-76))     n = 3.5;
            else if(rssi >= (-85) && rssi <= (-81))     n = 3.7;
            else if(rssi >= (-90) && rssi <= (-86))     n = 4.0;
            else if(rssi >= (-100) && rssi < (-90))     n = 4.5;
        }
            double distance = (double) Math.pow((double)10, (double)(-64-beacon.getRssi())/(10*n));
            return distance;
    }

    static class ViewHolder{
        //final TextView macTextView;
        //final TextView uudiTextView;
        final TextView majorTextView;
        //final TextView distance;
        //final TextView minor;
        //final TextView txpower;
        final TextView rssi;
        ViewHolder(View view) {
           // macTextView = (TextView) view.findViewById(R.id.mac);
            //uudiTextView = (TextView) view.findViewById(R.id.uudi);
            majorTextView = (TextView) view.findViewById(R.id.major);
            //distance = (TextView) view.findViewById(R.id.distance);
            //minor = (TextView) view.findViewById(R.id.minor);
            //txpower = (TextView) view.findViewById(R.id.txpower);
            rssi = (TextView) view.findViewById(R.id.rssi);
        }
    }
}
