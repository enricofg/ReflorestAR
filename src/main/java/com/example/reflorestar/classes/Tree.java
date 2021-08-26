package com.example.reflorestar.classes;

import com.google.ar.core.Pose;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Tree {
    public float latitude;
    public float longitude;
    public float height;
    public String type;
    public float tx;
    public float ty;
    public float tz;
    public float qx;
    public float qy;
    public float qz;
    public float qw;

    public Tree() {
    }

    public Tree(float latitude, float longitude, float height, String type, float tx, float ty, float tz, float qx, float qy, float qz, float qw) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.type = type;
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.qx = qx;
        this.qy = qy;
        this.qz = qz;
        this.qw = qw;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getTx() {
        return tx;
    }

    public void setTx(float tx) {
        this.tx = tx;
    }

    public float getTy() {
        return ty;
    }

    public void setTy(float ty) {
        this.ty = ty;
    }

    public float getTz() {
        return tz;
    }

    public void setTz(float tz) {
        this.tz = tz;
    }

    public float getQx() {
        return qx;
    }

    public void setQx(float qx) {
        this.qx = qx;
    }

    public float getQy() {
        return qy;
    }

    public void setQy(float qy) {
        this.qy = qy;
    }

    public float getQz() {
        return qz;
    }

    public void setQz(float qz) {
        this.qz = qz;
    }

    public float getQw() {
        return qw;
    }

    public void setQw(float qw) {
        this.qw = qw;
    }
}
