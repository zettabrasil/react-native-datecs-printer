package br.com.zettabrasil.datecsprinter.model;

import com.facebook.react.bridge.ReadableMap;

/**
 * Created by zettabrasil on 11/04/16.
 */
public class DeviceBt {

    private String name;
    private String address;

    public DeviceBt(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public DeviceBt(ReadableMap data) {
        this.name = data.getString("name");
        this.address = data.getString("address");
    }

    public DeviceBt(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
