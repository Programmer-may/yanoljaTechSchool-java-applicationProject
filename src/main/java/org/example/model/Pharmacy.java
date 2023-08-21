package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy implements Comparable<Pharmacy> {
    private String url;
    private String businessName;
    private String address;
    private String phoneNumber;
    private Float distance;

    @Override
    public int compareTo(Pharmacy o) {
        return (int) (this.getDistance() - o.getDistance());
    }
}
