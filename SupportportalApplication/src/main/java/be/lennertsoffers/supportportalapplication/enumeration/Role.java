package be.lennertsoffers.supportportalapplication.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static be.lennertsoffers.supportportalapplication.constant.Authority.*;

@Getter
@AllArgsConstructor
public enum Role {
    ROLE_USER(USER_AUTHORITIES),
    ROLE_HR(HR_AUTHORITIES),
    ROLE_MANAGER(MANAGER_AUTHORITIES),
    ROLE_ADMIN(ADMIN_AUTHORITIES),
    ROLE_SUPER_USER(SUPER_USER_AUTHORITIES);

    private String[] authorities;
}
