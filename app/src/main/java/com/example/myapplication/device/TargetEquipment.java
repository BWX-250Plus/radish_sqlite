package com.example.myapplication.device;

import com.example.myapplication.database.EquipmentBean;

public class TargetEquipment extends EquipmentBean {
    private String status;
    private String masterEquipmentName;
    private String temperature;
    private String humidity;

    public int getSwitchOfPic() {
        return SwitchOfPic;
    }

    public void setSwitchOfPic(int switchOfPic) {
        SwitchOfPic = switchOfPic;
    }

    private int SwitchOfPic;

    public String getMasterEquipmentName() {
        return masterEquipmentName;
    }

    public void setMasterEquipmentName(String masterEquipmentName) {
        this.masterEquipmentName = masterEquipmentName;
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
