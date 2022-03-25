package com.fimbleenterprises.demobuddy.objects_and_containers;

import org.json.JSONException;
import org.json.JSONObject;

public class RestResponse {

    public String context;
    public String value;

    /**
     * Converts a json string or a boolean string to a new RestResponse
     * @param response json formatted text or a boolean text value.
     */
    public RestResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            this.context = json.get("context").toString();
            this.value = json.get("value").toString();
        } catch (JSONException e) {
            try {
                boolean bool = Boolean.getBoolean(response);
                this.context = null;
                this.value = response;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
