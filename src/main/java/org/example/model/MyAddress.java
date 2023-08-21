package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAddress {
    private String query;
    private String latitude;
    private String longitude;
    private int radius;
}
