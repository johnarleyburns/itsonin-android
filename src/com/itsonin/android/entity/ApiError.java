package com.itsonin.android.entity;

import com.itsonin.android.enums.DeviceLevel;
import com.itsonin.android.enums.DeviceType;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 4/14/14
 * Time: 10:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApiError implements Serializable {

    // {"status":"error","message":"Error saving event: Event name is required, Guest name is required"}

        private static final long serialVersionUID = 1L;

        private String status;
        private String message;

        @SuppressWarnings("unused")
        private ApiError(){}

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
}
