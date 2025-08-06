package com.trip_gg.count;

import lombok.*;

@Data
@Getter
@Setter
public class Count {

    private int posts_id;
    private int comment_count;
    private int like_count;
    private int view_count;
}
