package dev.badbird.griefpreventiontp.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FilterOptions {
    private String nameFilter;
    private boolean privateClaims;
}
