package com.example.myapplication.device;

import com.example.myapplication.database.EquipmentBean;

public class Equipment extends EquipmentBean {
    private String status;
    private String src;               // 本设备的id，也就手机的device name
    private String Target;
    private int switchOfPic;
    private String temperature;
    private String humidity;

    private int index;                // 用于保存设备在list中的位置

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getTarget() {
        return Target;
    }

    public void setTarget(String target) {
        Target = target;
    }

    public int getSwitchOfPic() {
        return switchOfPic;
    }

    public void setSwitchOfPic(int switchOfPic) {
        this.switchOfPic = switchOfPic;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
