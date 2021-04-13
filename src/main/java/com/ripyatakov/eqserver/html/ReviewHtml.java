package com.ripyatakov.eqserver.html;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewHtml {
    private int qid;
    private int uid;
    private String userName;
    private String description;
    private float rate;
    private java.util.Date time;
}
