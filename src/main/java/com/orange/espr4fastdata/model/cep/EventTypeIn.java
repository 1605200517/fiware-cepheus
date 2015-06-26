package com.orange.espr4fastdata.model.cep;

/**
 * Created by pborscia on 03/06/2015.
 */
public class EventTypeIn extends EventType {
    private String provider;

    public EventTypeIn() {
        super();
    }

    public EventTypeIn(String id, String type, boolean isPattern) {
        super(id, type, isPattern);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
