package com.ripyatakov.eqserver.html;

import com.ripyatakov.eqserver.service.ReviewService;
import com.ripyatakov.eqserver.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

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
