package com.ripyatakov.eqserver.html;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewHtml {

    


    private String userName;
    private String description;
    private float rate;
    private java.util.Date time;
}
